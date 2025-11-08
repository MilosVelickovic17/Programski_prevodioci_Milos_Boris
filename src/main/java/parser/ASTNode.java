package parser;

import lexer.token.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

// ============================================
// BAZNA KLASA
// ============================================

public abstract class ASTNode {
    public abstract List<ASTNode> getChildren();
    public abstract String toString();
}

// ============================================
// PROGRAM I FUNKCIJE
// ============================================

class ProgramNode extends ASTNode {
    private List<ASTNode> elements = new ArrayList<>();

    public void addElement(ASTNode element) {
        elements.add(element);
    }

    @Override
    public List<ASTNode> getChildren() {
        return elements;
    }

    @Override
    public String toString() {
        return "Program";
    }
}

class MainFunctionNode extends ASTNode {
    private Token token;
    private List<ASTNode> statements = new ArrayList<>();
    private ASTNode returnStmt;

    public MainFunctionNode(Token token) {
        this.token = token;
    }

    public void addStatement(ASTNode stmt) {
        statements.add(stmt);
    }

    public void setReturn(ASTNode ret) {
        this.returnStmt = ret;
    }

    @Override
    public List<ASTNode> getChildren() {
        List<ASTNode> children = new ArrayList<>(statements);
        if (returnStmt != null) children.add(returnStmt);
        return children;
    }

    @Override
    public String toString() {
        return "MainFunction";
    }
}

class FunctionNode extends ASTNode {
    private Token type;
    private Token name;
    private List<ParamNode> params;
    private List<ASTNode> statements = new ArrayList<>();
    private ASTNode returnStmt;

    public FunctionNode(Token type, Token name, List<ParamNode> params) {
        this.type = type;
        this.name = name;
        this.params = params;
    }

    public void addStatement(ASTNode stmt) {
        statements.add(stmt);
    }

    public void setReturn(ASTNode ret) {
        this.returnStmt = ret;
    }

    @Override
    public List<ASTNode> getChildren() {
        List<ASTNode> children = new ArrayList<>();
        children.addAll(params);
        children.addAll(statements);
        if (returnStmt != null) children.add(returnStmt);
        return children;
    }

    @Override
    public String toString() {
        return "Function: " + type.lexeme + " " + name.lexeme;
    }
}

class VoidFunctionNode extends ASTNode {
    private Token name;
    private List<ParamNode> params;
    private List<ASTNode> statements = new ArrayList<>();

    public VoidFunctionNode(Token name, List<ParamNode> params) {
        this.name = name;
        this.params = params;
    }

    public void addStatement(ASTNode stmt) {
        statements.add(stmt);
    }

    @Override
    public List<ASTNode> getChildren() {
        List<ASTNode> children = new ArrayList<>();
        children.addAll(params);
        children.addAll(statements);
        return children;
    }

    @Override
    public String toString() {
        return "VoidFunction: " + name.lexeme;
    }
}

class ParamNode extends ASTNode {
    private Token type;
    private Token name;

    public ParamNode(Token type, Token name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public List<ASTNode> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "Param: " + type.lexeme + " " + name.lexeme;
    }
}

class ReturnNode extends ASTNode {
    private Token token;
    private ASTNode expression;

    public ReturnNode(Token token, ASTNode expression) {
        this.token = token;
        this.expression = expression;
    }

    @Override
    public List<ASTNode> getChildren() {
        return Collections.singletonList(expression);
    }

    @Override
    public String toString() {
        return "Return";
    }
}

// ============================================
// DEKLARACIJE
// ============================================

class DeclarationListNode extends ASTNode {
    private Token type;
    private List<DeclItemNode> items = new ArrayList<>();

    public DeclarationListNode(Token type) {
        this.type = type;
    }

    public void addItem(DeclItemNode item) {
        items.add(item);
    }

    @Override
    public List<ASTNode> getChildren() {
        return new ArrayList<>(items);
    }

    @Override
    public String toString() {
        return "Declaration: " + type.lexeme;
    }
}

