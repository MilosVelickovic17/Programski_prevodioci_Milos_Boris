package parser.ast;

import java.util.List;

public class For extends Stmt {
    public final Stmt init;
    public final Expr condition;
    public final Stmt update;
    public final Block body;

    public For(Stmt i, Expr c, Stmt u, Block b) {
        this.init = i;
        this.condition = c;
        this.update = u;
        this.body = b;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> v) {
        return v.visitFor(this);
    }

    @Override
    public List<Node> getChildren() {
        return List.of(init, condition, update, body);
    }
}
