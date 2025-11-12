package application;

import lexer.Lexer;
import lexer.token.Token;
import lexer.token.TokenFormatter;
import parser.Parser;
import parser.PrettyPrinter;
import parser.ast.Program;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Application {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java application.Application <source-file>");
            System.exit(64);
        }

        try {
            // Učitaj iz fajla
            String code = Files.readString(Path.of(args[0]));
            String[] lines = code.split("\n");

            // 1️⃣ Lexer
            Lexer lexer = new Lexer(lines);
            List<Token> tokens = lexer.scanTokens();
            System.out.println(TokenFormatter.formatList(tokens));

            // 2️⃣ Parser
            Parser parser = new Parser(tokens);
            Program program = parser.parseProgram();

            // 3️⃣ AST ispis
            PrettyPrinter printer = new PrettyPrinter();
            System.out.println("\n=== ABSTRACT SYNTAX TREE ===");
            System.out.println(printer.printAst(program));

            // 4️⃣ JSON ispis
            System.out.println("\n=== JSON AST ===");
            System.out.println(printer.toJson(program));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