class DeclItemNode extends ASTNode {
    private Token type;
    private Token name;
    private List<ASTNode> arrayDims;
    private ASTNode initValue;

    public DeclItemNode(Token type, Token name, List<ASTNode> arrayDims, ASTNode initValue) {
        this.type = type;
        this.name = name;
        this.arrayDims = arrayDims;
        this.initValue = initValue;
    }

    @Override
    public List<ASTNode> getChildren() {
        List<ASTNode> children = new ArrayList<>();
        if (arrayDims != null) {
            for (ASTNode dim : arrayDims) {
                if (dim != null) children.add(dim);
            }
        }
        if (initValue != null) children.add(initValue);
        return children;
    }

    @Override
    public String toString() {
        String arr = arrayDims != null ? "[" + arrayDims.size() + "D]" : "";
        String init = initValue != null ? " = ..." : "";
        return "DeclItem: " + name.lexeme + arr + init;
    }
}

// ============================================
// ASSIGNMENT I IDENTIFIKATORI
// ============================================

class AssignmentNode extends ASTNode {
    private ASTNode identifier;
    private ASTNode expression;

    public AssignmentNode(ASTNode identifier, ASTNode expression) {
        this.identifier = identifier;
        this.expression = expression;
    }

    @Override
    public List<ASTNode> getChildren() {
        List<ASTNode> children = new ArrayList<>();
        children.add(identifier);
        children.add(expression);
        return children;
    }

    @Override
    public String toString() {
        return "Assignment (=)";
    }
}

class IdentifierNode extends ASTNode {
    private Token name;
    private List<ASTNode> indices;

    public IdentifierNode(Token name, List<ASTNode> indices) {
        this.name = name;
        this.indices = indices;
    }

    @Override
    public List<ASTNode> getChildren() {
        return indices != null ? indices : Collections.emptyList();
    }

    @Override
    public String toString() {
        String idx = indices != null && !indices.isEmpty() ? "[...]" : "";
        return "Identifier: " + name.lexeme + idx;
    }
}

// ============================================
// IZRAZI
// ============================================

class BinaryOpNode extends ASTNode {
    private ASTNode left;
    private Token operator;
    private ASTNode right;

    public BinaryOpNode(ASTNode left, Token operator, ASTNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public List<ASTNode> getChildren() {
        List<ASTNode> children = new ArrayList<>();
        children.add(left);
        children.add(right);
        return children;
    }

    @Override
    public String toString() {
        return "BinaryOp: " + operator.lexeme;
    }
}

class UnaryOpNode extends ASTNode {
    private Token operator;
    private ASTNode expression;

    public UnaryOpNode(Token operator, ASTNode expression) {
        this.operator = operator;
        this.expression = expression;
    }

    @Override
    public List<ASTNode> getChildren() {
        return Collections.singletonList(expression);
    }

    @Override
    public String toString() {
        return "UnaryOp: " + operator.lexeme;
    }
}

class LiteralNode extends ASTNode {
    private Token token;

    public LiteralNode(Token token) {
        this.token = token;
    }

    @Override
    public List<ASTNode> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "Literal: " + token.literal + " (" + token.type + ")";
    }
}

class GroupingNode extends ASTNode {
    private ASTNode expression;

    public GroupingNode(ASTNode expression) {
        this.expression = expression;
    }

    @Override
    public List<ASTNode> getChildren() {
        return Collections.singletonList(expression);
    }

    @Override
    public String toString() {
        return "Grouping (...)";
    }
}

class FunctionCallNode extends ASTNode {
    private Token name;
    private List<ASTNode> arguments;

    public FunctionCallNode(Token name, List<ASTNode> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public List<ASTNode> getChildren() {
        return arguments;
    }

    @Override
    public String toString() {
        return "FunctionCall: " + name.lexeme + "(" + arguments.size() + " args)";
    }
}

// ============================================
// KONTROLNE STRUKTURE
// ============================================

class IfNode extends ASTNode {
    private Token token;
    private ASTNode condition;
    private List<ASTNode> thenBranch;
    private List<ElseIfNode> elseIfs;
    private List<ASTNode> elseBranch;

