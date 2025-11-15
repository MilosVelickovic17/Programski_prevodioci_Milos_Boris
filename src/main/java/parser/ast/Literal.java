package parser.ast;

import lexer.token.Token;

public class Literal extends Expr {
    public final Object value;
    public final Token token;

    public Literal(Object v, Token t) {
        this.value = v;
        this.token = t;
    }

    @Override
    public <R> R accept(Expr.Visitor<R> v) {
        return v.visitLiteral(this);
    }
}
