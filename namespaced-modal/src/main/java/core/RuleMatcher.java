package core;

import java.util.*;

public class RuleMatcher {

    private static boolean DEBUG = false;

    public static Optional<Map<String, Term>> match(Term pattern, Term term) {
        Map<String, Term> bindings = new HashMap<>();
        if (DEBUG) System.out.println("MATCH: Trying to match pattern " + pattern + " with term " + term);
        if (matchInternal(pattern, term, bindings)) {
            if (DEBUG) System.out.println("MATCH: Success with bindings " + bindings);
            return Optional.of(bindings);
        }
        if (DEBUG) System.out.println("MATCH: Failed");
        return Optional.empty();
    }

    public static Optional<Map<String, Term>> matchDebug(Term pattern, Term term) {
        DEBUG = true;
        Optional<Map<String, Term>> result = match(pattern, term);
        DEBUG = false;
        return result;
    }

    private static boolean matchInternal(Term pattern, Term term, Map<String, Term> bindings) {
        if (DEBUG) System.out.println("MATCH_INTERNAL: " + pattern + " vs " + term);

        switch (pattern) {
            case Term.Atom atom -> {
                if (atom.isVariable()) {
                    String var = atom.value();
                    if (bindings.containsKey(var)) {
                        boolean matches = bindings.get(var).equals(term);
                        if (DEBUG) System.out.println("MATCH_INTERNAL: Variable " + var + " already bound to " + bindings.get(var) + ", matches: " + matches);
                        return matches;
                    } else {
                        bindings.put(var, term);
                        if (DEBUG) System.out.println("MATCH_INTERNAL: Binding variable " + var + " to " + term);
                        return true;
                    }
                } else {
                    boolean matches = pattern.equals(term);
                    if (DEBUG) System.out.println("MATCH_INTERNAL: Atom " + pattern + " matches " + term + ": " + matches);
                    return matches;
                }
            }
            case Term.List patternList -> {
                if (!(term instanceof Term.List termList)) {
                    if (DEBUG) System.out.println("MATCH_INTERNAL: Pattern is list but term is not");
                    return false;
                }

                if (isListPattern(patternList)) {
                    if (DEBUG) System.out.println("MATCH_INTERNAL: Using list pattern matching");
                    return matchListPattern(patternList, termList, bindings);
                } else {
                    if (DEBUG) System.out.println("MATCH_INTERNAL: Using regular list matching");
                    return matchRegularList(patternList, termList, bindings);
                }
            }
        }
    }

    private static boolean isListPattern(Term.List list) {
        return list.elements().size() == 3 &&
                list.elements().get(1) instanceof Term.Atom atom &&
                ".".equals(atom.value());
    }

    private static boolean matchListPattern(Term.List pattern, Term.List term, Map<String, Term> bindings) {
        if (term.isEmpty()) {
            if (DEBUG) System.out.println("MATCH_LIST: Term is empty, cannot match list pattern");
            return false;
        }

        Term headPattern = pattern.elements().get(0);
        Term tailPattern = pattern.elements().get(2);

        if (!matchInternal(headPattern, term.head(), bindings)) {
            if (DEBUG) System.out.println("MATCH_LIST: Head pattern did not match");
            return false;
        }

        return matchInternal(tailPattern, term.tail(), bindings);
    }

    private static boolean matchRegularList(Term.List pattern, Term.List term, Map<String, Term> bindings) {
        if (pattern.elements().size() != term.elements().size()) {
            if (DEBUG) System.out.println("MATCH_REGULAR: Size mismatch: pattern=" + pattern.elements().size() + ", term=" + term.elements().size());
            return false;
        }

        for (int i = 0; i < pattern.elements().size(); i++) {
            if (!matchInternal(pattern.elements().get(i), term.elements().get(i), bindings)) {
                if (DEBUG) System.out.println("MATCH_REGULAR: Element " + i + " did not match");
                return false;
            }
        }

        return true;
    }

    public static Term substitute(Term template, Map<String, Term> bindings) {
        switch (template) {
            case Term.Atom atom -> {
                if (atom.isVariable() && bindings.containsKey(atom.value())) {
                    return bindings.get(atom.value());
                }
                return template;
            }
            case Term.List list -> {
                java.util.List<Term> newElements = new ArrayList<>();
                for (Term element : list.elements()) {
                    newElements.add(substitute(element, bindings));
                }
                return new Term.List(newElements);
            }
        }
    }
}