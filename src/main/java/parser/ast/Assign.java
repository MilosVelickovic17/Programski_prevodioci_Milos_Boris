package parser.ast;

import lexer.token.Token;

public class Assign extends Expr {
    public final Expr target;
    public final Token eq;
    public final Expr value;

    public Assign(Expr t, Token e, Expr v) {
        this.target = t;
        this.eq = e;
        this.value = v;
    }

    @Override
    public <R> R accept(Expr.Visitor<R> v) {
        return v.visitAssign(this);
    }
}
