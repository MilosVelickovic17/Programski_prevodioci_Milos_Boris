package parser.ast;

import java.util.ArrayList;
import java.util.List;
import lexer.token.Token;
import parser.Types;

public class VarDecl extends Stmt {
    public final Types.Type type;
    public final List<Item> items;

    public static class Item {
        public final Token name;
        public final List<Expr> arrayDims;
        public final Expr init;

        public Item(Token name, List<Expr> dims, Expr init) {
            this.name = name;
            this.arrayDims = dims;
            this.init = init;
        }
    }

    public VarDecl(Types.Type type, List<Item> items) {
        this.type = type;
        this.items = items;
    }

    @Override
    public <R> R accept(Stmt.Visitor<R> v) {
        return v.visitVarDecl(this);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>();
        for (Item item : items) {
            children.addAll(item.arrayDims);
            if (item.init != null) {
                children.add(item.init);
            }
        }
        return children;
    }
}
