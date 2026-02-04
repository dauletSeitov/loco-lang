package analyze;

import java.util.List;

public class AST {
}

interface Expr {
    double eval(Env env);
}

class NumExpr implements Expr {
    double v;

    NumExpr(double v) {
        this.v = v;
    }

    public double eval(Env env) {
        return v;
    }
}

class VarExpr implements Expr {
    String name;

    VarExpr(String n) {
        name = n;
    }

    public double eval(Env env) {
        return env.get(name);
    }
}

class BinExpr implements Expr {
    Expr l, r;
    TokenType op;

    BinExpr(Expr l, TokenType op, Expr r) {
        this.l = l;
        this.op = op;
        this.r = r;
    }

    public double eval(Env env) {
        return switch (op) {
            case PLUS -> l.eval(env) + r.eval(env);
            case MINUS -> l.eval(env) - r.eval(env);
            case MUL -> l.eval(env) * r.eval(env);
            case DIV -> l.eval(env) / r.eval(env);
            default -> 0;
        };
    }
}

class CallExpr implements Expr {
    String name;
    List<Expr> args;

    CallExpr(String n, List<Expr> a) {
        name = n;
        args = a;
    }

    public double eval(Env env) {
        return env.call(name, args);
    }
}
