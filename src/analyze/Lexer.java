package analyze;

public class Lexer {
    private final String input;
    private int pos = 0;

    Lexer(String input) {
        this.input = input;
    }

    Token next() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos)))
            pos++;

        if (pos >= input.length())
            return new Token(TokenType.EOF, "");

        char c = input.charAt(pos);

        if (Character.isDigit(c)) {
            int start = pos;
            while (pos < input.length() &&
                    (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.'))
                pos++;
            return new Token(TokenType.NUMBER, input.substring(start, pos));
        }

        if (Character.isLetter(c)) {
            int start = pos;
            while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos)))
                pos++;
            String t = input.substring(start, pos);
            return switch (t) {
                case "fun" -> new Token(TokenType.FUN, t);
                case "var" -> new Token(TokenType.VAR, t);
                case "return" -> new Token(TokenType.RETURN, t);
                default -> new Token(TokenType.IDENT, t);
            };
        }

        pos++;
        return switch (c) {
            case '+' -> new Token(TokenType.PLUS, "+");
            case '-' -> new Token(TokenType.MINUS, "-");
            case '*' -> new Token(TokenType.MUL, "*");
            case '/' -> new Token(TokenType.DIV, "/");
            case '=' -> new Token(TokenType.ASSIGN, "=");
            case ',' -> new Token(TokenType.COMMA, ",");
            case '(' -> new Token(TokenType.LPAREN, "(");
            case ')' -> new Token(TokenType.RPAREN, ")");
            case '{' -> new Token(TokenType.LBRACE, "{");
            case '}' -> new Token(TokenType.RBRACE, "}");
            default -> throw new RuntimeException("Unknown char: " + c);
        };
    }
}
