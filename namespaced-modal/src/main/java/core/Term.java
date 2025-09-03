package core;

import java.util.*;

public sealed interface Term permits Term.Atom, Term.List {

    record Atom(String value) implements Term {
        public boolean isNumber() {
            try {
                Double.parseDouble(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        public boolean isInteger() {
            try {
                Integer.parseInt(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        public boolean isBoolean() {
            return "true".equals(value) || "false".equals(value);
        }

        public boolean isVariable() {
            return value.startsWith("?");
        }

        public int asNumber() {
            return Integer.parseInt(value);
        }

        public double asDouble() {
            return Double.parseDouble(value);
        }

        public boolean asBoolean() {
            return Boolean.parseBoolean(value);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    record List(java.util.List<Term> elements) implements Term {
        public List(Term... elements) {
            this(Arrays.asList(elements));
        }

        public List(Collection<Term> elements) {
            this(new ArrayList<>(elements));
        }

        public boolean isEmpty() {
            return elements.isEmpty();
        }

        public Term head() {
            if (isEmpty()) throw new IllegalStateException("Empty list has no head");
            return elements.get(0);
        }

        public List tail() {
            if (isEmpty()) throw new IllegalStateException("Empty list has no tail");
            return new List(elements.subList(1, elements.size()));
        }

        public String getFunctionSymbol() {
            if (isEmpty()) return "";
            Term head = head();
            if (head instanceof Atom atom) {
                return atom.value();
            }
            return "";
        }

        @Override
        public String toString() {
            // Prüfe ob es eine gepunktete Liste (Cons-Cell) ist
            if (isDottedList()) {
                return "(" + elements.get(0) + " . " + elements.get(2) + ")";
            }

            // Normale Listenformatierung
            return "(" + String.join(" ", elements.stream().map(Term::toString).toArray(String[]::new)) + ")";
        }

        private boolean isDottedList() {
            return elements.size() == 3 &&
                   elements.get(1) instanceof Term.Atom atom &&
                   ".".equals(atom.value());
        }
    }

    static Term atom(String value) {
        return new Atom(value);
    }

    static Term list(Term... elements) {
        return new List(elements);
    }

    static Term number(int value) {
        return new Atom(String.valueOf(value));
    }

    static Term number(double value) {
        if (value == (int) value) {
            return new Atom(String.valueOf((int) value));
        } else {
            return new Atom(String.format("%.6g", value));
        }
    }

    static Term bool(boolean value) {
        return new Atom(String.valueOf(value));
    }
    
}