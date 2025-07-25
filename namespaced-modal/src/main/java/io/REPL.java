package io;

import core.RuleSet;
import core.Term;
import core.Rewriter;
import debug.Debugger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class REPL {
    private final RuleSet ruleSet;
    private final BufferedReader reader;
    private Debugger debugger;

    public REPL(RuleSet ruleSet) {
        this.ruleSet = ruleSet;
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.debugger = new Debugger(Debugger.Mode.QUIET);
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
        System.out.println("Examples:");
        System.out.println("  (+ (* 5 9) 13)       Evaluate arithmetic expression");
        System.out.println("  (length (1 2 3))     Use list operations");
    }

    private void setMode(String mode) {
        try {
            Debugger.Mode newMode = Debugger.Mode.valueOf(mode.toUpperCase().replace("-", "_"));
            debugger = new Debugger(newMode);
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
        Term term = TermParser.parse(expression);

        if (debugger.getMode() == Debugger.Mode.DEBUG) {
            System.out.println("DEBUG: Parsed term: " + term);
        }

        Rewriter rewriter = new Rewriter(ruleSet, debugger);
        Term result = rewriter.rewrite(term);

        if (debugger.getMode() == Debugger.Mode.DEBUG) {
            System.out.println("DEBUG: Result: " + result);
        }

        // Always print result in quiet mode
        if (debugger.getMode() == Debugger.Mode.QUIET) {
            System.out.println(result);
        }
    }
}