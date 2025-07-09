package core;

import java.util.*;

public class RuleSet {
    private final Map<String, java.util.List<Rule>> rulesByFunction = new HashMap<>();
    private final java.util.List<Rule> allRules = new ArrayList<>();

    public void addRule(Rule rule) {
        allRules.add(rule);

        // Extract function symbol from pattern
        String functionSymbol = extractFunctionSymbol(rule.pattern());
        if (functionSymbol != null) {
            rulesByFunction.computeIfAbsent(functionSymbol, k -> new ArrayList<>()).add(rule);
        }
    }

    private String extractFunctionSymbol(Term pattern) {
        if (pattern instanceof Term.List list && !list.isEmpty()) {
            return list.getFunctionSymbol();
        }
        return null;
    }

    public java.util.List<Rule> getRulesForFunction(String functionSymbol) {
        return rulesByFunction.getOrDefault(functionSymbol, Collections.emptyList());
    }

    public java.util.List<Rule> getAllRules() {
        return Collections.unmodifiableList(allRules);
    }

    public java.util.List<Rule> getRulesForNamespace(String namespace) {
        return allRules.stream()
                .filter(rule -> rule.namespace().equals(namespace))
                .toList();
    }

    public Set<String> getNamespaces() {
        return allRules.stream()
                .map(Rule::namespace)
                .collect(java.util.stream.Collectors.toSet());
    }

    public int size() {
        return allRules.size();
    }

    // Debug method to see function indexing
    public Map<String, java.util.List<Rule>> getFunctionIndex() {
        return Collections.unmodifiableMap(rulesByFunction);
    }
}