package analyze;

import java.util.List;

public class ScriptRuntimeException extends RuntimeException {
    final int line;
    final int col;
    final List<ScriptFrame> stack;

    ScriptRuntimeException(String message, int line, int col, List<ScriptFrame> stack) {
        super(message);
        this.line = line;
        this.col = col;
        this.stack = stack;
    }
}
