package core;

import debug.Debugger;

import java.util.*;

public class Rewriter {
    private final RuleSet ruleSet;
    private final Debugger debugger;
    private final Set<Term> seenTerms = new HashSet<>();

    public Rewriter(RuleSet ruleSet, Debugger debugger) {
        this.ruleSet = ruleSet;
        this.debugger = debugger;
    }

    public Term rewrite(Term term) {
        seenTerms.clear();
        if (debugger.getMode() == Debugger.Mode.DEBUG) {
            System.out.println("DEBUG: Starting rewrite of: " + term);
        }
        return rewriteInternal(term);
    }

    private Term rewriteInternal(Term term) {
        if (seenTerms.contains(term)) {
            throw new IllegalStateException("Cycle detected in rewriting: " + term);
        }
        seenTerms.add(term);

        debugger.onStepStart(term);

        // Try rewriting FIRST (before evaluation)
        Term rewritten = rewriteOnce(term);
        if (!rewritten.equals(term)) {
            seenTerms.remove(term);
            return rewriteInternal(rewritten);
        }

        // Try evaluation if no rewriting was possible
        Term evaluated = Evaluator.evaluate(term);
        if (!evaluated.equals(term)) {
            debugger.onEvaluation(term, evaluated);
            seenTerms.remove(term);
            return rewriteInternal(evaluated);
        }

        // No more rewrites possible
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

                // Use debug matching only in debug mode
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

            // Try rewriting subterms
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