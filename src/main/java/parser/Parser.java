package parser;

import lexer.token.Token;
import lexer.token.TokenType;
import parser.ast.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Program parseProgram() {
        List<Stmt> tops = new ArrayList<>();
        skipNewlines();

        while (!isAtEnd() && !check(TokenType.EOF)) {
            tops.add(topElement());
            skipNewlines();
        }

        while (!isAtEnd() && peek().type == TokenType.NEWLINE) advance();

        if (!isAtEnd() && peek().type == TokenType.EOF) {
            advance();
            return new Program(tops);
        }

        if (isAtEnd()) return new Program(tops);
        throw error(peek(), "Expected end of file, got: " + peek().type);
    }

    private Stmt topElement() {
        if (check(TokenType.INT) && checkNext(TokenType.MAIN)) {
            return mainFunction();
        }
        if (isTypeStart(peek().type)) {
            if (lookAheadIsTypedFunction()) return typedFunction();
            return declaration();
        }
        if (check(TokenType.IDENT) && lookAheadIsFuncHeader()) {
            return voidFunction();
        }
        throw error(peek(), "Unexpected top-level element");
    }

    private boolean lookAheadIsFuncHeader() {
        return check(TokenType.IDENT) && tokens.get(current + 1).type == TokenType.LPAREN;
    }

    private boolean lookAheadIsTypedFunction() {
        int save = current;
        type();
        boolean ok = check(TokenType.IDENT) && tokens.get(current + 1).type == TokenType.LPAREN;
        current = save;
        return ok;
    }

    private boolean isTypeStart(TokenType tt) {
        return tt == TokenType.INT || tt == TokenType.FLOAT || tt == TokenType.DOUBLE ||
                tt == TokenType.CHAR || tt == TokenType.STRING || tt == TokenType.BOOL;
    }

    private Stmt mainFunction() {
        consume(TokenType.INT, "Expected INT before MAIN");
        Token mainTok = consume(TokenType.MAIN, "Expected MAIN keyword");
        consume(TokenType.LUGLASTA, "Expected '{' after MAIN");
        skipNewlines();

        List<Stmt> body = new ArrayList<>();
        while (!check(TokenType.RETURN) && !check(TokenType.RUGLASTA) && !isAtEnd()) {
            body.add(statement());
            skipNewlines();
        }

        Return ret = returnStmt();
        consume(TokenType.RUGLASTA, "Expected '}' after main body");

        return new Function(
                Types.ReturnType.of(new Types.Type(Types.Base.INT)),
                mainTok,
                List.of(),
                new Block(concat(body, ret))
        );
    }

    private List<Stmt> concat(List<Stmt> l, Stmt one) {
        List<Stmt> out = new ArrayList<>(l);
        out.add(one);
        return out;
    }

    private Stmt voidFunction() {
        Token name = consume(TokenType.IDENT, "Expected function name");
        consume(TokenType.LPAREN, "Expected '(' after function name");
        List<Function.Param> params = paramListOpt();
        consume(TokenType.RPAREN, "Expected ')' after parameters");
        Block body = block();
        return new Function(Types.ReturnType.VOID(), name, params, body);
    }

    private Stmt typedFunction() {
        Types.Type t = type();
        Token name = consume(TokenType.IDENT, "Expected function name");
        consume(TokenType.LPAREN, "Expected '(' after function name");
        List<Function.Param> params = paramListOpt();
        consume(TokenType.RPAREN, "Expected ')' after parameters");
        Block body = blockWithOptionalReturn(true);
        return new Function(Types.ReturnType.of(t), name, params, body);
    }

    private List<Function.Param> paramListOpt() {
        List<Function.Param> params = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            Types.Type pt = type();
            Token name = consume(TokenType.IDENT, "Expected parameter name");
            params.add(new Function.Param(pt, name));
            while (match(TokenType.SEPARATOR_COMMA)) {
                pt = type();
                name = consume(TokenType.IDENT, "Expected parameter name");
                params.add(new Function.Param(pt, name));
            }
        }
        return params;
    }

    private Block block() {
        consume(TokenType.LUGLASTA, "Expected '{'");
        skipNewlines();
        List<Stmt> stmts = new ArrayList<>();
        while (!check(TokenType.RUGLASTA) && !isAtEnd()) {
            stmts.add(statement());
            skipNewlines();
        }
        consume(TokenType.RUGLASTA, "Expected '}'");
        return new Block(stmts);
    }

    private Block blockWithOptionalReturn(boolean typed) {
        consume(TokenType.LUGLASTA, "Expected '{'");
        skipNewlines();
        List<Stmt> stmts = new ArrayList<>();
        while (!check(TokenType.RUGLASTA) && !isAtEnd()) {
            if (check(TokenType.RETURN)) {
                stmts.add(returnStmt());
                skipNewlines();
                continue;
            }
            stmts.add(statement());
            skipNewlines();
        }
        consume(TokenType.RUGLASTA, "Expected '}'");
        return new Block(stmts);
    }

    private Return returnStmt() {
        Token r = consume(TokenType.RETURN, "Expected RETURN");
        Expr val = expression();
        return new Return(r, val);
    }

    private Stmt statement() {
        if (match(TokenType.PRINT)) return printStmt(previous());
        if (match(TokenType.SCAN)) return scanStmt(previous());
        if (match(TokenType.IF)) return ifStmt();
        if (match(TokenType.WHILE)) return whileStmt();
        if (match(TokenType.FOR)) return forStmt();
        if (isTypeStart(peek().type)) return declaration();

        Expr expr = assignmentOrCallExpr();
        return new ExprStmt(expr);
    }

    private Stmt printStmt(Token kw) {
        consume(TokenType.LPAREN, "Expected '(' after PRINT");
        Expr e = expression();
        consume(TokenType.RPAREN, "Expected ')' after expression");
        return new Print(kw, e);
    }

    private Stmt scanStmt(Token kw) {
        consume(TokenType.LPAREN, "Expected '(' after SCAN");
        Expr target = identifierExpr();
        consume(TokenType.RPAREN, "Expected ')' after SCAN target");
        return new Scan(kw, target);
    }

    private Stmt declaration() {
        Types.Type t = type();
        List<VarDecl.Item> items = new ArrayList<>();
        items.add(declItem());
        while (match(TokenType.SEPARATOR_COMMA)) items.add(declItem());
        return new VarDecl(t, items);
    }

    private VarDecl.Item declItem() {
        Token name = consume(TokenType.IDENT, "Expected variable name");
        List<Expr> dims = new ArrayList<>();
        while (match(TokenType.LBRACKET)) {
            if (!check(TokenType.RBRACKET)) dims.add(expression());
            consume(TokenType.RBRACKET, "Expected ']' in array dimension");
        }
        Expr init = null;
        if (match(TokenType.ASSIGN)) init = expression();
        return new VarDecl.Item(name, dims, init);
    }

    private Stmt ifStmt() {
        consume(TokenType.LPAREN, "Expected '(' after IF");
        Expr cond = condition();
        consume(TokenType.RPAREN, "Expected ')' after IF condition");
        Block thenB = block();

        List<If.ElseIf> elseIfs = new ArrayList<>();
        while (match(TokenType.ELSE_IF)) {
            consume(TokenType.LPAREN, "Expected '(' after ELSE_IF");
            Expr c = condition();
            consume(TokenType.RPAREN, "Expected ')' after condition");
            Block b = block();
            elseIfs.add(new If.ElseIf(c, b));
        }

        Block elseB = null;
        if (match(TokenType.ELSE)) elseB = block();
        return new If(cond, thenB, elseIfs, elseB);
    }

    private Stmt whileStmt() {
        consume(TokenType.LPAREN, "Expected '(' after WHILE");
        Expr cond = condition();
        consume(TokenType.RPAREN, "Expected ')' after condition");
        Block body = block();
        return new While(cond, body);
    }

    private Stmt forStmt() {
        consume(TokenType.LPAREN, "Expected '(' after FOR");
        Stmt init = statementFromForPart();
        consume(TokenType.SEPARATOR_COMMA, "Expected ',' after for-init");
        Expr cond = condition();
        consume(TokenType.SEPARATOR_COMMA, "Expected ',' after for-condition");
        Stmt update = statementFromForPart();
        consume(TokenType.RPAREN, "Expected ')' after for-header");
        Block body = block();
        return new For(init, cond, update, body);
    }

    private Stmt statementFromForPart() {
        if (isTypeStart(peek().type)) return declaration();
        Expr e = assignmentOrCallExpr();
        return new ExprStmt(e);
    }

    private Expr condition() {
        Expr left = expression();
        Token op = relOp();
        Expr right = expression();
        Expr base = new Binary(left, op, right);
        while (match(TokenType.AND, TokenType.OR)) {
            Token lop = previous();
            Expr c2 = condition();
            base = new Binary(base, lop, c2);
        }
        return base;
    }

    private Token relOp() {
        if (match(TokenType.LT, TokenType.LE, TokenType.GT, TokenType.GE, TokenType.EQ, TokenType.NEQ))
            return previous();
        throw error(peek(), "Expected relational operator");
    }

    private Expr expression() {
        return add();
    }

    private Expr add() {
        Expr expr = mul();
        while (match(TokenType.ADD, TokenType.SUBTRACT)) {
            Token op = previous();
            Expr right = mul();
            expr = new Binary(expr, op, right);
        }
        return expr;
    }

    private Expr mul() {
        Expr expr = unary();
        while (match(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.PERCENT)) {
            Token op = previous();
            Expr right = unary();
            expr = new Binary(expr, op, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(TokenType.ADD, TokenType.SUBTRACT, TokenType.NEG)) {
            Token op = previous();
            Expr right = unary();
            return new Unary(op, right);
        }
        return power();
    }

    private Expr power() {
        Expr left = primary();
        if (match(TokenType.POW)) {
            Token op = previous();
            Expr right = power();
            return new Binary(left, op, right);
        }
        return left;
    }

    private Expr primary() {
        if (match(TokenType.INT_LIT, TokenType.FlOAT_LIT))
            return new Literal(previous().literal, previous());
        if (match(TokenType.STRING, TokenType.CHAR))
            return new Literal(previous().literal, previous());
        if (match(TokenType.LPAREN)) {
            Expr e = expression();
            consume(TokenType.RPAREN, "Expected ')' after expression");
            return new Grouping(e);
        }
        if (check(TokenType.IDENT)) return identifierOrCall();
        throw error(peek(), "Expected expression");
    }

    private Expr identifierOrCall() {
        Expr id = new Variable(consume(TokenType.IDENT, "Expected identifier"));
        if (match(TokenType.LPAREN)) {
            List<Expr> args = new ArrayList<>();
            if (!check(TokenType.RPAREN)) {
                args.add(expression());
                while (match(TokenType.SEPARATOR_COMMA)) args.add(expression());
            }
            Token rp = consume(TokenType.RPAREN, "Expected ')'");
            return new Call(id, rp, args);
        }
        return id;
    }

    private Expr identifierExpr() {
        Expr id = new Variable(consume(TokenType.IDENT, "Expected identifier"));
        return id;
    }

    private Expr assignmentOrCallExpr() {
        Expr left = identifierOrCall();
        if (left instanceof Call) return left;
        if (match(TokenType.ASSIGN)) {
            Token eq = previous();
            Expr val = expression();
            return new Assign(left, eq, val);
        }
        return left;
    }

    private Types.Type type() {
        Token t;
        if (match(TokenType.INT)) t = previous();
        else if (match(TokenType.FLOAT)) t = previous();
        else if (match(TokenType.DOUBLE)) t = previous();
        else if (match(TokenType.CHAR)) t = previous();
        else if (match(TokenType.STRING)) t = previous();
        else if (match(TokenType.BOOL)) t = previous();
        else throw error(peek(), "Expected type");

        Types.Base base = switch (t.type) {
            case INT -> Types.Base.INT;
            case FLOAT -> Types.Base.FLOAT;
            case DOUBLE -> Types.Base.DOUBLE;
            case CHAR -> Types.Base.CHAR;
            case STRING -> Types.Base.STRING;
            case BOOL -> Types.Base.BOOL;
            default -> throw new IllegalStateException("Unexpected: " + t.type);
        };
        return new Types.Type(base);
    }

    private void skipNewlines() {
        while (match(TokenType.NEWLINE)) {}
    }

    private boolean match(TokenType... types) {
        for (TokenType t : types) {
            if (check(t)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType t, String msg) {
        if (check(t)) return advance();
        throw error(peek(), msg + ", got: " + peek().type);
    }

    private boolean check(TokenType t) {
        if (isAtEnd()) return false;
        return peek().type == t;
    }

    private boolean checkNext(TokenType t) {
        if (current + 1 >= tokens.size()) return false;
        return tokens.get(current + 1).type == t;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return current >= tokens.size() || peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private RuntimeException error(Token at, String msg) {
        return new RuntimeException("PARSER > " + msg + " at line " + at.line + ", col " + at.colStart);
    }
}
