package analyze;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class AST {
}

interface Expr {
    Object eval(Env env);
}

class NumExpr implements Expr {
    double v;
    int line;
    int col;

    NumExpr(double v, int line, int col) {
        this.v = v;
        this.line = line;
        this.col = col;
    }

    public Object eval(Env env) {
        return v;
    }
}

class StrExpr implements Expr {
    String v;
    int line;
    int col;

    StrExpr(String v, int line, int col) {
        this.v = v;
        this.line = line;
        this.col = col;
    }

    public Object eval(Env env) {
        return v;
    }
}

class BoolExpr implements Expr {
    boolean v;
    int line;
    int col;

    BoolExpr(boolean v, int line, int col) {
        this.v = v;
        this.line = line;
        this.col = col;
    }

    public Object eval(Env env) {
        return v;
    }
}

class ConstExpr implements Expr {
    Object v;

    ConstExpr(Object v) {
        this.v = v;
    }

    public Object eval(Env env) {
        return v;
    }
}

class TypeLiteral {
    final String name;

    TypeLiteral(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeLiteral that)) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}

class FunctionRef {
    final String name;

    FunctionRef(String name) {
        this.name = name;
    }
}

class ListExpr implements Expr {
    List<Expr> items;
    int line;
    int col;

    ListExpr(List<Expr> items, int line, int col) {
        this.items = items;
        this.line = line;
        this.col = col;
    }

    public Object eval(Env env) {
        List<Object> out = new ArrayList<>();
        for (Expr e : items) {
            out.add(e.eval(env));
        }
        return out;
    }
}

class DictKey {
    final String name;
    final boolean literal;

    DictKey(String name, boolean literal) {
        this.name = name;
        this.literal = literal;
    }

    String resolve(Env env, int line, int col) {
        if (literal) return name;
        if (env.has(name)) {
            Object v = env.get(name);
            if (v instanceof String s) return s;
            throw new ScriptRuntimeException("Dictionary key must be string", line, col, env.stackSnapshot());
        }
        return name;
    }
}

class DictExpr implements Expr {
    List<DictKey> keys;
    List<Expr> values;
    int line;
    int col;

    DictExpr(List<DictKey> keys, List<Expr> values, int line, int col) {
        this.keys = keys;
        this.values = values;
        this.line = line;
        this.col = col;
    }

    public Object eval(Env env) {
        Map<String, Object> out = new ScriptMap();
        for (int i = 0; i < keys.size(); i++) {
            String k = keys.get(i).resolve(env, line, col);
            out.put(k, values.get(i).eval(env));
        }
        return out;
    }
}

class ScriptMap extends LinkedHashMap<String, Object> {
    @Override
    public String toString() {
        if (isEmpty()) return "<>";
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        boolean first = true;
        for (Map.Entry<String, Object> e : entrySet()) {
            if (!first) sb.append(", ");
            first = false;
            sb.append(e.getKey()).append(": ").append(String.valueOf(e.getValue()));
        }
        sb.append(">");
        return sb.toString();
    }
}

class IndexExpr implements Expr {
    Expr target;
    Expr index;
    int line;
    int col;

    IndexExpr(Expr target, Expr index, int line, int col) {
        this.target = target;
        this.index = index;
        this.line = line;
        this.col = col;
    }

    public Object eval(Env env) {
        try {
            Object t = target.eval(env);
            if (t instanceof List<?> list) {
                int i = (int) Env.toNumber(index.eval(env));
                if (i < 0 || i >= list.size()) {
                    throw new ScriptRuntimeException("Index out of bounds: " + i, line, col, env.stackSnapshot());
                }
                return list.get(i);
            }
            if (t instanceof String s) {
                int i = (int) Env.toNumber(index.eval(env));
                if (i < 0 || i >= s.length()) {
                    throw new ScriptRuntimeException("Index out of bounds: " + i, line, col, env.stackSnapshot());
                }
                return String.valueOf(s.charAt(i));
            }
            if (t instanceof Map<?, ?> map) {
                Object k = index.eval(env);
                if (!(k instanceof String ks)) {
                    throw new ScriptRuntimeException("Indexing expects string key for map", line, col, env.stackSnapshot());
                }
                if (!map.containsKey(ks)) {
                    return null;
                }
                return map.get(ks);
            }
            throw new ScriptRuntimeException("Indexing expects list, string, or map", line, col, env.stackSnapshot());
        } catch (TypeErrorException e) {
            throw new ScriptRuntimeException(e.getMessage(), line, col, env.stackSnapshot());
        }
    }
}

class SetIndexExpr implements Expr {
    Expr target;
    Expr index;
    Expr value;
    int line;
    int col;

    SetIndexExpr(Expr target, Expr index, Expr value, int line, int col) {
        this.target = target;
        this.index = index;
        this.value = value;
        this.line = line;
        this.col = col;
    }

    public Object eval(Env env) {
        try {
            Object t = target.eval(env);
            Object v = value.eval(env);
            if (t instanceof List<?> list) {
                int i = (int) Env.toNumber(index.eval(env));
                if (i < 0 || i >= list.size()) {
                    throw new ScriptRuntimeException("Index out of bounds: " + i, line, col, env.stackSnapshot());
                }
                @SuppressWarnings("unchecked")
                List<Object> mutable = (List<Object>) list;
                mutable.set(i, v);
                return v;
            }
            if (t instanceof Map<?, ?> map) {
                Object k = index.eval(env);
                if (!(k instanceof String ks)) {
                    throw new ScriptRuntimeException("Index assignment expects string key for map", line, col, env.stackSnapshot());
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> mutable = (Map<String, Object>) map;
                mutable.put(ks, v);
                return v;
            }
            throw new ScriptRuntimeException("Index assignment expects list or map", line, col, env.stackSnapshot());
        } catch (TypeErrorException e) {
            throw new ScriptRuntimeException(e.getMessage(), line, col, env.stackSnapshot());
        }
    }
}

class GetFieldExpr implements Expr {
    Expr target;
    String name;
    int line;
    int col;

