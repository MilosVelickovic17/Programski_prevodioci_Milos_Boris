package parser.ast;

import java.util.List;
import lexer.token.Token;

public class Print extends Stmt {
    public final Token keyword;
    public final Expr expr;

    public Print(Token keyword, Expr expr) {
        this.keyword = keyword;
        this.expr = expr;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> v) {
        return v.visitPrint(this);
    }

    @Override
    public List<Node> getChildren() {
        return List.of(expr);
    }
}
