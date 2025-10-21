package lexer;

import lexer.token.Token;
import lexer.token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final ScannerCore sc;
    private final String[] source;  // 🔹 Niz linija
    private final List<Token> tokens = new ArrayList<>();

    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
            Map.entry("Dunk", TokenType.INT),
            Map.entry("Lay-up", TokenType.FLOAT),
            Map.entry("Jelly", TokenType.DOUBLE),
            Map.entry("Three", TokenType.CHAR),
            Map.entry("Middie", TokenType.BOOL),
            Map.entry("Block", TokenType.STRING),
            Map.entry("Steal", TokenType.ARRAY),

            Map.entry("LePrint", TokenType.PRINT),
            Map.entry("LeScan", TokenType.SCAN),

            Map.entry("Cleveland", TokenType.MAIN),
            Map.entry("Ohio", TokenType.RETURN),

            Map.entry("from", TokenType.ASSIGN),
            Map.entry("blocked-by", TokenType.DIVIDE),
            Map.entry("alley-oop-to", TokenType.MULTIPLY),
            Map.entry("passes-to", TokenType.ADD),
            Map.entry("fouled-by", TokenType.SUBTRACT),

            Map.entry("good", TokenType.LT),
            Map.entry("not-bad", TokenType.LE),
            Map.entry("bad", TokenType.GT),
            Map.entry("not-good", TokenType.GE),
            Map.entry("coast-to-coast", TokenType.EQ),
            Map.entry("chase-down", TokenType.NEQ),

            Map.entry("and", TokenType.AND),
            Map.entry("or", TokenType.OR),

            Map.entry("LBJ", TokenType.IF),
            Map.entry("Chosen-one", TokenType.ELSE),
            Map.entry("Bron-Bron", TokenType.ELSE_IF),
            Map.entry("L-train", TokenType.FOR),
            Map.entry("LeGoat", TokenType.WHILE),
            Map.entry("LeBwan", TokenType.DO)
    );

    public Lexer(String[] source) {
        this.source = source;
        this.sc = new ScannerCore(source);
    }

    public List<Token> scanTokens() {
        while (!sc.isAtEnd()) {
            sc.beginToken();
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "\0", null, sc.getLine(), sc.getCol(), sc.getCol()));
        return tokens;
    }

    private void scanToken() {
        char c = sc.advance();

        switch (c) {
            case '(' -> add(TokenType.LPAREN, "(");
            case ')' -> add(TokenType.RPAREN, ")");
            case '[' -> add(TokenType.LBRACKET, "[");
            case ']' -> add(TokenType.RBRACKET, "]");
            case '{' -> add(TokenType.LUGLASTA, "{");
            case '}' -> add(TokenType.RUGLASTA, "}");
            case ',' -> add(TokenType.SEPARATOR_COMMA, ",");
            case ':' -> add(TokenType.TYPE_COLON, ":");
            case '%' -> add(TokenType.PERCENT, "%");
            case '\n' -> tokens.add(new Token(TokenType.NEWLINE, "\n", null,
                    sc.getStartLine(), sc.getStartCol(), sc.getStartCol()));
            case ' ', '\r', '\t', '|' -> {}
            default -> {
                if (Character.isDigit(c)) number(c);
                else if (isIdentStart(c)) identifier(c);
                else throw error("Unexpected character");
            }
        }
    }

    private void number(char first) {
        StringBuilder sb = new StringBuilder();
        sb.append(first);
        while (Character.isDigit(sc.peek())) sb.append(sc.advance());

        String text = sb.toString();
        char nextChar = sc.peek();
        if (Character.isAlphabetic(nextChar)) {
            throw error("Error: Character in int literal");
        }
        addLiteralInt(text);
    }

    private void identifier(char first) {
        StringBuilder sb = new StringBuilder();
        sb.append(first);
        while (isIdentPart(sc.peek())) sb.append(sc.advance());

        String text = sb.toString();
        TokenType type;
        if (text.startsWith("Le")) {
            type = KEYWORDS.getOrDefault(text, TokenType.IDENT);
        } else {
            type = KEYWORDS.getOrDefault(text, TokenType.GRESKA);
            if (type.equals(TokenType.GRESKA)) {
                throw error("Identifier name incorrect");
            }
        }
        add(type, text);
    }

    private boolean isIdentStart(char c) {
        return Character.isLetter(c) || c == '_' || c == '-';
    }

    private boolean isIdentPart(char c) {
        return isIdentStart(c) || Character.isDigit(c);
    }

    private void add(TokenType type, String lexeme) {
        tokens.add(new Token(type, lexeme, null,
                sc.getStartLine(), sc.getStartCol(), sc.getCol() - 1));
    }

    private void addLiteralInt(String literal) {
        tokens.add(new Token(TokenType.INT_LIT, literal, Integer.valueOf(literal),
                sc.getStartLine(), sc.getStartCol(), sc.getCol() - 1));
    }

    private RuntimeException error(String msg) {
        return new RuntimeException("LEXER > " + msg + " at line " + sc.getStartLine() + ", col " + sc.getStartCol());
    }
}
