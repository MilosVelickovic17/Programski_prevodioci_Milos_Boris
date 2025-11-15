package parser.ast;

import java.util.List;
import lexer.token.Token;

public class Return extends Stmt {
    public final Token keyword;
    public final Expr value;

    public Return(Token k, Expr v) {
        this.keyword = k;
        this.value = v;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> v) {
        return v.visitReturn(this);
    }

    @Override
    public List<Node> getChildren() {
        return value != null ? List.of(value) : List.of();
    }
}
