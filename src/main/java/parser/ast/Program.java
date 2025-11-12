package parser.ast;

import java.util.List;
import com.google.gson.annotations.Expose;

public class Program extends Node {
    public final List<Stmt> topLevel;
    public Program(List<Stmt> topLevel) { this.topLevel = topLevel; }
}
