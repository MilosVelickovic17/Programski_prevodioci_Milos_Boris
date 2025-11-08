package lexer;

import lexer.token.Token;
import lexer.token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final ScannerCore sc;
    private final String[] source;
    private final List<Token> tokens = new ArrayList<>();

    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
            Map.entry("Dunk", TokenType.INT),
            Map.entry("Lay-up", TokenType.FLOAT),
            Map.entry("Jelly", TokenType.DOUBLE),
            Map.entry("Middie", TokenType.BOOL),
            Map.entry("Three", TokenType.CHAR),
            Map.entry("Block", TokenType.STRING),

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
    //0 - Bronny, 1 - Zhuri, 2 - LeBron, 3 - James, 4 - Savannah, 5 - Gloria, 6 - Bryce,
    // 7 - King, 8 - Goat, 9 - Sunshine

    private static final Map<String, TokenType> NUMBERS = Map.ofEntries(
            Map.entry("Bronny", TokenType.NULA),
            Map.entry("Zhuri", TokenType.JEDAN),
            Map.entry("LeBron", TokenType.DVA),
            Map.entry("James", TokenType.TRI),
            Map.entry("Savannah", TokenType.CETIRI),
            Map.entry("Gloria", TokenType.PET),
            Map.entry("Bryce", TokenType.SEST),
            Map.entry("King", TokenType.SEDAM),
            Map.entry("Goat", TokenType.OSAM),
            Map.entry("Sunshine", TokenType.DEVET)
    );

    private static final Map<String, Integer> LITERALS = Map.ofEntries(
            Map.entry("Bronny", 0),
            Map.entry("Zhuri", 1),
            Map.entry("LeBron", 2),
            Map.entry("James", 3),
            Map.entry("Savannah", 4),
            Map.entry("Gloria", 5),
            Map.entry("Bryce", 6),
            Map.entry("King", 7),
            Map.entry("Goat", 8),
            Map.entry("Sunshine", 9)
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
        tokens.add(new Token(TokenType.EOF, "\0", null,  sc.getLine(), sc.getCol(), sc.getCol()));
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
            case '!' -> identifier(c);
            default -> {
                if (isIdentStart(c)) identifier(c);
                else throw error("Unexpected character");
            }
        }
    }

    private void number(String start, char sign) {
        StringBuilder sb = new StringBuilder();
        StringBuilder number_builder = new StringBuilder();
        Integer num = LITERALS.get(start);
        boolean f = false;
        boolean zero = false;
        if(num == 0){
            zero = true;
        }
        if(sign == '.') {
            f = true;
            number_builder.append(num.toString());
            number_builder.append('.');
        }
        if(!zero || f) {
            while (isIdentPart(sc.peek())) {
                if (sc.peek() == '_') {
                    int broj = LITERALS.getOrDefault(sb.toString(), -1);
                    if (broj != -1) {
                        num = num * 10 + broj;
                    } else {
                        throw error("Error: Number written incorrectly");
                    }
                    sc.advance();
                    sb.setLength(0);
                }else if(sc.peek() == '.'){
                    if(!number_builder.isEmpty()) throw error("Error: Second '.' in float number");
                    int broj = LITERALS.getOrDefault(sb.toString(), -1);
                    if (broj != -1) {
                        num = num * 10 + broj;
                        number_builder.append(num.toString());
                        number_builder.append('.');
                        num = 1;
                        f = true;
                    } else {
                        throw error("Error: Number written incorrectly");
                    }
                    sc.advance();
                    sb.setLength(0);
                }
                else {
                    sb.append(sc.advance());
                }
            }
        }
        else throw error("Error: Multi-digit number starting with 0");

        if(!sb.isEmpty()){
            int broj = LITERALS.getOrDefault(sb.toString(), -1);
            if(broj != -1){
                num = num*10 + broj;
            }else{
                throw error("Error: Number written incorrectly");
            }
        }

        if (Character.isDigit(sc.peek())) {
            throw error("Error: Number in string of numbers");
        }
        if(f){
            if(zero){
                number_builder.append(num);
            }
            else {
                number_builder.append(num.toString().substring(1));
            }
            addLiteralFloat(number_builder.toString());
        }else addLiteralInt(num.toString());
    }

    private void identifier(char first) {
        StringBuilder sb = new StringBuilder();
        sb.append(first);
        boolean isNum = false;
        while (isIdentPart(sc.peek())) {
            if(sc.peek() == '_'){
                String num = sb.toString();
                TokenType t = NUMBERS.getOrDefault(num, TokenType.IDENT);
                if(t != TokenType.IDENT){
                    char sign = sc.advance();
                    number(num, sign);
                    isNum = true;
                    break;
                }
            }
            else if(sc.peek() == '.'){
                String num = sb.toString();
                TokenType t = NUMBERS.getOrDefault(num, TokenType.IDENT);
                if(t != TokenType.IDENT){
                    char sign = sc.advance();
                    number(num, sign);
                    isNum = true;
                    break;
                }
                throw error("Error: Character '.' in string of numbers");
            }
            sb.append(sc.advance());
        }

        if(!isNum) {
            if(first == '!'){
                throw error("Error: symbol '!' can only be used before number words!");
            }
            String text = sb.toString();
            TokenType type;
            type = NUMBERS.getOrDefault(text, TokenType.IDENT);
            if(type != TokenType.IDENT){
                Integer broj = LITERALS.getOrDefault(text, -1);
                addLiteralInt(broj.toString());
                return;
            }

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
    }

    private boolean isIdentStart(char c) {
        return Character.isLetter(c) || c == '_' || c == '-' || c == '.';
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
    private void addLiteralFloat(String literal) {
        tokens.add(new Token(TokenType.FlOAT_LIT, literal, Float.valueOf(literal),
                sc.getStartLine(), sc.getStartCol(), sc.getCol() - 1));
    }

    private RuntimeException error(String msg) {
        return new RuntimeException("LEXER > " + msg + " at line " + sc.getStartLine() + ", col " + sc.getStartCol());
    }
}
