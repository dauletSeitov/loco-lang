package analyze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    Lexer lex;
    Token cur;
    java.util.List<ImportRef> imports = new java.util.ArrayList<>();

    Parser(Lexer l) {
        lex = l;
        cur = lex.next();
    }

    void eat(TokenType t) {
        if (cur.type != t)
            throw new ScriptRuntimeException("Expected " + t + " got " + cur.type, cur.line, cur.col, java.util.List.of());
        cur = lex.next();
    }

    Expr expr() {
        return assign();
    }

    Expr assign() {
        Expr e = or();
        if (cur.type == TokenType.ASSIGN) {
            eat(TokenType.ASSIGN);
            Expr value = assign();
            if (e instanceof VarExpr v) {
                return new AssignExpr(v.name, value, v.line, v.col);
            }
            if (e instanceof IndexExpr idx) {
                return new SetIndexExpr(idx.target, idx.index, value, idx.line, idx.col);
            }
            throw new ScriptRuntimeException("Invalid assignment target", cur.line, cur.col, java.util.List.of());
        }
        return e;
    }

    Expr or() {
        Expr e = and();
        while (cur.type == TokenType.OR) {
            Token opTok = cur;
            TokenType op = cur.type;
            eat(op);
            e = new BinExpr(e, op, and(), opTok.line, opTok.col);
        }
        return e;
    }

    Expr and() {
        Expr e = equality();
        while (cur.type == TokenType.AND) {
            Token opTok = cur;
            TokenType op = cur.type;
            eat(op);
            e = new BinExpr(e, op, equality(), opTok.line, opTok.col);
        }
        return e;
    }

    Expr equality() {
        Expr e = comparison();
        while (cur.type == TokenType.EQ || cur.type == TokenType.NE) {
            Token opTok = cur;
            TokenType op = cur.type;
            eat(op);
            e = new BinExpr(e, op, comparison(), opTok.line, opTok.col);
        }
        return e;
    }

    Expr comparison() {
        Expr e = add();
        while (cur.type == TokenType.GT || cur.type == TokenType.GE
                || cur.type == TokenType.LT || cur.type == TokenType.LE) {
            Token opTok = cur;
            TokenType op = cur.type;
            eat(op);
            e = new BinExpr(e, op, add(), opTok.line, opTok.col);
        }
        return e;
    }

    Expr add() {
        Expr e = term();
        while (cur.type == TokenType.PLUS || cur.type == TokenType.MINUS) {
            Token opTok = cur;
            TokenType op = cur.type;
            eat(op);
            e = new BinExpr(e, op, term(), opTok.line, opTok.col);
        }
        return e;
    }

    Expr term() {
        Expr e = factor();
        while (cur.type == TokenType.MUL || cur.type == TokenType.DIV || cur.type == TokenType.MOD) {
            Token opTok = cur;
            TokenType op = cur.type;
            eat(op);
            e = new BinExpr(e, op, factor(), opTok.line, opTok.col);
        }
        return e;
    }

    Expr factor() {
        if (cur.type == TokenType.NOT) {
            Token tok = cur;
            eat(TokenType.NOT);
            return new UnaryExpr(TokenType.NOT, factor(), tok.line, tok.col);
        }
        if (cur.type == TokenType.MINUS) {
            Token tok = cur;
            eat(TokenType.MINUS);
            return new UnaryExpr(TokenType.MINUS, factor(), tok.line, tok.col);
        }

        if (cur.type == TokenType.NUMBER) {
            Token tok = cur;
            double v = Double.parseDouble(cur.text);
            eat(TokenType.NUMBER);
            return postfix(new NumExpr(v, tok.line, tok.col));
        }

        if (cur.type == TokenType.STRING) {
            Token tok = cur;
            String v = cur.text;
            eat(TokenType.STRING);
            return postfix(new StrExpr(v, tok.line, tok.col));
        }

        if (cur.type == TokenType.TRUE || cur.type == TokenType.FALSE) {
            Token tok = cur;
            boolean v = cur.type == TokenType.TRUE;
            eat(cur.type);
            return postfix(new BoolExpr(v, tok.line, tok.col));
        }
        if (cur.type == TokenType.NULL) {
            Token tok = cur;
            eat(TokenType.NULL);
            return postfix(new ConstExpr(null));
        }
        if (cur.type == TokenType.TYPE) {
            Token tok = cur;
            String name = cur.text;
            eat(TokenType.TYPE);
            return postfix(new ConstExpr(new TypeLiteral(name)));
        }

        if (cur.type == TokenType.LBRACKET) {
            Token tok = cur;
            eat(TokenType.LBRACKET);
            List<Expr> items = new ArrayList<>();
            if (cur.type != TokenType.RBRACKET) {
                items.add(expr());
                while (cur.type == TokenType.COMMA) {
                    eat(TokenType.COMMA);
                    items.add(expr());
                }
            }
            eat(TokenType.RBRACKET);
            return postfix(new ListExpr(items, tok.line, tok.col));
        }

        if (cur.type == TokenType.LT) {
            Token tok = cur;
            eat(TokenType.LT);
            List<DictKey> keys = new ArrayList<>();
            List<Expr> values = new ArrayList<>();
            if (cur.type != TokenType.GT) {
                while (true) {
                    if (cur.type == TokenType.STRING) {
                        String key = cur.text;
                        eat(TokenType.STRING);
                        eat(TokenType.COLON);
                        keys.add(new DictKey(key, true));
                        values.add(expr());
                    } else if (cur.type == TokenType.IDENT) {
                        String key = cur.text;
                        eat(TokenType.IDENT);
                        eat(TokenType.COLON);
                        keys.add(new DictKey(key, false));
                        values.add(expr());
                    } else {
                        throw new ScriptRuntimeException("Expected dictionary key", cur.line, cur.col, java.util.List.of());
                    }
                    if (cur.type == TokenType.COMMA) {
                        eat(TokenType.COMMA);
                        continue;
                    }
                    break;
                }
            }
            eat(TokenType.GT);
            return postfix(new DictExpr(keys, values, tok.line, tok.col));
        }

        if (cur.type == TokenType.IDENT) {
            Token tok = cur;
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
                return postfix(new CallExpr(name, args, tok.line, tok.col));
            }
            return postfix(new VarExpr(name, tok.line, tok.col));
        }

        if (cur.type == TokenType.LPAREN) {
            eat(TokenType.LPAREN);
            Expr e = expr();
            eat(TokenType.RPAREN);
            return postfix(e);
        }

        throw new ScriptRuntimeException("Bad factor", cur.line, cur.col, java.util.List.of());
    }

    Expr postfix(Expr e) {
        while (true) {
            if (cur.type == TokenType.LBRACKET) {
                Token tok = cur;
                eat(TokenType.LBRACKET);
                Expr idx = expr();
                eat(TokenType.RBRACKET);
                e = new IndexExpr(e, idx, tok.line, tok.col);
                continue;
            }
            if (cur.type == TokenType.DOT) {
                eat(TokenType.DOT);
                Token nameTok = cur;
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
                    e = new MethodCallExpr(e, name, args, nameTok.line, nameTok.col);
                } else {
                    e = new GetFieldExpr(e, name, nameTok.line, nameTok.col);
                }
                continue;
            }
            break;
        }
        return e;
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
        if (cur.type == TokenType.FOR) {
            eat(TokenType.FOR);
            eat(TokenType.LPAREN);
            Stmt init = null;
            if (cur.type != TokenType.SEMI) {
                if (cur.type == TokenType.VAR) {
                    eat(TokenType.VAR);
                    Token nameTok = cur;
                    String name = cur.text;
                    eat(TokenType.IDENT);
                    eat(TokenType.ASSIGN);
                    init = new VarStmt(name, expr(), nameTok.line, nameTok.col);
                } else {
                    init = new ExprStmt(expr());
                }
            }
            eat(TokenType.SEMI);
            Expr cond = null;
            if (cur.type != TokenType.SEMI) {
                cond = expr();
            }
            eat(TokenType.SEMI);
            Expr incr = null;
            if (cur.type != TokenType.RPAREN) {
                incr = expr();
            }
            eat(TokenType.RPAREN);
            return new ForStmt(init, cond, incr, block());
        }

        if (cur.type == TokenType.IF) {
            eat(TokenType.IF);
            eat(TokenType.LPAREN);
            Expr cond = expr();
            eat(TokenType.RPAREN);
            List<Stmt> thenBranch = block();
            List<Stmt> elseBranch = null;
            if (cur.type == TokenType.ELSE) {
                eat(TokenType.ELSE);
                elseBranch = block();
            }
            return new IfStmt(cond, thenBranch, elseBranch);
        }

        if (cur.type == TokenType.VAR) {
            eat(TokenType.VAR);
            Token nameTok = cur;
            String name = cur.text;
            eat(TokenType.IDENT);
            eat(TokenType.ASSIGN);
            return new VarStmt(name, expr(), nameTok.line, nameTok.col);
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
            if (cur.type == TokenType.IMPORT) {
                eat(TokenType.IMPORT);
                Token modTok = cur;
                String module = cur.text;
                eat(TokenType.IDENT);
                if (cur.type != TokenType.DOT) {
                    throw new ScriptRuntimeException("Expected '.' after module in import", cur.line, cur.col, java.util.List.of());
                }
                eat(TokenType.DOT);
                Token nameTok = cur;
                String name = cur.text;
                eat(TokenType.IDENT);
                String alias = name;
                if (cur.type == TokenType.AS) {
                    eat(TokenType.AS);
                    Token aliasTok = cur;
                    alias = cur.text;
                    eat(TokenType.IDENT);
                    if (alias.equals(name)) {
                        throw new ScriptRuntimeException("Alias must be different from original name", aliasTok.line, aliasTok.col, java.util.List.of());
                    }
                }
                for (ImportRef ref : imports) {
                    if (ref.name.equals(name) && module.equals(ref.module) && ref.alias.equals(alias)) {
                        throw new ScriptRuntimeException("Import already defined: " + module + "." + name, nameTok.line, nameTok.col, java.util.List.of());
                    }
                }
                imports.add(new ImportRef(module, name, alias, nameTok.line, nameTok.col));
                continue;
            }
            if (cur.type != TokenType.FUN) {
                throw new ScriptRuntimeException("Expected fun or import", cur.line, cur.col, java.util.List.of());
            }
            eat(TokenType.FUN);
            Token nameTok = cur;
            String name = cur.text;
            eat(TokenType.IDENT);
            if (funcs.containsKey(name)) {
                throw new ScriptRuntimeException("Function already defined: " + name, nameTok.line, nameTok.col, java.util.List.of());
            }
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

    java.util.List<ImportRef> getImports() {
        return imports;
    }

    static class ImportRef {
        String module;
        String name;
        String alias;
        int line;
        int col;

        ImportRef(String module, String name, String alias, int line, int col) {
            this.module = module;
            this.name = name;
            this.alias = alias;
            this.line = line;
            this.col = col;
        }
    }
}
