package parser.ast;

import java.util.List;

public class ExprStmt extends Stmt {
    public final Expr expr;

    public ExprStmt(Expr expr) {
        this.expr = expr;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> v) {
        return v.visitExprStmt(this);
    }

    @Override
    public List<Node> getChildren() {
        return List.of(expr);
    }
}
