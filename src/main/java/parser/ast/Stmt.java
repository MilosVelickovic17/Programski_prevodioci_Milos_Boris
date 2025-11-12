package parser.ast;

public abstract class Stmt extends Node {
    public interface Visitor<R> {
        R visitBlock(Block s);
        R visitExprStmt(ExprStmt s);
        R visitPrint(Print s);
        R visitScan(Scan s);
        R visitVarDecl(VarDecl s);
        R visitIf(If s);
        R visitWhile(While s);
        R visitFor(For s);
        R visitReturn(Return s);
        R visitFunction(Function s);
    }
    public abstract <R> R accept(Visitor<R> visitor);
}
