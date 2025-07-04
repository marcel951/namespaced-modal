package engine;

import core.Rule;
import core.Term;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Rewriter {
    private RuleManager ruleManager;
    private Map<String, Term> bindings;

    public Rewriter(RuleManager ruleManager) {
        this.ruleManager = ruleManager;
        this.bindings = new HashMap<>();
    }

    public Term rewrite(Term term) {
        return rewriteStep(term);
    }

    public Term rewriteMultiple(Term term, int maxSteps) {
        Term current = term;
        for (int i = 0; i < maxSteps; i++) {
            Term next = rewriteStep(current);
            if (next.equals(current)) {
                break;
            }
            current = next;
        }
        return current;
    }

    private Term rewriteStep(Term term) {
        if (term.isAtom()) {
            return term;
        }

        // Versuche Regel auf den ganzen Term anzuwenden
        Rule matchingRule = findMatchingRule(term);
        if (matchingRule != null) {
            bindings.clear();
            if (match(matchingRule.getPattern(), term)) {
                matchingRule.incrementReferences();
                return substitute(matchingRule.getReplacement());
            }
        }

        // Falls keine Regel passt, versuche Kinder zu rewriten
        List<Term> newChildren = new ArrayList<>();
        boolean changed = false;

        for (Term child : term.getChildren()) {
            Term newChild = rewriteStep(child);
            newChildren.add(newChild);
            if (!newChild.equals(child)) {
                changed = true;
            }
        }

        return changed ? new Term(newChildren) : term;
    }

    private Rule findMatchingRule(Term term) {
        for (Rule rule : ruleManager.getAllRules()) {
            bindings.clear();
            if (match(rule.getPattern(), term)) {
                return rule;
            }
        }
        return null;
    }

    private boolean match(Term pattern, Term term) {
        if (pattern.isVariable()) {
            String varName = pattern.getVariableName();
            if (bindings.containsKey(varName)) {
                return bindings.get(varName).equals(term);
            } else {
                bindings.put(varName, term);
                return true;
            }
        }

        if (pattern.isAtom() && term.isAtom()) {
            return pattern.getValue().equals(term.getValue());
        }

        if (pattern.isList() && term.isList()) {
            if (pattern.size() != term.size()) {
                return false;
            }

            for (int i = 0; i < pattern.size(); i++) {
                if (!match(pattern.getChildren().get(i), term.getChildren().get(i))) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    private Term substitute(Term template) {
        if (template.isVariable()) {
            String varName = template.getVariableName();
            Term binding = bindings.get(varName);
            return binding != null ? binding : template;
        }

        if (template.isAtom()) {
            return template;
        }

        List<Term> newChildren = new ArrayList<>();
        for (Term child : template.getChildren()) {
            newChildren.add(substitute(child));
        }

        return new Term(newChildren);
    }
}