    public IfNode(Token token, ASTNode condition, List<ASTNode> thenBranch,
                  List<ElseIfNode> elseIfs, List<ASTNode> elseBranch) {
        this.token = token;
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseIfs = elseIfs;
        this.elseBranch = elseBranch;
    }

    @Override
    public List<ASTNode> getChildren() {
        List<ASTNode> children = new ArrayList<>();
        children.add(condition);
        children.addAll(thenBranch);
        if (elseIfs != null) children.addAll(elseIfs);
        if (elseBranch != null) children.addAll(elseBranch);
        return children;
    }

    @Override
    public String toString() {
        return "If";
    }
}

class ElseIfNode extends ASTNode {
    private ASTNode condition;
    private List<ASTNode> body;

    public ElseIfNode(ASTNode condition, List<ASTNode> body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public List<ASTNode> getChildren() {
        List<ASTNode> children = new ArrayList<>();
        children.add(condition);
        children.addAll(body);
        return children;
    }

    @Override
    public String toString() {
        return "ElseIf";
    }
}

class ForLoopNode extends ASTNode {
    private Token token;
    private ASTNode init;
    private ASTNode condition;
    private ASTNode increment;
    private List<ASTNode> body;

    public ForLoopNode(Token token, ASTNode init, ASTNode condition,
                       ASTNode increment, List<ASTNode> body) {
        this.token = token;
        this.init = init;
        this.condition = condition;
        this.increment = increment;
        this.body = body;
    }

    @Override
    public List<ASTNode> getChildren() {
        List<ASTNode> children = new ArrayList<>();
        children.add(init);
        children.add(condition);
        children.add(increment);
        children.addAll(body);
        return children;
    }

    @Override
    public String toString() {
        return "ForLoop";
    }
}

class WhileLoopNode extends ASTNode {
    private Token token;
    private ASTNode condition;
    private List<ASTNode> body;

    public WhileLoopNode(Token token, ASTNode condition, List<ASTNode> body) {
        this.token = token;
        this.condition = condition;
        this.body = body;
    }

    @Override
    public List<ASTNode> getChildren() {
        List<ASTNode> children = new ArrayList<>();
        children.add(condition);
        children.addAll(body);
        return children;
    }

    @Override
    public String toString() {
        return "WhileLoop";
    }
}

class ConditionNode extends ASTNode {
    private ASTNode left;
    private Token operator;
    private ASTNode right;

    public ConditionNode(ASTNode left, Token operator, ASTNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public List<ASTNode> getChildren() {
        List<ASTNode> children = new ArrayList<>();
        children.add(left);
        children.add(right);
        return children;
    }

    @Override
    public String toString() {
        return "Condition: " + operator.lexeme;
    }
}

class LogicalOpNode extends ASTNode {
    private ASTNode left;
    private Token operator;
    private ASTNode right;

    public LogicalOpNode(ASTNode left, Token operator, ASTNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public List<ASTNode> getChildren() {
        List<ASTNode> children = new ArrayList<>();
        children.add(left);
        children.add(right);
        return children;
    }

    @Override
    public String toString() {
        return "LogicalOp: " + operator.lexeme;
    }
}

// ============================================
// POSEBNE NAREDBE
// ============================================

class PrintNode extends ASTNode {
    private Token token;
    private ASTNode expression;

    public PrintNode(Token token, ASTNode expression) {
        this.token = token;
        this.expression = expression;
    }

    @Override
    public List<ASTNode> getChildren() {
        return Collections.singletonList(expression);
    }

    @Override
    public String toString() {
        return "Print";
    }
}

class ScanNode extends ASTNode {
    private Token token;
    private ASTNode identifier;

    public ScanNode(Token token, ASTNode identifier) {
        this.token = token;
        this.identifier = identifier;
    }

    @Override
    public List<ASTNode> getChildren() {
        return Collections.singletonList(identifier);
    }

    @Override
    public String toString() {
        return "Scan";
    }
}