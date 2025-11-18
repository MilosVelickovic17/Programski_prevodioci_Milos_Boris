package parser.ast;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Node {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    public final transient int id;

    public Node() {
        this.id = COUNTER.incrementAndGet();
    }

    public List<Node> getChildren() {
        return Collections.emptyList();
    }
}
