
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
            System.out.println("DEBUG: Starting rewrite of: " + term);
        }
        return rewriteInternal(term);
    }

    private Term rewriteInternal(Term term) {
        if (++stepCount > MAX_STEPS) {
            throw new IllegalStateException("Rewrite step limit exceeded (mögliche Nichtterminierung durch Regeln).");
        }

        if (seenTerms.contains(term)) {
            throw new IllegalStateException("Cycle detected in rewriting: " + term);
        }
        seenTerms.add(term);

        debugger.onStepStart(term);

        Term rewritten = rewriteOnce(term);
        if (!rewritten.equals(term)) {
            seenTerms.remove(term);
            return rewriteInternal(rewritten);
        }

        Term evaluated = Evaluator.evaluate(term);
        if (!evaluated.equals(term)) {
            debugger.onEvaluation(term, evaluated);
            seenTerms.remove(term);
            return rewriteInternal(evaluated);
        }

        debugger.onStepEnd(term);
        seenTerms.remove(term);
        return term;
    }

    private static boolean isNegativeNumber(Term t) {
        if (t instanceof Term.Atom a && a.isNumber()) {
            try {
                return a.asDouble() < 0;
            } catch (NumberFormatException ignored) { /* nicht numerisch */ }
        }
        return false;
    }

    private Term rewriteOnce(Term term) {
        if (debugger.getMode() == Debugger.Mode.DEBUG) {
            System.out.println("DEBUG: rewriteOnce called with: " + term);
        }

        if (term instanceof Term.List list && !list.isEmpty()) {
            String functionSymbol = list.getFunctionSymbol();

            // DOMAIN GUARDS: Verhindere nicht-terminierende Umschreibungen
            if ("pow".equals(functionSymbol)) {
                if (list.elements().size() >= 3) {
                    Term exp = list.elements().get(2);
                    if (isNegativeNumber(exp)) {
                        throw new IllegalArgumentException("pow mit negativem Exponenten wird nicht unterstützt: " + term);
                    }
                }
            }
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
            if ("take".equals(functionSymbol) || "drop".equals(functionSymbol)) {
                if (list.elements().size() >= 2) {
                    Term n = list.elements().get(1);
                    if (isNegativeNumber(n)) {
                        throw new IllegalArgumentException(functionSymbol + " mit negativem n ist nicht definiert: " + term);
                    }
                }
            }

            if (debugger.getMode() == Debugger.Mode.DEBUG) {
                System.out.println("DEBUG: Function symbol: '" + functionSymbol + "'");
            }

            var rulesForFunction = ruleSet.getRulesForFunction(functionSymbol);
            if (debugger.getMode() == Debugger.Mode.DEBUG) {
                System.out.println("DEBUG: Found " + rulesForFunction.size() + " rules for function '" + functionSymbol + "'");
            }

            for (Rule rule : rulesForFunction) {
                if (debugger.getMode() == Debugger.Mode.DEBUG) {
                    System.out.println("DEBUG: Trying rule: " + rule);
                    System.out.println("DEBUG: Pattern: " + rule.pattern());
                    System.out.println("DEBUG: Term: " + term);
                }

                Optional<Map<String, Term>> match =
                        debugger.getMode() == Debugger.Mode.DEBUG ?
                                RuleMatcher.matchDebug(rule.pattern(), term) :
                                RuleMatcher.match(rule.pattern(), term);

                if (match.isPresent()) {
                    if (debugger.getMode() == Debugger.Mode.DEBUG) {
                        System.out.println("DEBUG: Rule matched!");
                    }
                    Term result = RuleMatcher.substitute(rule.replacement(), match.get());
                    debugger.onRuleApplied(rule, term, result);

                    if (!debugger.shouldContinue()) {
                        return term;
                    }

                    return result;
                } else {
                    if (debugger.getMode() == Debugger.Mode.DEBUG) {
                        System.out.println("DEBUG: Rule did not match");
                    }
                }
            }

            java.util.List<Term> newElements = new ArrayList<>();
            boolean changed = false;

            for (Term element : list.elements()) {
                Term rewrittenElement = rewriteInternal(element); // Use rewriteInternal for subterms
                newElements.add(rewrittenElement);
                if (!rewrittenElement.equals(element)) {
                    changed = true;
                }
            }

            if (changed) {
                return new Term.List(newElements);
            }
        }

        return term;
    }
}