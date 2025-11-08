package parser;

import lexer.token.Token;
import lexer.token.TokenType;
import java.util.List;
import java.util.ArrayList;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // ============================================
    // GLAVNI ENTRY POINT
    // ============================================

    public ASTNode parse() {
        try {
            return program();
        } catch (ParseException e) {
            System.err.println("Parse error: " + e.getMessage());
            return null;
        }
    }

    // ============================================
    // PROGRAM I TOP-LEVEL ELEMENTI
    // ============================================

    // program = N_L { top_element } N_L
    private ASTNode program() {
        ProgramNode prog = new ProgramNode();

        consumeNewlines(); // N_L

        while (!isAtEnd() && !check(TokenType.EOF)) {
            if (check(TokenType.NEWLINE)) {
                advance();
                continue;
            }
            prog.addElement(topElement());
        }

        consumeNewlines(); // N_L
        return prog;
    }

    // top_element = main_function | function_def | declaration | assignment
    private ASTNode topElement() {
        // main funkcija
        if (check(TokenType.INT) && checkAhead(1, TokenType.MAIN)) {
            return mainFunction();
        }

        // funkcija ili void funkcija
        if (isType() || check(TokenType.IDENT)) {
            // Proveri da li je funkcija (ima zagrade)
            if (checkFunctionAhead()) {
                return functionDef();
            }
        }

        // deklaracija
        if (isType() || check(TokenType.BOOL)) {
            return declaration();
        }

        // assignment
        if (check(TokenType.IDENT)) {
            return assignment();
        }

        throw error(peek(), "Expected declaration, assignment or function");
    }

    // main_function = INT MAIN LUGLASTA N_L { statement } N_L return RUGLASTA
    private ASTNode mainFunction() {
        consume(TokenType.INT, "Expected 'int'");
        Token mainToken = consume(TokenType.MAIN, "Expected 'main'");
        consume(TokenType.LUGLASTA, "Expected '{'");
        consumeNewlines();

        MainFunctionNode main = new MainFunctionNode(mainToken);

        while (!check(TokenType.RETURN) && !isAtEnd()) {
            if (check(TokenType.NEWLINE)) {
                advance();
                continue;
            }
            main.addStatement(statement());
        }

        consumeNewlines();
        main.setReturn(returnStmt());
        consume(TokenType.RUGLASTA, "Expected '}'");

        return main;
    }

    // return = RETURN expression | RETURN CHARACTER | RETURN STRING
    private ASTNode returnStmt() {
        Token ret = consume(TokenType.RETURN, "Expected 'return'");
        ASTNode expr = expression();
        return new ReturnNode(ret, expr);
    }

    // function_def = void_function | function
    private ASTNode functionDef() {
        // Proveri da li je void (IDENT direktno)
        if (check(TokenType.IDENT)) {
            return voidFunction();
        } else {
            return function();
        }
    }

    // void_function = IDENT LPAREN [param_list] RPAREN LUGLASTA { statement } RUGLASTA
    private ASTNode voidFunction() {
        Token name = consume(TokenType.IDENT, "Expected function name");
        consume(TokenType.LPAREN, "Expected '('");

        List<ParamNode> params = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            params = paramList();
        }

        consume(TokenType.RPAREN, "Expected ')'");
        consume(TokenType.LUGLASTA, "Expected '{'");

        VoidFunctionNode func = new VoidFunctionNode(name, params);

        while (!check(TokenType.RUGLASTA) && !isAtEnd()) {
            if (check(TokenType.NEWLINE)) {
                advance();
                continue;
            }
            func.addStatement(statement());
        }

        consume(TokenType.RUGLASTA, "Expected '}'");
        return func;
    }

    // function = type IDENT LPAREN [param_list] RPAREN LUGLASTA { statement } N_L return RUGLASTA
    private ASTNode function() {
        Token type = consumeType();
        Token name = consume(TokenType.IDENT, "Expected function name");
        consume(TokenType.LPAREN, "Expected '('");

        List<ParamNode> params = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            params = paramList();
        }

        consume(TokenType.RPAREN, "Expected ')'");
        consume(TokenType.LUGLASTA, "Expected '{'");

        FunctionNode func = new FunctionNode(type, name, params);

        while (!check(TokenType.RETURN) && !isAtEnd()) {
            if (check(TokenType.NEWLINE)) {
                advance();
                continue;
            }
            func.addStatement(statement());
        }

        consumeNewlines();
        func.setReturn(returnStmt());
        consume(TokenType.RUGLASTA, "Expected '}'");

        return func;
    }

    // param_list = type IDENT { SEPARATOR_COMMA type IDENT }
    private List<ParamNode> paramList() {
        List<ParamNode> params = new ArrayList<>();

        Token type = consumeType();
        Token name = consume(TokenType.IDENT, "Expected parameter name");
        params.add(new ParamNode(type, name));

        while (match(TokenType.SEPARATOR_COMMA)) {
            type = consumeType();
            name = consume(TokenType.IDENT, "Expected parameter name");
            params.add(new ParamNode(type, name));
        }

        return params;
    }

    // statement = [N_L] stmt_content [N_L]
    private ASTNode statement() {
        consumeNewlines();
        ASTNode stmt = stmtContent();
        consumeNewlines();
        return stmt;
    }

    // stmt_content = (declaration | assignment | function_call | if_stmt | loop_stmt | print_stmt | scan_stmt)
    private ASTNode stmtContent() {
        if (isType() || check(TokenType.BOOL)) {
            return declaration();
        }
        if (check(TokenType.IF)) {
            return ifStmt();
        }
        if (check(TokenType.FOR) || check(TokenType.WHILE)) {
            return loopStmt();
        }
        if (check(TokenType.PRINT)) {
            return printStmt();
        }
        if (check(TokenType.SCAN)) {
            return scanStmt();
        }
        if (check(TokenType.IDENT)) {
            // Proveri da li je funkcija ili assignment
            if (checkAhead(1, TokenType.LPAREN)) {
                return functionCall();
            } else {
                return assignment();
            }
        }

        throw error(peek(), "Expected statement");
    }

    // ============================================
    // DEKLARACIJE I DODELE
    // ============================================

    // declaration = var_decl | bool_decl
    private ASTNode declaration() {
        if (check(TokenType.BOOL)) {
            return boolDecl();
        }
        return varDecl();
    }

    // var_decl = type decl_list
    private ASTNode varDecl() {
        Token type = consumeType();
        return declList(type);
    }

    // bool_decl = BOOL decl_list
    private ASTNode boolDecl() {
        Token type = consume(TokenType.BOOL, "Expected 'bool'");
        return declList(type);
    }

    // decl_list = decl_item { SEPARATOR_COMMA decl_item }
    private ASTNode declList(Token type) {
        DeclarationListNode declList = new DeclarationListNode(type);

        declList.addItem(declItem(type));

        while (match(TokenType.SEPARATOR_COMMA)) {
            declList.addItem(declItem(type));
        }

        return declList;
    }

    // decl_item = IDENT [ array_dims ] [ ASSIGN init_value ]
    private DeclItemNode declItem(Token type) {
        Token name = consume(TokenType.IDENT, "Expected identifier");

        List<ASTNode> arrayDims = null;
        if (check(TokenType.LBRACKET)) {
            arrayDims = arrayDims();
        }

        ASTNode initValue = null;
        if (match(TokenType.ASSIGN)) {
            initValue = initValue();
        }

        return new DeclItemNode(type, name, arrayDims, initValue);
    }

    // array_dims = LBRACKET [ expression ] RBRACKET { LBRACKET [ expression ] RBRACKET }
    private List<ASTNode> arrayDims() {
        List<ASTNode> dims = new ArrayList<>();

        consume(TokenType.LBRACKET, "Expected '['");
        if (!check(TokenType.RBRACKET)) {
            dims.add(expression());
        } else {
            dims.add(null); // prazna dimenzija
        }
        consume(TokenType.RBRACKET, "Expected ']'");

        while (check(TokenType.LBRACKET)) {
            advance();
            if (!check(TokenType.RBRACKET)) {
                dims.add(expression());
            } else {
                dims.add(null);
            }
            consume(TokenType.RBRACKET, "Expected ']'");
        }

        return dims;
    }

    // init_value = expression
    private ASTNode initValue() {
        return expression();
    }

    // assignment = identifier ASSIGN expression
    private ASTNode assignment() {
        ASTNode id = identifier();
        consume(TokenType.ASSIGN, "Expected '='");
        ASTNode expr = expression();
        return new AssignmentNode(id, expr);
    }

    // identifier = IDENT { LBRACKET expression RBRACKET }
    private ASTNode identifier() {
        Token name = consume(TokenType.IDENT, "Expected identifier");
        List<ASTNode> indices = new ArrayList<>();

        while (match(TokenType.LBRACKET)) {
            indices.add(expression());
            consume(TokenType.RBRACKET, "Expected ']'");
        }

        return new IdentifierNode(name, indices);
    }

    // ============================================
    // IZRAZI
    // ============================================

    // expression = add
    private ASTNode expression() {
        return add();
    }

    // add = mul { ( ADD | SUBTRACT ) mul }
    private ASTNode add() {
        ASTNode left = mul();

        while (match(TokenType.ADD, TokenType.SUBTRACT)) {
            Token op = previous();
            ASTNode right = mul();
            left = new BinaryOpNode(left, op, right);
        }

        return left;
    }

    // mul = unary { ( MULTIPLY | DIVIDE | PERCENT ) unary }
    private ASTNode mul() {
        ASTNode left = unary();

        while (match(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.PERCENT)) {
            Token op = previous();
            ASTNode right = unary();
            left = new BinaryOpNode(left, op, right);
        }

        return left;
    }

    // unary = [ ADD | SUBTRACT ] power
    private ASTNode unary() {
        if (match(TokenType.ADD, TokenType.SUBTRACT)) {
            Token op = previous();
            ASTNode expr = power();
            return new UnaryOpNode(op, expr);
        }

        return power();
    }

    // power = primary [ POW power ]
    private ASTNode power() {
        ASTNode left = primary();

        if (match(TokenType.POW)) {
            Token op = previous();
            ASTNode right = power(); // desna asocijativnost
            return new BinaryOpNode(left, op, right);
        }

        return left;
    }

    // primary = NUMBER | CHAR_LIT | STRING_LIT | function_call | identifier | LPAREN expression RPAREN
    private ASTNode primary() {
        // Literali
        if (match(TokenType.INT_LIT, TokenType.FlOAT_LIT, TokenType.DOUBLE_LIT)) {
            return new LiteralNode(previous());
        }

        // CHAR_LIT i STRING_LIT nisu u TokenType enum-u, pretpostavljam da su u literal polju
        // Ako imate posebne tokene za njih, dodajte ovde

        // Zagrada
        if (match(TokenType.LPAREN)) {
            ASTNode expr = expression();
            consume(TokenType.RPAREN, "Expected ')' after expression");
            return new GroupingNode(expr);
        }

        // Funkcija ili identifikator
        if (check(TokenType.IDENT)) {
            if (checkAhead(1, TokenType.LPAREN)) {
                return functionCall();
            } else {
                return identifier();
            }
        }

        throw error(peek(), "Expected expression");
    }

    // function_call = IDENT LPAREN [ args ] RPAREN
    private ASTNode functionCall() {
        Token name = consume(TokenType.IDENT, "Expected function name");
        consume(TokenType.LPAREN, "Expected '('");

        List<ASTNode> arguments = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            arguments = args();
        }

        consume(TokenType.RPAREN, "Expected ')'");
        return new FunctionCallNode(name, arguments);
    }

    // args = expression { SEPARATOR_COMMA expression }
    private List<ASTNode> args() {
        List<ASTNode> arguments = new ArrayList<>();

        arguments.add(expression());

        while (match(TokenType.SEPARATOR_COMMA)) {
            arguments.add(expression());
        }

        return arguments;
    }

    // ============================================
    // KONTROLNE STRUKTURE
    // ============================================

    // if_stmt = IF LPAREN condition RPAREN LUGLASTA { statement } RUGLASTA
    //           [ELSE IF LPAREN condition RPAREN LUGLASTA { statement } RUGLASTA]
    //           [ELSE LUGLASTA { statement } RUGLASTA]
    private ASTNode ifStmt() {
        Token ifToken = consume(TokenType.IF, "Expected 'if'");
        consume(TokenType.LPAREN, "Expected '('");
        ASTNode condition = condition();
        consume(TokenType.RPAREN, "Expected ')'");
        consume(TokenType.LUGLASTA, "Expected '{'");

        List<ASTNode> thenBranch = new ArrayList<>();
        while (!check(TokenType.RUGLASTA) && !isAtEnd()) {
            if (check(TokenType.NEWLINE)) {
                advance();
                continue;
            }
            thenBranch.add(statement());
        }
        consume(TokenType.RUGLASTA, "Expected '}'");

        List<ElseIfNode> elseIfs = new ArrayList<>();
        List<ASTNode> elseBranch = null;

        // ELSE IF
        while (check(TokenType.ELSE) && checkAhead(1, TokenType.IF)) {
            advance(); // ELSE
            advance(); // IF
            consume(TokenType.LPAREN, "Expected '('");
            ASTNode elseIfCond = condition();
            consume(TokenType.RPAREN, "Expected ')'");
            consume(TokenType.LUGLASTA, "Expected '{'");

            List<ASTNode> elseIfBody = new ArrayList<>();
            while (!check(TokenType.RUGLASTA) && !isAtEnd()) {
                if (check(TokenType.NEWLINE)) {
                    advance();
                    continue;
                }
                elseIfBody.add(statement());
            }
            consume(TokenType.RUGLASTA, "Expected '}'");

            elseIfs.add(new ElseIfNode(elseIfCond, elseIfBody));
        }

        // ELSE
        if (match(TokenType.ELSE)) {
            consume(TokenType.LUGLASTA, "Expected '{'");
            elseBranch = new ArrayList<>();
            while (!check(TokenType.RUGLASTA) && !isAtEnd()) {
                if (check(TokenType.NEWLINE)) {
                    advance();
                    continue;
                }
                elseBranch.add(statement());
            }
            consume(TokenType.RUGLASTA, "Expected '}'");
        }

        return new IfNode(ifToken, condition, thenBranch, elseIfs, elseBranch);
    }

    // loop_stmt = FOR ... | WHILE ...
    private ASTNode loopStmt() {
        if (check(TokenType.FOR)) {
            return forLoop();
        } else {
            return whileLoop();
        }
    }

    // FOR LPAREN assignment SEPARATOR_COMMA condition SEPARATOR_COMMA assignment RPAREN LUGLASTA { statement } RUGLASTA
    private ASTNode forLoop() {
        Token forToken = consume(TokenType.FOR, "Expected 'for'");
        consume(TokenType.LPAREN, "Expected '('");

        ASTNode init = assignment();
        consume(TokenType.SEPARATOR_COMMA, "Expected ','");
        ASTNode cond = condition();
        consume(TokenType.SEPARATOR_COMMA, "Expected ','");
        ASTNode increment = assignment();

        consume(TokenType.RPAREN, "Expected ')'");
        consume(TokenType.LUGLASTA, "Expected '{'");

        List<ASTNode> body = new ArrayList<>();
        while (!check(TokenType.RUGLASTA) && !isAtEnd()) {
            if (check(TokenType.NEWLINE)) {
                advance();
                continue;
            }
            body.add(statement());
        }
        consume(TokenType.RUGLASTA, "Expected '}'");

        return new ForLoopNode(forToken, init, cond, increment, body);
    }

    // WHILE LPAREN condition RPAREN LUGLASTA { statement } RUGLASTA
    private ASTNode whileLoop() {
        Token whileToken = consume(TokenType.WHILE, "Expected 'while'");
        consume(TokenType.LPAREN, "Expected '('");
        ASTNode cond = condition();
        consume(TokenType.RPAREN, "Expected ')'");
        consume(TokenType.LUGLASTA, "Expected '{'");

        List<ASTNode> body = new ArrayList<>();
        while (!check(TokenType.RUGLASTA) && !isAtEnd()) {
            if (check(TokenType.NEWLINE)) {
                advance();
                continue;
            }
            body.add(statement());
        }
        consume(TokenType.RUGLASTA, "Expected '}'");

        return new WhileLoopNode(whileToken, cond, body);
    }

    // condition = expression , rel_op , expression , [ {logical_op , condition} ]
    private ASTNode condition() {
        ASTNode left = expression();
        Token op = relOp();
        ASTNode right = expression();

        ASTNode cond = new ConditionNode(left, op, right);

        while (isLogicalOp()) {
            Token logOp = logicalOp();
            ASTNode rightCond = condition();
            cond = new LogicalOpNode(cond, logOp, rightCond);
        }

        return cond;
    }

    // rel_op = LT | LE | GT | GE | EQ | NEQ
    private Token relOp() {
        if (match(TokenType.LT, TokenType.LE, TokenType.GT,
                TokenType.GE, TokenType.EQ, TokenType.NEQ)) {
            return previous();
        }
        throw error(peek(), "Expected relational operator");
    }

    // logical_op = AND | OR
    private Token logicalOp() {
        if (match(TokenType.AND, TokenType.OR)) {
            return previous();
        }
        throw error(peek(), "Expected logical operator");
    }

    private boolean isLogicalOp() {
        return check(TokenType.AND) || check(TokenType.OR);
    }

    // ============================================
    // POSEBNE NAREDBE
    // ============================================

    // print_stmt = PRINT LPAREN expression RPAREN
    private ASTNode printStmt() {
        Token print = consume(TokenType.PRINT, "Expected 'print'");
        consume(TokenType.LPAREN, "Expected '('");
        ASTNode expr = expression();
        consume(TokenType.RPAREN, "Expected ')'");
        return new PrintNode(print, expr);
    }

    // scan_stmt = SCAN LPAREN identifier RPAREN
    private ASTNode scanStmt() {
        Token scan = consume(TokenType.SCAN, "Expected 'scan'");
        consume(TokenType.LPAREN, "Expected '('");
        ASTNode id = identifier();
        consume(TokenType.RPAREN, "Expected ')'");
        return new ScanNode(scan, id);
    }

    // ============================================
    // HELPER METODE
    // ============================================

    private boolean isType() {
        return check(TokenType.INT) || check(TokenType.FLOAT) ||
                check(TokenType.DOUBLE) || check(TokenType.CHAR) ||
                check(TokenType.STRING);
    }

    private Token consumeType() {
        if (match(TokenType.INT, TokenType.FLOAT, TokenType.DOUBLE,
                TokenType.CHAR, TokenType.STRING)) {
            return previous();
        }
        throw error(peek(), "Expected type");
    }

    private boolean checkFunctionAhead() {
        // Proveri da li nakon IDENT ili tipa dolazi LPAREN (funkcija)
        int pos = current;
        if (isType()) pos++;
        if (pos < tokens.size() && tokens.get(pos).type == TokenType.IDENT) {
            pos++;
            return pos < tokens.size() && tokens.get(pos).type == TokenType.LPAREN;
        }
        if (check(TokenType.IDENT)) {
            pos++;
            return pos < tokens.size() && tokens.get(pos).type == TokenType.LPAREN;
        }
        return false;
    }

    private void consumeNewlines() {
        while (match(TokenType.NEWLINE)) {
            // Progutaj sve newline-ove
        }
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean checkAhead(int offset, TokenType type) {
        int pos = current + offset;
        if (pos >= tokens.size()) return false;
        return tokens.get(pos).type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return current >= tokens.size() || peek().type == TokenType.EOF;
    }

    private Token peek() {
        if (current >= tokens.size()) {
            return tokens.get(tokens.size() - 1);
        }
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseException error(Token token, String message) {
        return new ParseException(message + " at " + token.toString());
    }

    // ============================================
    // TESTIRANJE I ISPIS STABLA
    // ============================================

    public void printTree(ASTNode node, String indent) {
        if (node == null) return;
        System.out.println(indent + node.toString());
        for (ASTNode child : node.getChildren()) {
            printTree(child, indent + "  ");
        }
    }

    // Exception klasa
    public static class ParseException extends RuntimeException {
        public ParseException(String message) {
            super(message);
        }
    }
}