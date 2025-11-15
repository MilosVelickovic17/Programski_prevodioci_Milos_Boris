package parser.ast;

import java.util.List;

public class While extends Stmt {
    public final Expr condition;
    public final Block body;

    public While(Expr c, Block b) {
        this.condition = c;
        this.body = b;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> v) {
        return v.visitWhile(this);
    }

    @Override
    public List<Node> getChildren() {
        return List.of(condition, body);
    }
}
