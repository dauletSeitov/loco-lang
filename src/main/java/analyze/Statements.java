package analyze;

public class Statements {
}


interface Stmt {
    void exec(Env env);
}

class VarStmt implements Stmt {
    String name;
    Expr expr;
    int line;
    int col;

    VarStmt(String n, Expr e, int line, int col) {
        name = n;
        expr = e;
        this.line = line;
        this.col = col;
    }

    public void exec(Env env) {
        env.define(name, expr.eval(env), line, col);
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

class IfStmt implements Stmt {
    Expr cond;
    java.util.List<Stmt> thenBranch;
    java.util.List<Stmt> elseBranch;

    IfStmt(Expr cond, java.util.List<Stmt> thenBranch, java.util.List<Stmt> elseBranch) {
        this.cond = cond;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public void exec(Env env) {
        if (Env.isTruthy(cond.eval(env))) {
            Env blockEnv = new Env(env.funcs, env);
            for (Stmt s : thenBranch) s.exec(blockEnv);
            return;
        }
        if (elseBranch != null) {
            Env blockEnv = new Env(env.funcs, env);
            for (Stmt s : elseBranch) s.exec(blockEnv);
        }
    }
}

class ForStmt implements Stmt {
    Stmt init;
    Expr cond;
    Expr incr;
    java.util.List<Stmt> body;

    ForStmt(Stmt init, Expr cond, Expr incr, java.util.List<Stmt> body) {
        this.init = init;
        this.cond = cond;
        this.incr = incr;
        this.body = body;
    }

    public void exec(Env env) {
        Env loopEnv = new Env(env.funcs, env);
        if (init != null) {
            if (init instanceof ExprStmt es && es.expr instanceof AssignExpr ae) {
                if (!loopEnv.vars.containsKey(ae.name)) {
                    Object v = ae.value.eval(loopEnv);
                    loopEnv.define(ae.name, v, ae.line, ae.col);
                } else {
                    init.exec(loopEnv);
                }
            } else {
                init.exec(loopEnv);
            }
        }
        while (cond == null || Env.isTruthy(cond.eval(loopEnv))) {
            Env bodyEnv = new Env(loopEnv.funcs, loopEnv);
            for (Stmt s : body) s.exec(bodyEnv);
            if (incr != null) incr.eval(loopEnv);
        }
    }
}



class ReturnValue extends ScriptRuntimeException {
    Object value;

    ReturnValue(Object v) {
        super("return", 0, 0, java.util.List.of());
        value = v;
    }
}
