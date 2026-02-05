package analyze;

public class Token {
    TokenType type;
    String text;
    int line;
    int col;

    Token(TokenType type, String text, int line, int col) {
        this.type = type;
        this.text = text;
        this.line = line;
        this.col = col;
    }
}
