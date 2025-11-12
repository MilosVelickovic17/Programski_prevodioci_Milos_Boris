package parser.ast;

import com.google.gson.annotations.Expose;
import lexer.token.Token;

import java.util.List;

public class Return extends Stmt {
    public final Token keyword;
    public final Expr value;
    public Return(Token k, Expr v){ this.keyword = k; this.value = v; }
    public <R> R accept(Visitor<R> v){ return v.visitReturn(this); }
    @Override
    public List<Node> getChildren() {
        return value != null ? List.of(value) : List.of();
    }

}
