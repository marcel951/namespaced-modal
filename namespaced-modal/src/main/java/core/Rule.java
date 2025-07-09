package core;

public record Rule(String namespace, String name, Term pattern, Term replacement) {

    public String fullName() {
        return namespace + "." + name;
    }

    public boolean matches(String functionSymbol) {
        return name.equals(functionSymbol);
    }

    @Override
    public String toString() {
        return "<" + fullName() + "> " + pattern + " " + replacement;
    }
}