package analyze;

public class Statements {
}


interface Stmt {
    void exec(Env env);
}

class VarStmt implements Stmt {
    String name;
    Expr expr;

    VarStmt(String n, Expr e) {
        name = n;
        expr = e;
    }

    public void exec(Env env) {
        env.set(name, expr.eval(env));
    }
}

class ExprStmt implements Stmt {
    Expr expr;

    ExprStmt(Expr e) {
        expr = e;
    }

    public void exec(Env env) {
        expr.eval(env);
    }
}

class ReturnStmt implements Stmt {
    Expr expr;

    ReturnStmt(Expr e) {
        expr = e;
    }

    public void exec(Env env) {
        throw new ReturnValue(expr.eval(env));
    }
}



class ReturnValue extends RuntimeException {
    double value;

    ReturnValue(double v) {
        value = v;
    }
}
