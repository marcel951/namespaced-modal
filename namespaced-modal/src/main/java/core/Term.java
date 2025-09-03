
package core;

import java.util.*;

/**
 * Term representation following classical Lisp/Scheme semantics
 * as described in SICP and other PL textbooks
 */
public sealed interface Term permits Term.Atom, Term.List, Term.Cons {

    record Atom(String value) implements Term {
        public boolean isNumber() {
            try {
                Double.parseDouble(value);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        public boolean isVariable() {
            return value.startsWith("?");
        }

        public boolean isBoolean() {
            return "true".equals(value) || "false".equals(value);
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

    /**
     * Proper list: finite sequence of elements
     * Represented as (e1 e2 ... en)
     */
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
            return "(" + String.join(" ",
                    elements.stream()
                            .map(Term::toString)
                            .toArray(String[]::new)) + ")";
        }
    }

    /**
     * Cons pair: (car . cdr) - fundamental building block
     * Can represent both proper lists and improper pairs
     */
    record Cons(Term car, Term cdr) implements Term {

        /**
         * Convert cons structure to proper list if possible
         */
        public Optional<List> toList() {
            java.util.List<Term> elements = new ArrayList<>();
            Term current = this;

            while (current instanceof Cons cons) {
                elements.add(cons.car);
                current = cons.cdr;
            }

            // Proper list ends with empty list
            if (current instanceof List list && list.isEmpty()) {
                return Optional.of(new List(elements));
            }

            return Optional.empty(); // Improper list
        }

        /**
         * Check if this cons represents a proper list
         */
        public boolean isProperList() {
            return toList().isPresent();
        }

        /**
         * Get length of proper list, -1 if improper
         */
        public int length() {
            return toList().map(list -> list.elements.size()).orElse(-1);
        }

        @Override
        public String toString() {
            Optional<List> asList = toList();
            if (asList.isPresent()) {
                // Display as proper list: (a b c)
                return asList.get().toString();
            } else {
                // Display as dotted pair: (a . b)
                return "(" + car + " . " + cdr + ")";
            }
        }
    }

    // Factory methods
    static Term atom(String value) {
        return new Atom(value);
    }

    static Term list(Term... elements) {
        return new List(elements);
    }

    static Term cons(Term car, Term cdr) {
        return new Cons(car, cdr);
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

    static Term nil() {
        return new List(); // Empty list
    }
}