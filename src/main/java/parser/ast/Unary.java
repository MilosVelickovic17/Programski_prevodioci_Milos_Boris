package parser.ast;

import lexer.token.Token;

public class Unary extends Expr {
    public final Token op;
    public final Expr expr;

    public Unary(Token o, Expr e) {
        this.op = o;
        this.expr = e;
    }

    @Override
    public <R> R accept(Expr.Visitor<R> v) {
        return v.visitUnary(this);
    }
}
