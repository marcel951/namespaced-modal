package core;

import debug.Debugger;

import java.util.*;

public class Rewriter {
    private final RuleSet ruleSet;
    private final Debugger debugger;
    private final Set<Term> seenTerms = Collections.newSetFromMap(new IdentityHashMap<>());

    private static final int MAX_STEPS = 10000;
    private int stepCount = 0;

    public Rewriter(RuleSet ruleSet, Debugger debugger) {
        this.ruleSet = ruleSet;
        this.debugger = debugger;
    }

    public Term rewrite(Term term) {
        seenTerms.clear();
        stepCount = 0;
        if (debugger.getMode() == Debugger.Mode.DEBUG) {
            System.out.println("DEBUG: Starting outermost rewrite of: " + term);
        }
        return rewriteOutermost(term);
    }

    private Term rewriteOutermost(Term term) {
        if (++stepCount > MAX_STEPS) {
            throw new IllegalStateException("Rewrite step limit exceeded (mögliche Nichtterminierung durch Regeln).");
        }

        if (seenTerms.contains(term)) {
            throw new IllegalStateException("Cycle detected in rewriting: " + term);
        }
        seenTerms.add(term);

        debugger.onStepStart(term);

        Term current = term;
        boolean changed = true;

        while (changed) {
            changed = false;

            // SCHRITT 1: Versuche Regeln auf äußerem Term
            Term ruleResult = tryApplyRules(current);
            if (!ruleResult.equals(current)) {
                current = ruleResult;
                changed = true;
                continue;
            }

            // SCHRITT 2: Versuche direkte Evaluation
            Term evalResult = tryDirectEvaluation(current);
            if (!evalResult.equals(current)) {
                current = evalResult;
                changed = true;
                continue;
            }

            // SCHRITT 3: Gehe zu Untertermen
            Term subResult = rewriteSubterms(current);
            if (!subResult.equals(current)) {
                current = subResult;
                changed = true;
            }
        }

        debugger.onStepEnd(current);
        seenTerms.remove(term);
        return current;
    }

    private Term tryDirectEvaluation(Term term) {
        if (!(term instanceof Term.List list) || list.isEmpty()) {
            return term;
        }

        Term head = list.head();
        if (head instanceof Term.Atom atom && ":".equals(atom.value())) {
            return evaluateArithmetic(list);
        }

        return term;
    }

    private Term evaluateArithmetic(Term.List list) {
        if (list.elements().size() != 4) {
            throw new IllegalArgumentException("Arithmetic evaluation requires exactly 4 elements (: op arg1 arg2)");
        }

        Term operator = list.elements().get(1);
        Term arg1 = list.elements().get(2);
        Term arg2 = list.elements().get(3);

        if (!(operator instanceof Term.Atom opAtom)) {
            throw new IllegalArgumentException("Operator must be an atom");
        }

        return evaluateBinaryOp(opAtom.value(), arg1, arg2);
    }

    private Term evaluateBinaryOp(String op, Term arg1, Term arg2) {
        if (!(arg1 instanceof Term.Atom a1) || !(arg2 instanceof Term.Atom a2)) {
            throw new IllegalArgumentException("Arithmetic arguments must be atoms: " + arg1 + ", " + arg2);
        }

        switch (op) {
            case "=" -> {
                return Term.bool(arg1.equals(arg2));
            }
            case "==" -> {
                return Term.bool(arg1.equals(arg2));
            }
            case "!=" -> {
                return Term.bool(!arg1.equals(arg2));
            }
        }

        if (!a1.isNumber() || !a2.isNumber()) {
            throw new IllegalArgumentException("Arithmetic arguments must be numbers: " + arg1 + ", " + arg2);
        }

        double val1 = a1.asDouble();
        double val2 = a2.asDouble();

        double result = switch (op) {
            case "+" -> val1 + val2;
            case "-" -> val1 - val2;
            case "*" -> val1 * val2;
            case "/" -> {
                if (val2 == 0.0) throw new ArithmeticException("Division by zero");
                yield val1 / val2;
            }
            case "%" -> {
                if (val2 == 0.0) throw new ArithmeticException("Division by zero");
                yield val1 % val2;
            }
            case "mod" -> {
                if (val2 == 0.0) throw new ArithmeticException("Division by zero");
                yield val1 % val2;
            }
            case ">" -> (val1 > val2) ? 1.0 : 0.0;
            case "<" -> (val1 < val2) ? 1.0 : 0.0;
            case ">=" -> (val1 >= val2) ? 1.0 : 0.0;
            case "<=" -> (val1 <= val2) ? 1.0 : 0.0;
            default -> throw new IllegalArgumentException("Unknown operator: " + op);
        };

        if (op.equals(">") || op.equals("<") || op.equals(">=") || op.equals("<=")) {
            return Term.bool(result != 0.0);
        }

        return Term.number(result);
    }

    private Term tryApplyRules(Term term) {
        if (!(term instanceof Term.List list) || list.isEmpty()) {
            return term;
        }

        String functionSymbol = list.getFunctionSymbol();
        validateDomainConstraints(term, functionSymbol, list);

        var rulesForFunction = ruleSet.getRulesForFunction(functionSymbol);

        for (Rule rule : rulesForFunction) {
            Optional<Map<String, Term>> match = RuleMatcher.match(rule.pattern(), term);

            if (match.isPresent()) {
                // KRITISCH: Evaluiere Bindings vor Substitution!
                Map<String, Term> evaluatedBindings = new HashMap<>();
                for (Map.Entry<String, Term> entry : match.get().entrySet()) {
                    Term evaluatedValue = rewriteOutermost(entry.getValue()); // Rekursiv evaluieren!
                    evaluatedBindings.put(entry.getKey(), evaluatedValue);
                }

                Term result = RuleMatcher.substitute(rule.replacement(), evaluatedBindings);
                debugger.onRuleApplied(rule, term, result);

                if (!debugger.shouldContinue()) {
                    return term;
                }

                return result;
            }
        }

        return term;
    }

    private Term rewriteSubterms(Term term) {
        if (!(term instanceof Term.List list) || list.isEmpty()) {
            return term;
        }

        java.util.List<Term> newElements = new ArrayList<>();
        boolean changed = false;

        for (Term element : list.elements()) {
            Term rewrittenElement = rewriteOutermost(element);
            newElements.add(rewrittenElement);
            if (!rewrittenElement.equals(element)) {
                changed = true;
            }
        }

        if (changed) {
            return new Term.List(newElements);
        }

        return term;
    }

    private static boolean isNegativeNumber(Term t) {
        if (t instanceof Term.Atom a && a.isNumber()) {
            try {
                return a.asDouble() < 0;
            } catch (NumberFormatException ignored) {
            }
        }
        return false;
    }

    private void validateDomainConstraints(Term term, String functionSymbol, Term.List list) {
        if ("fact".equals(functionSymbol)) {
            if (list.elements().size() >= 2) {
                Term n = list.elements().get(1);
                if (isNegativeNumber(n)) {
                    throw new IllegalArgumentException("factorial ist für negative Eingaben nicht definiert: " + term);
                }
            }
        }
        if ("fib".equals(functionSymbol)) {
            if (list.elements().size() >= 2) {
                Term n = list.elements().get(1);
                if (isNegativeNumber(n)) {
                    throw new IllegalArgumentException("fibonacci ist für negative Eingaben nicht definiert: " + term);
                }
            }
        }
    }
}