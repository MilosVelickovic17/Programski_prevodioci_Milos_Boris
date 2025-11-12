package parser.ast;

import com.google.gson.annotations.Expose;
import lexer.token.Token;

public class Literal extends Expr {
    public final Object value;
    public final Token token;
    public Literal(Object v, Token t){ this.value=v; this.token=t; }
    public <R> R accept(Visitor<R> v){ return v.visitLiteral(this); }
}
