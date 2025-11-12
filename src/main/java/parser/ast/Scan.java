package parser.ast;

import com.google.gson.annotations.Expose;
import lexer.token.Token;

public class Scan extends Stmt {
    public final Token keyword;
    public final Expr target;
    public Scan(Token keyword, Expr target) { this.keyword = keyword; this.target = target; }
    public <R> R accept(Visitor<R> v) { return v.visitScan(this); }
}
