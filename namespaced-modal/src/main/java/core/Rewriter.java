
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

    private Term rewriteOnce(Term term) {
        if (debugger.getMode() == Debugger.Mode.DEBUG) {
            System.out.println("DEBUG: rewriteOnce called with: " + term);
        }

        if (term instanceof Term.List list && !list.isEmpty()) {
            String functionSymbol = list.getFunctionSymbol();

            for (int i = 0; i < list.elements().size(); i++) {
                Term element = list.elements().get(i);
                Term rewrittenElement = rewriteOnce(element);
                if (!rewrittenElement.equals(element)) {
                    java.util.List<Term> newElements = new ArrayList<>(list.elements());
                    newElements.set(i, rewrittenElement);
                    return new Term.List(newElements);
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
        }

        return term;
    }
}