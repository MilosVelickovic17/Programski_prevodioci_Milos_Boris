package parser.ast;

public class Grouping extends Expr {
    public final Expr expr;

    public Grouping(Expr e) {
        this.expr = e;
    }

    @Override
    public <R> R accept(Expr.Visitor<R> v) {
        return v.visitGrouping(this);
    }
}
