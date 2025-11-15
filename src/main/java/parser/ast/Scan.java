package parser.ast;

import java.util.List;
import lexer.token.Token;

public class Scan extends Stmt {
    public final Token keyword;
    public final Expr target;

    public Scan(Token keyword, Expr target) {
        this.keyword = keyword;
        this.target = target;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> v) {
        return v.visitScan(this);
    }

    @Override
    public List<Node> getChildren() {
        return List.of(target);
    }
}
