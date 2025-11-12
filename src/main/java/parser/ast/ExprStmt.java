package parser.ast;

import com.google.gson.annotations.Expose;

public class ExprStmt extends Stmt {
    public final Expr expr;
    public ExprStmt(Expr expr) { this.expr = expr; }
    public <R> R accept(Visitor<R> v) { return v.visitExprStmt(this); }
}
