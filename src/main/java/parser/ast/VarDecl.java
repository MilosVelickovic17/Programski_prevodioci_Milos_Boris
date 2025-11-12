package parser.ast;

import java.util.List;
import com.google.gson.annotations.Expose;
import lexer.token.Token;
import parser.Types;

public class VarDecl extends Stmt {
    public final Types.Type type;
    public final List<Item> items;

    public static class Item {
        public final Token name;
        public final List<Expr> arrayDims;
        public final Expr init;
        public Item(Token name, List<Expr> dims, Expr init){
            this.name = name; this.arrayDims = dims; this.init = init;
        }
    }

    public VarDecl(Types.Type type, List<Item> items){ this.type = type; this.items = items; }
    public <R> R accept(Visitor<R> v){ return v.visitVarDecl(this); }
}
