package DigipuffLearnsJava;

public enum CompSym {
    EQUAL_TO("="),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_THAN_OR_EQUAL_TO(">="),
    LESS_THAN_OR_EQUAL_TO("<="),;

    private String value;

    CompSym(String value) {
        this.value = value;
    }

    public static CompSym fromValue(String value) {
        for(CompSym name: CompSym.values()) {
            if(name.value.equals(value)) return name;
        }
        return null;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
