package parser.ast;

import com.google.gson.annotations.Expose;
import lexer.token.Token;

public class Binary extends Expr {
    public final Expr left;
    public final Token op;
    public final Expr right;
    public Binary(Expr l, Token o, Expr r){ this.left=l; this.op=o; this.right=r; }
    public <R> R accept(Visitor<R> v){ return v.visitBinary(this); }
}
