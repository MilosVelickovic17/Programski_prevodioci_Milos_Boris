package parser;

public final class Types {
    public enum Base { INT, FLOAT, DOUBLE, CHAR, BOOL, STRING }

    public static final class Type {
        public final Base base;
        public Type(Base b){ this.base=b; }
        @Override public String toString(){ return base.name(); }
    }

    public static final class ReturnType {
        public final boolean isVoid; public final Type type; // ako !isVoid
        private ReturnType(boolean isVoid, Type t){ this.isVoid=isVoid; this.type=t; }
        public static ReturnType VOID(){ return new ReturnType(true, null); }
        public static ReturnType of(Type t){ return new ReturnType(false, t); }
        @Override public String toString(){ return isVoid? "VOID" : type.toString(); }
    }
}

