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

    public int removeRule(String fullName) {
        int removedCount = 0;

        // Remove from allRules list
        Iterator<Rule> allRulesIterator = allRules.iterator();
        while (allRulesIterator.hasNext()) {
            Rule rule = allRulesIterator.next();
            if (rule.fullName().equals(fullName)) {
                allRulesIterator.remove();
                removedCount++;
            }
        }

        // Remove from function index
        Iterator<Map.Entry<String, java.util.List<Rule>>> functionIterator = rulesByFunction.entrySet().iterator();
        while (functionIterator.hasNext()) {
            Map.Entry<String, java.util.List<Rule>> entry = functionIterator.next();
            java.util.List<Rule> rules = entry.getValue();
            rules.removeIf(rule -> rule.fullName().equals(fullName));

            // Remove empty lists from function index
            if (rules.isEmpty()) {
                functionIterator.remove();
            }
        }

        return removedCount;
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