package analyze;

public class Token {
    TokenType type;
    String text;

    Token(TokenType type, String text) {
        this.type = type;
        this.text = text;
    }
}