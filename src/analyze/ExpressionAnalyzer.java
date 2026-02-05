package analyze;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ExpressionAnalyzer {

    public static void main(String[] args) throws IOException {
        String programSrc = Files.readString(Path.of("/home/vader/IdeaProjects/lexical-analyzer/resource/program.java"));
        Parser p = new Parser(new Lexer(programSrc));
        try {
            Map<String, Function> funcs = p.parseProgram();
            Map<String, Map<String, Function>> modules = new java.util.HashMap<>();
            for (Parser.ImportRef ref : p.getImports()) {
                String module = ref.module;
                Map<String, Function> moduleFuncs = modules.get(module);
                if (moduleFuncs == null) {
                    try {
                        String moduleSrc = Files.readString(Path.of("/home/vader/IdeaProjects/lexical-analyzer/resource/" + module + ".java"));
                        Parser moduleParser = new Parser(new Lexer(moduleSrc));
                        moduleFuncs = moduleParser.parseProgram();
                        modules.put(module, moduleFuncs);
                    } catch (IOException e) {
                        throw new ScriptRuntimeException("Module file not found: " + module, ref.line, ref.col, List.of());
                    }
                }
                Function f = moduleFuncs.get(ref.name);
                if (f == null) {
                    throw new ScriptRuntimeException("Unknown function in module " + module + ": " + ref.name, ref.line, ref.col, List.of());
                }
                if (funcs.containsKey(ref.alias)) {
                    throw new ScriptRuntimeException("Function already defined: " + ref.alias, ref.line, ref.col, List.of());
                }
                funcs.put(ref.alias, f);
            }
            Env env = new Env(funcs, null);
            env.call("main", List.of(), 1, 1);
        } catch (ScriptRuntimeException e) {
            System.err.println("Script error: " + e.getMessage() + " at " + e.line + ":" + e.col);
            if (!e.stack.isEmpty()) {
                System.err.println("Script stack:");
                for (ScriptFrame f : e.stack) {
                    System.err.println("  at " + f.name + " (" + f.line + ":" + f.col + ")");
                }
            }
            e.printStackTrace();
        }
    }
}
