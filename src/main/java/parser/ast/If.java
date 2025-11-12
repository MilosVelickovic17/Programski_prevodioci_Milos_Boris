package parser.ast;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;

public class If extends Stmt {
    public final Expr condition;
    public final Block thenBranch;
    public final List<ElseIf> elseIfs;
    public final Block elseBranch;

    public static class ElseIf {
        public final Expr condition;
        public final Block body;
        public ElseIf(Expr c, Block b){ this.condition = c; this.body = b; }
    }

    public If(Expr c, Block t, List<ElseIf> e, Block el){ 
        this.condition = c; this.thenBranch = t; this.elseIfs = e; this.elseBranch = el;
    }
    public <R> R accept(Visitor<R> v){ return v.visitIf(this); }
    @Override
    public List<Node> getChildren() {
        List<Node> list = new ArrayList<>();
        list.add(condition);
        list.add(thenBranch);
        if (elseBranch != null) list.add(elseBranch);
        return list;
    }

}
