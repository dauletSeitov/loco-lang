package analyze;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Env {
    Map<String, Double> vars = new HashMap<>();
    Map<String, Function> funcs;
    Env parent;

    Env(Map<String, Function> funcs, Env parent) {
        this.funcs = funcs;
        this.parent = parent;
    }

    double get(String name) {
        if (vars.containsKey(name)) return vars.get(name);
        if (parent != null) return parent.get(name);
        throw new RuntimeException("Undefined variable " + name);
    }

    void set(String name, double v) {
        vars.put(name, v);
    }

    double call(String name, List<Expr> args) {
        if (name.equals("println")) {
            System.out.println(args.get(0).eval(this));
            return 0;
        }

        Function f = funcs.get(name);
        Env local = new Env(funcs, this);

        for (int i = 0; i < f.params.size(); i++)
            local.set(f.params.get(i), args.get(i).eval(this));

        try {
            for (Stmt s : f.body) s.exec(local);
        } catch (ReturnValue rv) {
            return rv.value;
        }
        return 0;
    }
}

