package parser.ast;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;

public class Block extends Stmt {
    public final List<Stmt> statements;
    public Block(List<Stmt> statements) { this.statements = statements; }
    public <R> R accept(Visitor<R> v) { return v.visitBlock(this); }
    @Override
    public List<Node> getChildren() {
        return new ArrayList<>(statements);
    }

}
