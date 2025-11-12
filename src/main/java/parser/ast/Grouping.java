package parser.ast;

import com.google.gson.annotations.Expose;

public class Grouping extends Expr {
    public final Expr expr;
    public Grouping(Expr e){ this.expr=e; }
    public <R> R accept(Visitor<R> v){ return v.visitGrouping(this); }
}
