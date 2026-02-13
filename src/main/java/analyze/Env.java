package analyze;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Env {
    private static final BufferedReader READER = new BufferedReader(new InputStreamReader(System.in));
    Map<String, Object> vars = new HashMap<>();
    Map<String, Function> funcs;
    Env parent;
    java.util.ArrayDeque<ScriptFrame> stack;

    Env(Map<String, Function> funcs, Env parent) {
        this.funcs = funcs;
        this.parent = parent;
        this.stack = parent == null ? new java.util.ArrayDeque<>() : parent.stack;
    }

    Object get(String name) {
        if (vars.containsKey(name)) return vars.get(name);
        if (parent != null) return parent.get(name);
        throw new ScriptRuntimeException("Undefined variable " + name, 0, 0, stackSnapshot());
    }

    boolean has(String name) {
        if (vars.containsKey(name)) return true;
        if (parent != null) return parent.has(name);
        return false;
    }

    void set(String name, Object v) {
        if (vars.containsKey(name)) {
            vars.put(name, v);
            return;
        }
        if (parent != null && parent.has(name)) {
            parent.set(name, v);
            return;
        }
        vars.put(name, v);
    }

    void define(String name, Object v, int line, int col) {
        if (vars.containsKey(name)) {
            throw new ScriptRuntimeException("Variable already defined: " + name, line, col, stackSnapshot());
        }
        vars.put(name, v);
    }

    Object call(String name, List<Expr> args, int line, int col) {
        if (name.equals("println")) {
            if (args.isEmpty()) {
                System.out.println();
                return null;
            }
            System.out.println(args.get(0).eval(this));
            return null;
        }

        if (name.equals("size")) {
            if (args.size() != 1) {
                throw new ScriptRuntimeException("size() expects 1 argument", line, col, stackSnapshot());
            }
            Object v = args.get(0).eval(this);
            if (v instanceof String s) return (double) s.length();
            if (v instanceof java.util.List<?> l) return (double) l.size();
            if (v instanceof java.util.Map<?, ?> m) return (double) m.size();
            throw new ScriptRuntimeException("size() expects string, list, or map", line, col, stackSnapshot());
        }

        if (name.equals("map")) {
            if (args.size() != 2) {
                throw new ScriptRuntimeException("map() expects 2 arguments", line, col, stackSnapshot());
            }
            Object target = args.get(0).eval(this);
            Object fnVal = args.get(1).eval(this);
            String funcName = null;
            if (fnVal instanceof FunctionRef fr) {
                funcName = fr.name;
            } else if (fnVal instanceof String s) {
                funcName = s;
            }
            if (funcName == null) {
                throw new ScriptRuntimeException("map() expects function reference or name", line, col, stackSnapshot());
            }
            if (target instanceof java.util.List<?> list) {
                java.util.List<Object> out = new java.util.ArrayList<>();
                for (Object item : list) {
                    out.add(call(funcName, java.util.List.of(new ConstExpr(item)), line, col));
                }
                return out;
            }
            if (target instanceof String s) {
                StringBuilder out = new StringBuilder();
                for (int i = 0; i < s.length(); i++) {
                    Object r = call(funcName, java.util.List.of(new ConstExpr(String.valueOf(s.charAt(i)))), line, col);
                    out.append(String.valueOf(r));
                }
                return out.toString();
            }
            throw new ScriptRuntimeException("map() expects list or string", line, col, stackSnapshot());
        }

        if (name.equals("readln")) {
            if (!args.isEmpty()) {
                throw new ScriptRuntimeException("readln() expects 0 arguments", line, col, stackSnapshot());
            }
            try {
                String lineIn = READER.readLine();
                return lineIn == null ? "" : lineIn;
            } catch (IOException e) {
                throw new ScriptRuntimeException("readln() failed: " + e.getMessage(), line, col, stackSnapshot());
            }
        }

        if (name.equals("toNumber")) {
            if (args.size() != 1) {
                throw new ScriptRuntimeException("toNumber() expects 1 argument", line, col, stackSnapshot());
            }
            return toNumber(args.get(0).eval(this));
        }

        if (name.equals("toString")) {
            if (args.size() != 1) {
                throw new ScriptRuntimeException("toString() expects 1 argument", line, col, stackSnapshot());
            }
            Object v = args.get(0).eval(this);
            if (v instanceof Number n) {
                int code = n.intValue();
                if (code < 0 || code > Character.MAX_VALUE) {
                    throw new ScriptRuntimeException("toString() ascii code out of range", line, col, stackSnapshot());
                }
                return String.valueOf((char) code);
            }
            return String.valueOf(v);
        }

        if (name.equals("typeOf")) {
            if (args.size() != 1) {
                throw new ScriptRuntimeException("typeOf() expects 1 argument", line, col, stackSnapshot());
            }
            Object v = args.get(0).eval(this);
            if (v == null) return new TypeLiteral("NULL");
            if (v instanceof Number) return new TypeLiteral("NUMBER");
            if (v instanceof String) return new TypeLiteral("STRING");
            if (v instanceof Boolean) return new TypeLiteral("BOOLEAN");
            if (v instanceof java.util.List<?>) return new TypeLiteral("ARRAY");
            if (v instanceof java.util.Map<?, ?>) return new TypeLiteral("STRUCTURE");
            if (v instanceof FunctionRef) return new TypeLiteral("FUNCTION");
            throw new ScriptRuntimeException("typeOf() unknown type: " + v, line, col, stackSnapshot());
        }

        Function f = funcs.get(name);
        if (f == null) {
            throw new ScriptRuntimeException("Undefined function " + name, line, col, stackSnapshot());
        }
        if (args.size() != f.params.size()) {
            throw new ScriptRuntimeException(
                    "Function " + name + " expects " + f.params.size() + " args, got " + args.size(),
                    line,
                    col,
                    stackSnapshot()
            );
        }
        stack.push(new ScriptFrame(name, line, col));
        Env local = new Env(funcs, this);

        for (int i = 0; i < f.params.size(); i++) {
            // Bind parameters locally without overwriting captured variables.
            local.define(f.params.get(i), args.get(i).eval(this), line, col);
        }

        try {
            for (Stmt s : f.body) s.exec(local);
        } catch (ReturnValue rv) {
            return rv.value;
        } finally {
            stack.pop();
        }
        return null;
    }

    static double toNumber(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        if (v instanceof String s) {
            if (s.length() == 1) return (double) s.charAt(0);
            throw new TypeErrorException("Expected number or single-character string, got " + v);
        }
        throw new TypeErrorException("Expected number, got " + v);
    }

    static boolean isTruthy(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean b) return b;
        if (v instanceof Number n) return n.doubleValue() != 0.0;
        if (v instanceof String s) return !s.isEmpty();
        if (v instanceof java.util.List<?> l) return !l.isEmpty();
        if (v instanceof java.util.Map<?, ?> m) return !m.isEmpty();
        return true;
    }

    java.util.List<ScriptFrame> stackSnapshot() {
        return new java.util.ArrayList<>(stack);
    }

    Function getFunction(String name) {
        return funcs.get(name);
    }
}
