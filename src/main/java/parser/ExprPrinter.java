package parser;

import parser.ast.*;

public class ExprPrinter implements Expr.Visitor<String> {

    @Override
    public String visitBinary(Binary e) {
        return "(" + e.left.accept(this) + " " + e.op.lexeme + " " + e.right.accept(this) + ")";
    }

    @Override
    public String visitUnary(Unary e) {
        return e.op.lexeme + e.expr.accept(this);
    }

    @Override
    public String visitLiteral(Literal e) {
        return String.valueOf(e.value);
    }

    @Override
    public String visitGrouping(Grouping e) {
        return "(" + e.expr.accept(this) + ")";
    }

    @Override
    public String visitVariable(Variable e) {
        return e.name.lexeme;
    }

    @Override
    public String visitAssign(Assign e) {
        return e.target.accept(this) + " = " + e.value.accept(this);
    }

    @Override
    public String visitCall(Call e) {
        if (e.args == null || e.args.isEmpty()) {
            return e.callee.accept(this) + "()";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(e.callee.accept(this)).append("(");
        for (int i = 0; i < e.args.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(e.args.get(i).accept(this));
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String visitArrayAccess(ArrayAccess e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.array.accept(this));
        for (Expr idx : e.indices) {
            sb.append("[").append(idx.accept(this)).append("]");
        }
        return sb.toString();
    }
}
