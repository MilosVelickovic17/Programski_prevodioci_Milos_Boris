package parser.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.annotations.Expose;

public abstract class Node {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    public final int id;

    public Node() {
        this.id = COUNTER.incrementAndGet();
    }

    public List<Node> getChildren() {
        return null;
    }


}
