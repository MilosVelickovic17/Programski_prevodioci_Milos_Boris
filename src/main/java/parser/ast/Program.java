package parser.ast;

import java.util.ArrayList;
import java.util.List;

public class Program extends Node {
    public final List<Stmt> topLevel;

    public Program(List<Stmt> topLevel) {
        this.topLevel = topLevel;
    }

    @Override
    public List<Node> getChildren() {
        return new ArrayList<>(topLevel);
    }
}
