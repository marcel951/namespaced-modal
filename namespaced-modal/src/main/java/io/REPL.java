
package io;

import core.*;
import debug.Debugger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import core.TermEvaluator;

public class REPL {
    private final RuleSet ruleSet;
    private final BufferedReader reader;
    private Debugger debugger;
    private TermEvaluator evaluator;

    public REPL(RuleSet ruleSet) {
        this.ruleSet = ruleSet;
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.debugger = new Debugger(Debugger.Mode.QUIET);
        this.evaluator = new TermEvaluator(ruleSet, debugger);
    }


    public void run() throws IOException {
        System.out.println("Namespaced-Modal Term-Rewriting Language");
        System.out.println("Loaded " + ruleSet.size() + " rules");
        System.out.println("Type :help for commands or :exit to quit");
        System.out.println();

        while (true) {
            System.out.print("> ");
            String line = reader.readLine();

            if (line == null || ":exit".equals(line.trim())) {
                System.out.println("Goodbye!");
                break;
            }

            try {
                if (line.trim().isEmpty()) {
                    continue;
                }

                if (line.startsWith(":")) {
                    handleCommand(line.trim());
                } else if (DynamicRuleParser.isDynamicRuleCommand(line.trim())) {
                    handleDynamicRule(line.trim());
                } else {
                    evaluateExpression(line.trim());
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                if (debugger.getMode() == Debugger.Mode.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleDynamicRule(String input) {
        try {
            DynamicRuleParser.ParseResult result = DynamicRuleParser.parse(input);

            if (result.getType() == DynamicRuleParser.ParseResult.Type.ADD) {
                ruleSet.addRule(result.getRule());
                System.out.println("Regel " + result.getRule().fullName() + " hinzugefügt");
            } else if (result.getType() == DynamicRuleParser.ParseResult.Type.REMOVE) {
                int removedCount = ruleSet.removeRule(result.getFullName());
                if (removedCount > 0) {
                    System.out.println(removedCount + " Regel" + (removedCount > 1 ? "n" : "") + " für " + result.getFullName() + " entfernt");
                } else {
                    System.out.println("Regel " + result.getFullName() + " nicht gefunden");
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Fehler beim Parsen der Regel: " + e.getMessage());
        }
    }

    private void handleCommand(String command) {
        String[] parts = command.split("\\s+");

        switch (parts[0]) {
            case ":help" -> showHelp();
            case ":mode" -> {
                if (parts.length > 1) {
                    setMode(parts[1]);
                } else {
                    System.out.println("Current mode: " + debugger.getMode().name().toLowerCase().replace('_', '-'));
                }
            }
            case ":rules" -> {
                if (parts.length > 1) {
                    showRules(parts[1]);
                } else {
                    showAllRules();
                }
            }
            case ":namespaces" -> showNamespaces();
            default -> System.out.println("Unknown command: " + command);
        }
    }

    private void showHelp() {
        System.out.println("Available commands:");
        System.out.println("  :help                 Show this help");
        System.out.println("  :mode [debug|trace|quiet|step-by-step]  Set or show evaluation mode");
        System.out.println("  :rules [namespace]    Show rules (all or for specific namespace)");
        System.out.println("  :namespaces          Show all available namespaces");
        System.out.println("  :exit                 Exit the REPL");
        System.out.println();
        System.out.println("Dynamic rule management:");
        System.out.println("  <namespace.name>pattern replacement    Add a new rule");
        System.out.println("  >namespace.name<                       Remove rule(s)");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  (+ (* 5 9) 13)                        Evaluate arithmetic expression");
        System.out.println("  (length (1 2 3))                      Use list operations");
        System.out.println("  <math.double>(?x) (* ?x 2)             Add doubling rule");
        System.out.println("  >math.double<                          Remove doubling rule");
    }

    private void setMode(String mode) {
        try {
            Debugger.Mode newMode = Debugger.Mode.valueOf(mode.toUpperCase().replace("-", "_"));
            debugger = new Debugger(newMode);
            evaluator = new TermEvaluator(ruleSet, debugger);

            System.out.println("Mode set to: " + mode);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid mode: " + mode);
            System.out.println("Valid modes: debug, trace, quiet, step-by-step");
        }
    }

    private void showRules(String namespace) {
        var rules = ruleSet.getRulesForNamespace(namespace);
        if (rules.isEmpty()) {
            System.out.println("No rules found for namespace: " + namespace);
        } else {
            System.out.println("Rules for namespace " + namespace + ":");
            rules.forEach(System.out::println);
        }
    }

    private void showAllRules() {
        System.out.println("All rules (" + ruleSet.size() + "):");
        ruleSet.getAllRules().forEach(System.out::println);
    }

    private void showNamespaces() {
        System.out.println("Available namespaces:");
        ruleSet.getNamespaces().forEach(ns -> System.out.println("  " + ns));
    }

    private void evaluateExpression(String expression) {
        try {
            Term term = TermParser.parse(expression);

            if (debugger.getMode() == Debugger.Mode.DEBUG) {
                System.out.println("DEBUG: Parsed: " + term);
            }
        } catch (Exception e) {
            System.err.println("Parse error: " + e.getMessage());
            return;
        }

        Term term = TermParser.parse(expression);

        if (debugger.getMode() == Debugger.Mode.DEBUG) {
            System.out.println("DEBUG: Evaluating: " + term);
        }

        Term result = evaluator.evaluate(term);

        if (debugger.getMode() == Debugger.Mode.DEBUG) {
            System.out.println("DEBUG: Result: " + result);
        }

        if (debugger.getMode() == Debugger.Mode.QUIET ||
                debugger.getMode() == Debugger.Mode.STEP_BY_STEP ||
                debugger.getMode() == Debugger.Mode.TRACE) {
            System.out.println(result);
        }
    }
}