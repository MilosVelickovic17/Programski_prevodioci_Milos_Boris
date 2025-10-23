package lexer;

import java.util.ArrayList;
import java.util.List;

public final class ScannerCore {
    private final String[] src;
    private List<Integer> komentari = new ArrayList<>();
    private int line = 0;
    private int col = 0;

    private int startLine = 0;
    private int startCol = 0;

    public ScannerCore(String[] src) {
        this.src = src;
        comments(src);
    }

    public void comments(String[] lines){
        for (int i = 0; i < lines.length; i++) {
            if(!lines[i].trim().isEmpty()) {
                if (lines[i].trim().charAt(lines[i].trim().length() - 1) != '|' || lines[i].trim().charAt(0) != '|') {
                    //System.out.println("JESTE, linija : " + i + " karakter : " + lines[i].charAt(0) + " karakter : " + lines[i].charAt(lines[i].length() - 2));
                    komentari.add(i);
                }
            }
        }
    }


    public boolean isAtEnd() {
        return line >= src.length || (line == src.length - 1 && col >= src[line].length());
    }

    public char peek() {
        if (isAtEnd()) return '\0';
        if (col >= src[line].length()) return '\n'; // prelazak na novu liniju
        return src[line].charAt(col);
    }

    public char peekNext() {
        if (isAtEnd()) return '\0';
        if (col + 1 < src[line].length()) return src[line].charAt(col + 1);
        else if (line + 1 < src.length) return '\n'; // sledeća linija
        else return '\0';
    }

    public char advance() {
        if (isAtEnd()) return '\0';
        char c;
        if (col >= src[line].length()) {
            // prelazak u novu liniju
            c = '\n';
            line++;
            col = 0;
        }
        else if(komentari.contains(line)){
            line++;
            col = 0;
            c = ' ';
        }
        else {
            c = src[line].charAt(col++);
        }
        return c;
    }

    public void beginToken() {
        startLine = line;
        startCol = col;
    }

    public int getLine() { return line + 1; }
    public int getCol() { return col + 1; }
    public int getStartLine() { return startLine + 1; }
    public int getStartCol() { return startCol + 1; }
}
