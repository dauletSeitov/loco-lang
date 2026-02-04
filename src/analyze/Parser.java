package analyze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    Lexer lex;
    Token cur;

    Parser(Lexer l) {
        lex = l;
        cur = lex.next();
    }

    void eat(TokenType t) {
        if (cur.type != t)
            throw new RuntimeException("Expected " + t + " got " + cur.type);
        cur = lex.next();
    }

    Expr expr() {
        Expr e = term();
        while (cur.type == TokenType.PLUS || cur.type == TokenType.MINUS) {
            TokenType op = cur.type;
            eat(op);
            e = new BinExpr(e, op, term());
        }
        return e;
    }

    Expr term() {
        Expr e = factor();
        while (cur.type == TokenType.MUL || cur.type == TokenType.DIV) {
            TokenType op = cur.type;
            eat(op);
            e = new BinExpr(e, op, factor());
        }
        return e;
    }

    Expr factor() {
        if (cur.type == TokenType.NUMBER) {
            double v = Double.parseDouble(cur.text);
            eat(TokenType.NUMBER);
            return new NumExpr(v);
        }

        if (cur.type == TokenType.IDENT) {
            String name = cur.text;
            eat(TokenType.IDENT);
            if (cur.type == TokenType.LPAREN) {
                eat(TokenType.LPAREN);
                List<Expr> args = new ArrayList<>();
                if (cur.type != TokenType.RPAREN) {
                    args.add(expr());
                    while (cur.type == TokenType.COMMA) {
                        eat(TokenType.COMMA);
                        args.add(expr());
                    }
                }
                eat(TokenType.RPAREN);
                return new CallExpr(name, args);
            }
            return new VarExpr(name);
        }

        if (cur.type == TokenType.LPAREN) {
            eat(TokenType.LPAREN);
            Expr e = expr();
            eat(TokenType.RPAREN);
            return e;
        }

        throw new RuntimeException("Bad factor");
    }

    List<Stmt> block() {
        eat(TokenType.LBRACE);
        List<Stmt> stmts = new ArrayList<>();
        while (cur.type != TokenType.RBRACE) {
            stmts.add(statement());
        }
        eat(TokenType.RBRACE);
        return stmts;
    }

    Stmt statement() {
        if (cur.type == TokenType.VAR) {
            eat(TokenType.VAR);
            String name = cur.text;
            eat(TokenType.IDENT);
            eat(TokenType.ASSIGN);
            return new VarStmt(name, expr());
        }

        if (cur.type == TokenType.RETURN) {
            eat(TokenType.RETURN);
            return new ReturnStmt(expr());
        }

        return new ExprStmt(expr());
    }

    Map<String, Function> parseProgram() {
        Map<String, Function> funcs = new HashMap<>();

        while (cur.type != TokenType.EOF) {
            eat(TokenType.FUN);
            String name = cur.text;
            eat(TokenType.IDENT);
            eat(TokenType.LPAREN);

            List<String> params = new ArrayList<>();
            if (cur.type != TokenType.RPAREN) {
                params.add(cur.text);
                eat(TokenType.IDENT);
                while (cur.type == TokenType.COMMA) {
                    eat(TokenType.COMMA);
                    params.add(cur.text);
                    eat(TokenType.IDENT);
                }
            }
            eat(TokenType.RPAREN);
            funcs.put(name, new Function(params, block()));
        }
        return funcs;
    }
}