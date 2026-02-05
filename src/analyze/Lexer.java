package analyze;

public class Lexer {
    private final String input;
    private int pos = 0;
    private int line = 1;
    private int col = 1;

    Lexer(String input) {
        this.input = input;
    }

    Token next() {
        while (true) {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
                advance();
            }
            if (pos + 1 < input.length()
                    && input.charAt(pos) == '/'
                    && input.charAt(pos + 1) == '/') {
                while (pos < input.length() && input.charAt(pos) != '\n') {
                    advance();
                }
                continue;
            }
            break;
        }

        if (pos >= input.length())
            return new Token(TokenType.EOF, "", line, col);

        char c = input.charAt(pos);
        int startLine = line;
        int startCol = col;

        if (Character.isDigit(c)) {
            int start = pos;
            while (pos < input.length() &&
                    (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.'))
                advance();
            return new Token(TokenType.NUMBER, input.substring(start, pos), startLine, startCol);
        }

        if (c == '"') {
            advance();
            int start = pos;
            while (pos < input.length() && input.charAt(pos) != '"')
                advance();
            if (pos >= input.length())
                throw new ScriptRuntimeException("Unterminated string literal", startLine, startCol, java.util.List.of());
            String text = input.substring(start, pos);
            advance();
            return new Token(TokenType.STRING, text, startLine, startCol);
        }

        if (Character.isLetter(c)) {
            int start = pos;
            while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos)))
                advance();
            String t = input.substring(start, pos);
            return switch (t) {
                case "fun" -> new Token(TokenType.FUN, t, startLine, startCol);
                case "var" -> new Token(TokenType.VAR, t, startLine, startCol);
                case "return" -> new Token(TokenType.RETURN, t, startLine, startCol);
                case "if" -> new Token(TokenType.IF, t, startLine, startCol);
                case "else" -> new Token(TokenType.ELSE, t, startLine, startCol);
                case "for" -> new Token(TokenType.FOR, t, startLine, startCol);
                case "import" -> new Token(TokenType.IMPORT, t, startLine, startCol);
                case "as" -> new Token(TokenType.AS, t, startLine, startCol);
                case "true" -> new Token(TokenType.TRUE, t, startLine, startCol);
                case "false" -> new Token(TokenType.FALSE, t, startLine, startCol);
                case "null" -> new Token(TokenType.NULL, t, startLine, startCol);
                case "NULL", "NUMBER", "STRING", "BOOLEAN", "ARRAY", "STRUCTURE", "FUNCTION" ->
                        new Token(TokenType.TYPE, t, startLine, startCol);
                default -> new Token(TokenType.IDENT, t, startLine, startCol);
            };
        }

        advance();
        return switch (c) {
            case '+' -> new Token(TokenType.PLUS, "+", startLine, startCol);
            case '-' -> new Token(TokenType.MINUS, "-", startLine, startCol);
            case '*' -> new Token(TokenType.MUL, "*", startLine, startCol);
            case '/' -> new Token(TokenType.DIV, "/", startLine, startCol);
            case '%' -> new Token(TokenType.MOD, "%", startLine, startCol);
            case '=' -> {
                if (pos < input.length() && input.charAt(pos) == '=') {
                    advance();
                    yield new Token(TokenType.EQ, "==", startLine, startCol);
                }
                yield new Token(TokenType.ASSIGN, "=", startLine, startCol);
            }
            case '!' -> {
                if (pos < input.length() && input.charAt(pos) == '=') {
                    advance();
                    yield new Token(TokenType.NE, "!=", startLine, startCol);
                }
                yield new Token(TokenType.NOT, "!", startLine, startCol);
            }
            case '&' -> new Token(TokenType.AND, "&", startLine, startCol);
            case '|' -> new Token(TokenType.OR, "|", startLine, startCol);
            case '>' -> {
                if (pos < input.length() && input.charAt(pos) == '=') {
                    advance();
                    yield new Token(TokenType.GE, ">=", startLine, startCol);
                }
                yield new Token(TokenType.GT, ">", startLine, startCol);
            }
            case '<' -> {
                if (pos < input.length() && input.charAt(pos) == '=') {
                    advance();
                    yield new Token(TokenType.LE, "<=", startLine, startCol);
                }
                yield new Token(TokenType.LT, "<", startLine, startCol);
            }
            case ',' -> new Token(TokenType.COMMA, ",", startLine, startCol);
            case ';' -> new Token(TokenType.SEMI, ";", startLine, startCol);
            case ':' -> new Token(TokenType.COLON, ":", startLine, startCol);
            case '.' -> new Token(TokenType.DOT, ".", startLine, startCol);
            case '(' -> new Token(TokenType.LPAREN, "(", startLine, startCol);
            case ')' -> new Token(TokenType.RPAREN, ")", startLine, startCol);
            case '[' -> new Token(TokenType.LBRACKET, "[", startLine, startCol);
            case ']' -> new Token(TokenType.RBRACKET, "]", startLine, startCol);
            case '{' -> new Token(TokenType.LBRACE, "{", startLine, startCol);
            case '}' -> new Token(TokenType.RBRACE, "}", startLine, startCol);
            default -> throw new ScriptRuntimeException("Unknown char: " + c, startLine, startCol, java.util.List.of());
        };
    }

    private void advance() {
        if (pos >= input.length()) return;
        char c = input.charAt(pos);
        pos++;
        if (c == '\n') {
            line++;
            col = 1;
        } else {
            col++;
        }
    }
}
