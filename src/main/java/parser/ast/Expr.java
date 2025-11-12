package parser.ast;

public abstract class Expr extends Node {
    public interface Visitor<R> {
        R visitBinary(Binary e);
        R visitUnary(Unary e);
        R visitLiteral(Literal e);
        R visitGrouping(Grouping e);
        R visitVariable(Variable e);
        R visitAssign(Assign e);
        R visitCall(Call e);
        R visitArrayAccess(ArrayAccess e);
    }
    public abstract <R> R accept(Visitor<R> visitor);
}
