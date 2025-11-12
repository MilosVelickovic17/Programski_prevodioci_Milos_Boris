package parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import parser.ast.*;

import java.util.ArrayList;
import java.util.List;

public class PrettyPrinter {

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public String toJson(Program program) {
        return gson.toJson(program);
    }

    public String printAst(Program program) {
        StringBuilder sb = new StringBuilder();
        sb.append("Program\n");
        List<Stmt> top = program.topLevel;
        for (int i = 0; i < top.size(); i++) {
            printNode(top.get(i), "", sb, i == top.size() - 1);
        }
        return sb.toString();
    }

    private void printNode(Node node, String prefix, StringBuilder sb, boolean isLast) {
        if (node == null) return;

        String branch = isLast ? "└── " : "├── ";
        sb.append(prefix).append(branch);

        // === OPIS CVORA ===
        sb.append(node.getClass().getSimpleName());
        if (node instanceof Function f) {
            sb.append(" name=").append(f.name.lexeme);
            if (f.retType != null && f.retType.type != null)
                sb.append(" retType=").append(f.retType.type.base);
        } else if (node instanceof VarDecl v) {
            sb.append(" type=").append(v.type.base);
        } else if (node instanceof Return r) {
            sb.append(" value=").append(exprToString(r.value));
        } else if (node instanceof Print p) {
            sb.append(" expr=").append(exprToString(p.expr));
        } else if (node instanceof ExprStmt e) {
            sb.append(" expr=").append(exprToString(e.expr));
        } else if (node instanceof For) {
            sb.append(" (for)");
        }

        sb.append("\n");

        // === DECA ===
        List<? extends Node> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            for (int i = 0; i < children.size(); i++) {
                boolean last = (i == children.size() - 1);
                String newPrefix = prefix + (isLast ? "    " : "│   ");
                printNode(children.get(i), newPrefix, sb, last);
            }
        }
    }

    private String exprToString(Expr e) {
        if (e == null) return "null";
        if (e instanceof Literal l) return String.valueOf(l.value);
        if (e instanceof Variable v) return v.name.lexeme;
        if (e instanceof Binary b)
            return "(" + exprToString(b.left) + " " + b.op.lexeme + " " + exprToString(b.right) + ")";
        if (e instanceof Assign a)
            return exprToString(a.target) + " = " + exprToString(a.value);
        if (e instanceof Call c)
            return exprToString(c.callee) + "()";
        if (e instanceof Grouping g)
            return "(" + exprToString(g.expr) + ")";
        return e.getClass().getSimpleName();
    }
}
