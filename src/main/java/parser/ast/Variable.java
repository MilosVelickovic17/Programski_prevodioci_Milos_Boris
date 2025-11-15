package parser.ast;

import lexer.token.Token;

public class Variable extends Expr {
    public final Token name;

    public Variable(Token n) {
        this.name = n;
    }

    @Override
    public <R> R accept(Expr.Visitor<R> v) {
        return v.visitVariable(this);
    }
}
