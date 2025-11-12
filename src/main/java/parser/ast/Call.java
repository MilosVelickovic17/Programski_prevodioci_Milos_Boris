package parser.ast;

import java.util.List;
import com.google.gson.annotations.Expose;
import lexer.token.Token;

public class Call extends Expr {
    public final Expr callee;
    public final Token paren;
    public final List<Expr> args;
    public Call(Expr c, Token p, List<Expr> a){ this.callee=c; this.paren=p; this.args=a; }
    public <R> R accept(Visitor<R> v){ return v.visitCall(this); }
}
