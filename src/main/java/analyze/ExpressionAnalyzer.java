package analyze;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ExpressionAnalyzer {
    public static final class ExecutionResult {
        public final String stdout;
        public final String stderr;
        public final Exception error;

        ExecutionResult(String stdout, String stderr, Exception error) {
            this.stdout = stdout;
            this.stderr = stderr;
            this.error = error;
        }
    }

    public static void main(String[] args) throws IOException {
        Path llRoot = args.length > 0 ? Path.of(args[0]) : Path.of(".");
        ExecutionResult result = execute(loadSourcesFromRoot(llRoot));
        if (!result.stdout.isEmpty()) {
            System.out.print(result.stdout);
        }
        if (!result.stderr.isEmpty()) {
            System.err.print(result.stderr);
        }
    }

    public static Library getDefaultLibraries() {
        URL stdUrl = ExpressionAnalyzer.class.getResource("/std");
        if (stdUrl == null) {
            return new Library(List.of());
        }
        List<Library.LibraryFile> files = new ArrayList<>();
        try {
            String protocol = stdUrl.getProtocol();
            if ("file".equals(protocol)) {
                URI uri = stdUrl.toURI();
                Path stdPath = Path.of(uri);
                try (var stream = Files.list(stdPath)) {
                    stream.forEach(p -> {
                        String content;
                        try {
                            content = Files.readString(p);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        files.add(new Library.LibraryFile(p.getFileName().toString(), content));
                    });
                }
            } else if ("jar".equals(protocol)) {
                JarURLConnection conn = (JarURLConnection) stdUrl.openConnection();
                String root = conn.getEntryName();
                String rootNormalized = root == null ? "" : root.endsWith("/") ? root.substring(0, root.length() - 1) : root;
                try (JarFile jar = conn.getJarFile()) {
                    jar.stream()
                            .filter(entry -> isStdLibraryEntry(entry, rootNormalized))
                            .forEach(entry -> {
                                String relName = entry.getName().substring(rootNormalized.length() + 1);
                                try (InputStream in = jar.getInputStream(entry)) {
                                    String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                                    files.add(new Library.LibraryFile(relName, content));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                }
            } else {
                throw new RuntimeException("Unsupported resource protocol for /std: " + protocol);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load default libraries from /std", e);
        }
        return new Library(List.copyOf(files));
    }

    private static boolean isStdLibraryEntry(JarEntry entry, String root) {
        if (entry.isDirectory()) {
            return false;
        }
        if (root == null || root.isEmpty()) {
            return false;
        }
        String name = entry.getName();
        return name.startsWith(root + "/") && name.length() > root.length() + 1;
    }

    public static ExecutionResult execute(List<SourceFile> sources) {
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        Exception error = null;

        try {
            System.setOut(new PrintStream(outBuf, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(errBuf, true, StandardCharsets.UTF_8));

            if (sources == null || sources.isEmpty()) {
                throw new IllegalArgumentException("At least one source file must be provided.");
            }

            runProgram(sources);
        } catch (ScriptRuntimeException e) {
            error = e;
            writeScriptError(errBuf, e, findMainLabel(sources));
            e.printStackTrace(new PrintStream(errBuf, true, StandardCharsets.UTF_8));
        } catch (Exception e) {
            error = e;
            e.printStackTrace(new PrintStream(errBuf, true, StandardCharsets.UTF_8));
        } finally {
            System.setOut(oldOut);
            System.setErr(oldErr);
        }

        String stdout = outBuf.toString(StandardCharsets.UTF_8);
        String stderr = errBuf.toString(StandardCharsets.UTF_8);
        return new ExecutionResult(stdout, stderr, error);
    }

    private static void runProgram(List<SourceFile> sources) throws IOException {
        Map<String, SourceFile> sourceMap = toSourceMap(sources);
        SourceFile mainSource = sourceMap.get("main.ll");
        if (mainSource == null) {
            throw new IllegalArgumentException("Missing main source file: main.ll");
        }
        String programSrc = mainSource.content();
        Parser p = new Parser(new Lexer(programSrc));
        Map<String, Function> funcs = p.parseProgram();
        Map<String, Map<String, Function>> modules = new HashMap<>();
        Map<String, String> moduleLabels = new HashMap<>();
        for (Parser.ImportRef ref : p.getImports()) {
            String module = ref.module;
            Map<String, Function> moduleFuncs = modules.get(module);
            if (moduleFuncs == null) {
                try {
                    SourceFile moduleSrc = readModuleSource(sourceMap, module);
                    Parser moduleParser = new Parser(new Lexer(moduleSrc.content()));
                    moduleFuncs = moduleParser.parseProgram();
                    modules.put(module, moduleFuncs);
                    moduleLabels.put(module, moduleSrc.fileName());
                } catch (IOException e) {
                    throw new ScriptRuntimeException("Module file not found: " + module, ref.line, ref.col, List.of());
                }
            }
            Function f = moduleFuncs.get(ref.name);
            if (f == null) {
                String moduleLabel = moduleLabels.getOrDefault(module, module + ".ll");
                throw new ScriptRuntimeException(
                        "Unknown function in module " + module + ": " + ref.name + " (in " + moduleLabel + ")",
                        ref.line,
                        ref.col,
                        List.of()
                );
            }
            if (funcs.containsKey(ref.alias)) {
                throw new ScriptRuntimeException("Function already defined: " + ref.alias, ref.line, ref.col, List.of());
            }
            funcs.put(ref.alias, f);
        }
        Env env = new Env(funcs, null);
        env.call("main", List.of(), 1, 1);
    }

    private static SourceFile readModuleSource(Map<String, SourceFile> sources, String module) throws IOException {
        SourceFile fromSources = sources.get(module + ".ll");
        if (fromSources != null) {
            return fromSources;
        }
        if ("std".equals(module)) {
            try (InputStream in = ExpressionAnalyzer.class.getResourceAsStream("/std/std.ll")) {
                if (in != null) {
                    return new SourceFile("jar:/std.ll", new String(in.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
        }
        throw new IOException("Module file not found: " + module);
    }

    private static void writeScriptError(ByteArrayOutputStream errBuf, ScriptRuntimeException e, String sourceLabel) {
        PrintStream err = new PrintStream(errBuf, true, StandardCharsets.UTF_8);
        err.println("Script error in " + sourceLabel + ": " + e.getMessage() + " at " + e.line + ":" + e.col);
        if (!e.stack.isEmpty()) {
            err.println("Script stack:");
            for (ScriptFrame f : e.stack) {
                err.println("  at " + f.name + " (" + f.line + ":" + f.col + ")");
            }
        }
    }

    private static List<SourceFile> loadSourcesFromRoot(Path llRoot) throws IOException {
        List<SourceFile> sources = new ArrayList<>();
        try (var stream = Files.list(llRoot)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".ll"))
                    .forEach(p -> {
                        try {
                            sources.add(new SourceFile(p.getFileName().toString(), Files.readString(p)));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        return sources;
    }

    private static Map<String, SourceFile> toSourceMap(List<SourceFile> sources) {
        Map<String, SourceFile> sourceMap = new HashMap<>();
        for (SourceFile source : sources) {
            SourceFile existing = sourceMap.putIfAbsent(source.fileName(), source);
            if (existing != null) {
                throw new IllegalArgumentException("Duplicate source file: " + source.fileName());
            }
        }
        return sourceMap;
    }

    private static String findMainLabel(List<SourceFile> sources) {
        for (SourceFile source : sources) {
            if ("main.ll".equals(source.fileName())) {
                return source.fileName();
            }
        }
        return sources.isEmpty() ? "<unknown>" : sources.get(0).fileName();
    }
}
