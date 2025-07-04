package engine;

import core.Rule;
import core.Term;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class RuleManager {
    private List<Rule> rules;

    public RuleManager() {
        this.rules = new ArrayList<>();
    }

    public void addRule(Rule rule) {
        rules.add(rule);
    }

    public void addRule(String name, Term pattern, Term replacement) {
        rules.add(new Rule(name, pattern, replacement));
    }

    public void addRule(Term pattern, Term replacement) {
        rules.add(new Rule(pattern, replacement));
    }

    public boolean removeRule(Term pattern) {
        Iterator<Rule> iterator = rules.iterator();
        while (iterator.hasNext()) {
            Rule rule = iterator.next();
            if (rule.getPattern().equals(pattern)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public Rule findMatchingRule(Term term) {
        for (Rule rule : rules) {
            if (couldMatch(rule.getPattern(), term)) {
                return rule;
            }
        }
        return null;
    }

    private boolean couldMatch(Term pattern, Term term) {
        if (pattern.isVariable()) {
            return true;
        }

        if (pattern.isAtom() && term.isAtom()) {
            return pattern.getValue().equals(term.getValue());
        }

        if (pattern.isList() && term.isList()) {
            if (pattern.size() != term.size()) {
                return false;
            }

            for (int i = 0; i < pattern.size(); i++) {
                if (!couldMatch(pattern.getChildren().get(i), term.getChildren().get(i))) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    public List<Rule> getAllRules() {
        return new ArrayList<>(rules);
    }

    public int getRuleCount() {
        return rules.size();
    }

    public void clear() {
        rules.clear();
    }

    public List<Rule> getUnusedRules() {
        List<Rule> unused = new ArrayList<>();
        for (Rule rule : rules) {
            if (rule.getReferences() == 0) {
                unused.add(rule);
            }
        }
        return unused;
    }
}