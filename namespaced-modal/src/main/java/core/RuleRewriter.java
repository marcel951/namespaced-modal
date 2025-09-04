package core;

import debug.Debugger;
import java.util.*;

public class RuleRewriter {
    private final RuleSet ruleSet;
    private final Debugger debugger;

    public RuleRewriter(RuleSet ruleSet, Debugger debugger) {
        this.ruleSet = ruleSet;
        this.debugger = debugger;
    }

    public Optional<Term> tryRewrite(Term term) {
        if (!(term instanceof Term.List list) || list.isEmpty()) {
            return Optional.empty();
        }

        String funcSymbol = list.getFunctionSymbol();
        var rules = ruleSet.getRulesForFunction(funcSymbol);

        for (Rule rule : rules) {
            if (isBaseCase(rule)) {
                Optional<Map<String, Term>> match = RuleMatcher.match(rule.pattern(), term);
                if (match.isPresent()) {
                    Term result = RuleMatcher.substitute(rule.replacement(), match.get());

                    // WICHTIG: Sicherstellen dass onRuleApplied aufgerufen wird
                    debugger.onRuleApplied(rule, term, result);

                    return Optional.of(result);
                }
            }
        }


        for (Rule rule : rules) {
            if (!isBaseCase(rule)) {
                Optional<Map<String, Term>> match = RuleMatcher.match(rule.pattern(), term);
                if (match.isPresent()) {
                    Term result = RuleMatcher.substitute(rule.replacement(), match.get());
                    debugger.onRuleApplied(rule, term, result);
                    return Optional.of(result);
                }
            }
        }

        return Optional.empty();
    }

    private boolean isBaseCase(Rule rule) {
        String ruleId = rule.namespace() + "." + rule.name();
        if (ruleId.contains("base") || ruleId.contains("empty") || ruleId.contains("zero")) {
            return true;
        }

        String pattern = rule.pattern().toString();
        if (pattern.matches(".*\\b[0-9]+\\b.*") || pattern.contains("()")) {
            return true;
        }

        String replacement = rule.replacement().toString();
        if (rule.pattern() instanceof Term.List patternList && !patternList.isEmpty()) {
            String funcName = patternList.getFunctionSymbol();
            return !replacement.contains(funcName);
        }

        return false;
    }
}