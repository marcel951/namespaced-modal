package core;

import java.util.*;

/**
 * Pattern matching following classical term rewriting systems
 * Supports both list patterns and cons patterns
 */
public class RuleMatcher {

    public static Optional<Map<String, Term>> match(Term pattern, Term term) {
        Map<String, Term> bindings = new HashMap<>();
        if (matchInternal(pattern, term, bindings)) {
            return Optional.of(bindings);
        }
        return Optional.empty();
    }

    public static Optional<Map<String, Term>> matchDebug(Term pattern, Term term) {
        System.out.println("DEBUG MATCH: pattern=" + pattern + ", term=" + term);
        Optional<Map<String, Term>> result = match(pattern, term);
        System.out.println("DEBUG RESULT: " + result);
        return result;
    }

    private static boolean matchInternal(Term pattern, Term term, Map<String, Term> bindings) {
        // Variable binding
        if (pattern instanceof Term.Atom patAtom && patAtom.isVariable()) {
            String varName = patAtom.value();
            if (bindings.containsKey(varName)) {
                return bindings.get(varName).equals(term);
            }
            bindings.put(varName, term);
            return true;
        }

        // Atom matching
        if (pattern instanceof Term.Atom patAtom && term instanceof Term.Atom termAtom) {
            return patAtom.value().equals(termAtom.value());
        }

        // List vs List matching
        if (pattern instanceof Term.List patList && term instanceof Term.List termList) {
            return matchList(patList, termList, bindings);
        }

        // Cons vs Cons matching
        if (pattern instanceof Term.Cons patCons && term instanceof Term.Cons termCons) {
            return matchInternal(patCons.car(), termCons.car(), bindings) &&
                    matchInternal(patCons.cdr(), termCons.cdr(), bindings);
        }

        // List vs Cons matching (convert cons to list if proper)
        if (pattern instanceof Term.List patList && term instanceof Term.Cons termCons) {
            Optional<Term.List> termAsList = termCons.toList();
            if (termAsList.isPresent()) {
                return matchList(patList, termAsList.get(), bindings);
            }
        }

        // Cons pattern vs List term
        if (pattern instanceof Term.Cons patCons && term instanceof Term.List termList) {
            if (termList.isEmpty()) {
                return false; // Can't match cons against empty list
            }
            // Convert list to cons structure
            Term termAsCons = listToCons(termList);
            return matchInternal(pattern, termAsCons, bindings);
        }

        return false;
    }

    private static boolean matchList(Term.List pattern, Term.List term, Map<String, Term> bindings) {
        List<Term> patElements = pattern.elements();
        List<Term> termElements = term.elements();

        // NEUE LOGIK: Erkenne Cons-Pattern (?head . ?tail)
        if (patElements.size() == 3 &&
                patElements.get(1) instanceof Term.Atom dotAtom &&
                ".".equals(dotAtom.value())) {

            // Das ist ein Cons-Pattern in List-Form: (?head . ?tail)
            if (termElements.isEmpty()) {
                return false; // Leere Liste kann nicht gegen Cons-Pattern matchen
            }

            Term headPattern = patElements.get(0);
            Term tailPattern = patElements.get(2);

            // Match head
            Term termHead = termElements.get(0);
            if (!matchInternal(headPattern, termHead, bindings)) {
                return false;
            }

            // Match tail (Rest der Liste)
            Term termTail = termElements.size() == 1 ?
                    new Term.List() : // Leere Liste für letztes Element
                    new Term.List(termElements.subList(1, termElements.size()));

            return matchInternal(tailPattern, termTail, bindings);
        }

        // Standard List-Matching (exakte Länge)
        if (patElements.size() != termElements.size()) {
            return false;
        }

        for (int i = 0; i < patElements.size(); i++) {
            if (!matchInternal(patElements.get(i), termElements.get(i), bindings)) {
                return false;
            }
        }
        return true;
    }


    private static boolean isConsPattern(List<Term> elements) {
        // Prüfe ob das zweite Element ein Atom ist das wie ein Cons-Tail aussieht
        if (elements.size() != 2) return false;

        Term second = elements.get(1);
        // Heuristik: Wenn zweites Element Variable ist, könnte es Cons-Pattern sein
        // Aber wir brauchen bessere Erkennung...

        // EINFACHER: Prüfe auf "." in der String-Repräsentation
        String patternStr = "(" + elements.get(0) + " " + elements.get(1) + ")";
        // Das ist nicht optimal, aber funktional...

        return false; // Erstmal deaktiviert - andere Lösung nutzen
    }


    /**
     * Convert proper list to cons structure for pattern matching
     */
    private static Term listToCons(Term.List list) {
        if (list.isEmpty()) {
            return list;
        }

        List<Term> elements = list.elements();
        Term result = Term.nil();

        // Build cons structure from right to left
        for (int i = elements.size() - 1; i >= 0; i--) {
            result = Term.cons(elements.get(i), result);
        }

        return result;
    }

    public static Term substitute(Term template, Map<String, Term> bindings) {
        if (template instanceof Term.Atom atom && atom.isVariable()) {
            return bindings.getOrDefault(atom.value(), template);
        }

        if (template instanceof Term.List list) {
            List<Term> newElements = new ArrayList<>();
            for (Term element : list.elements()) {
                newElements.add(substitute(element, bindings));
            }
            return new Term.List(newElements);
        }

        if (template instanceof Term.Cons cons) {
            Term result = Term.cons(
                    substitute(cons.car(), bindings),
                    substitute(cons.cdr(), bindings)
            );

            // NEUE KONVERTIERUNG: Cons zu List wenn möglich
            if (result instanceof Term.Cons resultCons) {
                var asList = resultCons.toList();
                if (asList.isPresent()) {
                    return asList.get();
                }
            }

            return result;
        }

        return template;
    }

}