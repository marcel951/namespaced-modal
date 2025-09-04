package core;

import debug.Debugger;
import java.util.*;

public class TermEvaluator {
    private final RuleRewriter rewriter;
    private final SpecialOperatorEvaluator specialEvaluator;
    private final Debugger debugger;

    private final Map<Term, Term> memoCache = new HashMap<>();
    private final Set<Term> activeEvaluations = new HashSet<>();

    public TermEvaluator(RuleSet ruleSet, Debugger debugger) {
        this.rewriter = new RuleRewriter(ruleSet, debugger);
        this.specialEvaluator = new SpecialOperatorEvaluator();
        this.debugger = debugger;
    }

    public Term evaluate(Term term) {
        memoCache.clear();
        activeEvaluations.clear();

        if (debugger.getMode() == Debugger.Mode.DEBUG) {
            System.out.println("DEBUG: Starting evaluation of: " + term);
        }

        Term result = evaluateRecursive(term);


        if (result instanceof Term.Cons cons) {
            var asList = cons.toList();
            if (asList.isPresent()) {
                return asList.get();
            }
        }

        return result;
    }

    private Term evaluateRecursive(Term term) {
        if (memoCache.containsKey(term)) {
            return memoCache.get(term);
        }

        if (activeEvaluations.contains(term)) {
            return term;
        }

        activeEvaluations.add(term);
        try {
            Term result = evaluateCore(term);
            if (!result.equals(term)) {
                memoCache.put(term, result);
            }
            return result;
        } finally {
            activeEvaluations.remove(term);
        }
    }

    private Term evaluateCore(Term term) {
        if (term instanceof Term.Atom) {
            return term;
        }

        if (!(term instanceof Term.List list) || list.isEmpty()) {
            return term;
        }

        debugger.onStepStart(term);

        String funcSymbol = list.getFunctionSymbol();

        if (specialEvaluator.isSpecialOperator(funcSymbol)) {
            Term result = specialEvaluator.evaluate(list, this);
            debugger.onStepEnd(result);
            return result;
        }

        Optional<Term> rewriteResult = rewriter.tryRewrite(list);
        if (rewriteResult.isPresent()) {
            Term result = evaluateRecursive(rewriteResult.get());
            debugger.onStepEnd(result);
            return result;
        }

        Term result = evaluateSubterms(list);
        debugger.onStepEnd(result);
        return result;
    }

    private Term evaluateSubterms(Term.List list) {
        List<Term> newElements = new ArrayList<>();
        boolean changed = false;

        for (Term element : list.elements()) {
            Term evaluatedElement = evaluateRecursive(element);
            newElements.add(evaluatedElement);
            if (!evaluatedElement.equals(element)) {
                changed = true;
            }
        }

        return changed ? new Term.List(newElements) : list;
    }
}