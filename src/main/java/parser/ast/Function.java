package parser.ast;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import lexer.token.Token;
import parser.Types;

public class Function extends Stmt {
    public final Types.ReturnType retType;
    public final Token name;
    public final List<Param> params;
    public final Block body;

    public static class Param {
        public final Types.Type type;
        public final Token name;
        public Param(Types.Type t, Token n){ this.type = t; this.name = n; }
    }

    public Function(Types.ReturnType r, Token n, List<Param> p, Block b){
        this.retType = r; this.name = n; this.params = p; this.body = b;
    }
    public <R> R accept(Visitor<R> v){ return v.visitFunction(this); }
    @Override
    public List<Node> getChildren() {
        List<Node> list = new ArrayList<>();
        list.add(body);
        return list;
    }

}
