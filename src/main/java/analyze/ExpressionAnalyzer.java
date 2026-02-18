package analyze;

import analyze.ds.Library;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        ExecutionResult result = execute(llRoot, null, llRoot.resolve("main.ll").toString());
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
        if (!"file".equals(stdUrl.getProtocol())) {
            throw new RuntimeException("Unsupported resource protocol for /std: " + stdUrl.getProtocol());
        }
        List<Library.LibraryFile> files = new ArrayList<>();
        try {
            URI uri = stdUrl.toURI();
            Path stdPath = Path.of(uri);
            try (var stream = Files.list(stdPath)) {

                stream.forEach(p -> {
                    String content = null;
                    try {
                        content = Files.readString(p);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    files.add(new Library.LibraryFile("std/" + p.getFileName(), content));
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load default libraries from /std", e);
        }
        return new Library(List.copyOf(files));
    }


    public static ExecutionResult execute(Path llRoot, String programSource, String sourceLabel) {
        String label = sourceLabel;
        if (label == null) {
            label = llRoot != null ? llRoot.resolve("main.ll").toString() : "<inline>";
        }

        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        ByteArrayOutputStream errBuf = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        Exception error = null;

        try {
            System.setOut(new PrintStream(outBuf, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(errBuf, true, StandardCharsets.UTF_8));

            if (llRoot == null && programSource == null) {
                throw new IllegalArgumentException("Either llRoot or programSource must be provided.");
            }

            runProgram(llRoot, programSource);
        } catch (ScriptRuntimeException e) {
            error = e;
            writeScriptError(errBuf, e, label);
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

    private static final class ModuleSource {
        final String code;
        final String name;

        ModuleSource(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    private static void runProgram(Path llRoot, String programSource) throws IOException {
        String programSrc = programSource == null
                ? Files.readString(llRoot.resolve("main.ll"))
                : programSource;
        Parser p = new Parser(new Lexer(programSrc));
        Map<String, Function> funcs = p.parseProgram();
        Map<String, Map<String, Function>> modules = new HashMap<>();
        Map<String, String> moduleLabels = new HashMap<>();
        for (Parser.ImportRef ref : p.getImports()) {
            String module = ref.module;
            Map<String, Function> moduleFuncs = modules.get(module);
            if (moduleFuncs == null) {
                try {
                    ModuleSource moduleSrc = readModuleSource(llRoot, module);
                    Parser moduleParser = new Parser(new Lexer(moduleSrc.code));
                    moduleFuncs = moduleParser.parseProgram();
                    modules.put(module, moduleFuncs);
                    moduleLabels.put(module, moduleSrc.name);
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

    private static ModuleSource readModuleSource(Path llRoot, String module) throws IOException {
        if (llRoot != null) {
            Path filePath = llRoot.resolve(module + ".ll");
            if (Files.exists(filePath)) {
                return new ModuleSource(Files.readString(filePath), filePath.toString());
            }
            Path fallbackPath = Path.of("resource", module + ".ll");
            if (Files.exists(fallbackPath)) {
                return new ModuleSource(Files.readString(fallbackPath), fallbackPath.toString());
            }
        }
        if ("std".equals(module)) {
            try (InputStream in = ExpressionAnalyzer.class.getResourceAsStream("/std/std.ll")) {
                if (in != null) {
                    return new ModuleSource(new String(in.readAllBytes(), StandardCharsets.UTF_8), "jar:/std.ll");
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
}