    GetFieldExpr(Expr target, String name, int line, int col) {
        this.target = target;
        this.name = name;
        this.line = line;
        this.col = col;
    }

    public Object eval(Env env) {
        Object t = target.eval(env);
        if (t instanceof Map<?, ?> map) {
            if (!map.containsKey(name)) {
                throw new ScriptRuntimeException("Missing key: " + name, line, col, env.stackSnapshot());
            }
            return map.get(name);
        }
        throw new ScriptRuntimeException("Field access expects map", line, col, env.stackSnapshot());
    }
}

class MethodCallExpr implements Expr {
    Expr target;
    String name;
    List<Expr> args;
    int line;
    int col;

    MethodCallExpr(Expr target, String name, List<Expr> args, int line, int col) {
        this.target = target;
        this.name = name;
        this.args = args;
        this.line = line;
        this.col = col;
    }

    public Object eval(Env env) {
        target.eval(env);
        throw new ScriptRuntimeException("Unknown method: " + name, line, col, env.stackSnapshot());
    }
}

class AssignExpr implements Expr {
    String name;
    Expr value;
    int line;
    int col;

    AssignExpr(String name, Expr value, int line, int col) {
        this.name = name;
        this.value = value;
        this.line = line;
        this.col = col;
    }

    public Object eval(Env env) {
        Object v = value.eval(env);
        env.set(name, v);
        return v;
    }
}

class VarExpr implements Expr {
    String name;
    int line;
    int col;

    VarExpr(String n, int line, int col) {
        name = n;
        this.line = line;
        this.col = col;
    }

    public Object eval(Env env) {
        if (env.has(name)) return env.get(name);
        Function f = env.getFunction(name);
        if (f != null) return new FunctionRef(name);
        throw new ScriptRuntimeException("Undefined variable " + name, line, col, env.stackSnapshot());
    }
}

class BinExpr implements Expr {
    Expr l, r;
    TokenType op;
    int line;
    int col;

    BinExpr(Expr l, TokenType op, Expr r, int line, int col) {
        this.l = l;
        this.op = op;
        this.r = r;
        this.line = line;
        this.col = col;
    }

    public Object eval(Env env) {
        try {
            Object lv = l.eval(env);
            Object rv = r.eval(env);
            return switch (op) {
                case PLUS -> {
                    if (lv instanceof java.util.List<?> && rv instanceof java.util.List<?>) {
                        List<Object> out = new ArrayList<>();
                        out.addAll((List<?>) lv);
                        out.addAll((List<?>) rv);
                        yield out;
                    }
                    if (lv instanceof String || rv instanceof String) {
                        yield String.valueOf(lv) + String.valueOf(rv);
                    }
                    yield Env.toNumber(lv) + Env.toNumber(rv);
                }
                case MINUS -> Env.toNumber(lv) - Env.toNumber(rv);
                case MUL -> Env.toNumber(lv) * Env.toNumber(rv);
                case DIV -> Env.toNumber(lv) / Env.toNumber(rv);
                case MOD -> Env.toNumber(lv) % Env.toNumber(rv);
            case EQ -> java.util.Objects.equals(lv, rv);
            case NE -> !java.util.Objects.equals(lv, rv);
            case GT -> compare(lv, rv, env) > 0;
            case GE -> compare(lv, rv, env) >= 0;
            case LT -> compare(lv, rv, env) < 0;
            case LE -> compare(lv, rv, env) <= 0;
            case AND -> Env.isTruthy(lv) && Env.isTruthy(rv);
            case OR -> Env.isTruthy(lv) || Env.isTruthy(rv);
                default -> 0.0;
            };
        } catch (TypeErrorException e) {
            throw new ScriptRuntimeException(e.getMessage(), line, col, env.stackSnapshot());
        }
    }

    private int compare(Object lv, Object rv, Env env) {
        if (lv instanceof String ls && rv instanceof String rs) {
            return ls.compareTo(rs);
        }
        double l = Env.toNumber(lv);
        double r = Env.toNumber(rv);
        return Double.compare(l, r);
    }
}

class UnaryExpr implements Expr {
    TokenType op;
    Expr expr;
    int line;
    int col;

    UnaryExpr(TokenType op, Expr expr, int line, int col) {
        this.op = op;
        this.expr = expr;
        this.line = line;
        this.col = col;
    }

    public Object eval(Env env) {
        try {
            Object v = expr.eval(env);
            return switch (op) {
                case MINUS -> -Env.toNumber(v);
                case NOT -> !Env.isTruthy(v);
                default -> v;
            };
        } catch (TypeErrorException e) {
            throw new ScriptRuntimeException(e.getMessage(), line, col, env.stackSnapshot());
        }
    }
}

class CallExpr implements Expr {
    String name;
    List<Expr> args;
    int line;
    int col;

    CallExpr(String n, List<Expr> a, int line, int col) {
        name = n;
        args = a;
        this.line = line;
        this.col = col;
    }

    public Object eval(Env env) {
        if (env.has(name)) {
            Object v = env.get(name);
            if (v instanceof FunctionRef fr) {
                return env.call(fr.name, args, line, col);
            }
        }
        return env.call(name, args, line, col);
    }
}
