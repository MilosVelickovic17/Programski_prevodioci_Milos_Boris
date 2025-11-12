package parser.ast;

import com.google.gson.annotations.Expose;
import lexer.token.Token;

public class Variable extends Expr {
    public final Token name;
    public Variable(Token n){ this.name=n; }
    public <R> R accept(Visitor<R> v){ return v.visitVariable(this); }
}
