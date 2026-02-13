package analyze;

public class ScriptFrame {
    final String name;
    final int line;
    final int col;

    ScriptFrame(String name, int line, int col) {
        this.name = name;
        this.line = line;
        this.col = col;
    }
}
