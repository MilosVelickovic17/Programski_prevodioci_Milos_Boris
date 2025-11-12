package parser.ast;

import com.google.gson.annotations.Expose;

public class While extends Stmt {
    public final Expr condition;
    public final Block body;
    public While(Expr c, Block b){ this.condition = c; this.body = b; }
    public <R> R accept(Visitor<R> v){ return v.visitWhile(this); }
}
