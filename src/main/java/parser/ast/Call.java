package parser.ast;

import java.util.List;
import lexer.token.Token;

public class Call extends Expr {
    public final Expr callee;
    public final Token paren;
    public final List<Expr> args;

    public Call(Expr c, Token p, List<Expr> a) {
        this.callee = c;
        this.paren = p;
        this.args = a;
    }

    @Override
    public <R> R accept(Expr.Visitor<R> v) {
        return v.visitCall(this);
    }
}
