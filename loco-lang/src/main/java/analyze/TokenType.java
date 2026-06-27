package analyze;

public enum TokenType {
    NUMBER, STRING, IDENT,
    FUN, VAR, RETURN, IF, ELSE, FOR, IMPORT, AS, TRUE, FALSE, NULL,
    TYPE,
    PLUS, MINUS, MUL, DIV, MOD,
    ASSIGN, COMMA, SEMI, COLON,
    GT, LT, GE, LE, EQ, NE,
    AND, OR, NOT,
    DOT,
    LPAREN, RPAREN,
    LBRACKET, RBRACKET,
    LBRACE, RBRACE,
    EOF
}
