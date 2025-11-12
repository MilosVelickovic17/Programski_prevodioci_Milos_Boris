package parser.ast;

import com.google.gson.annotations.Expose;
import lexer.token.Token;

public class Unary extends Expr {
    public final Token op;
    public final Expr expr;
    public Unary(Token o, Expr e){ this.op=o; this.expr=e; }
    public <R> R accept(Visitor<R> v){ return v.visitUnary(this); }
}
