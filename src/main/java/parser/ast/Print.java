package parser.ast;

import com.google.gson.annotations.Expose;
import lexer.token.Token;

import java.util.List;

public class Print extends Stmt {
    public final Token keyword;
    public final Expr expr;
    public Print(Token keyword, Expr expr) { this.keyword = keyword; this.expr = expr; }
    public <R> R accept(Visitor<R> v) { return v.visitPrint(this); }
    @Override
    public List<Node> getChildren() {
        return List.of(expr);
    }

}
