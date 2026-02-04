package analyze;

import java.util.List;
import java.util.Map;

public class ExpressionAnalyzer {

    public static void main(String[] args) {
        String src = """
                fun main() {
                    var a = 3
                    var b = 4
                    var r = sum(a, b) + a / b
                    println(r)
                    var r2 = minus(a, b)
                    println(r2)
                }
                
                fun sum(a, b) {
                
                    var c = 56
                    
                    var d = minus(a, b)
                    
                    println(c)
                    println(d)
                    
                    return c + d
                }
                
                
                fun minus(a, b) {
                    return a - b
                }
                """;

        Parser p = new Parser(new Lexer(src));
        Map<String, Function> funcs = p.parseProgram();
        Env env = new Env(funcs, null);
        env.call("main", List.of());
    }
}

