package analyze;

import java.util.List;

public class Function {
    List<String> params;
    List<Stmt> body;

    Function(List<String> p, List<Stmt> b) {
        params = p;
        body = b;
    }
}