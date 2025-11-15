package parser.ast;

import java.util.ArrayList;
import java.util.List;

public class Block extends Stmt {
    public final List<Stmt> statements;

    public Block(List<Stmt> statements) {
        this.statements = statements;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> v) {
        return v.visitBlock(this);
    }

    @Override
    public List<Node> getChildren() {
        return new ArrayList<>(statements);
    }
}
