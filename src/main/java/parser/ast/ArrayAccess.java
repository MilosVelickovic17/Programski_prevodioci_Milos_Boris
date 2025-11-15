package parser.ast;

import java.util.List;

public class ArrayAccess extends Expr {
    public final Expr array;
    public final List<Expr> indices;

    public ArrayAccess(Expr a, List<Expr> i) {
        this.array = a;
        this.indices = i;
    }

    @Override
    public <R> R accept(Expr.Visitor<R> v) {
        return v.visitArrayAccess(this);
    }
}
