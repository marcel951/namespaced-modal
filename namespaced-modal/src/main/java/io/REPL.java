package io;

import core.RuleSet;
import core.Term;
import core.Rewriter;
import debug.Debugger;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.jline.console.SystemRegistry;
import org.jline.console.impl.Builtins;
import org.jline.console.impl.SystemRegistryImpl;
import org.jline.keymap.KeyMap;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class REPL {
    private final RuleSet ruleSet;
    private final LineReader reader;
    private final Terminal terminal;
    private Debugger debugger;

    public REPL(RuleSet ruleSet) throws IOException {
        this.ruleSet = ruleSet;
        this.terminal = TerminalBuilder.builder()
                .system(true)
                .build();

        // Erstelle LineReader mit Auto-Vervollständigung
        this.reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(new REPLCompleter())
                .parser(new DefaultParser())
                .build();

        this.debugger = new Debugger(Debugger.Mode.QUIET);
    }

    public void run() throws IOException {
        System.out.println("Namespaced-Modal Term-Rewriting Language");
        System.out.println("Loaded " + ruleSet.size() + " rules");
        System.out.println("Type :help for commands or :exit to quit");
        System.out.println("Use ↑/↓ for history, Tab for completion");
        System.out.println();

        try {
            while (true) {
                String line;
                try {
                    line = reader.readLine("> ");
                } catch (UserInterruptException e) {
                    // Ctrl+C gedrückt
                    continue;
                } catch (EndOfFileException e) {
                    // Ctrl+D gedrückt oder EOF
                    System.out.println("Goodbye!");
                    break;
                }

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
        } finally {
            terminal.close();
        }
    }

    // Auto-Vervollständigung implementieren
    private class REPLCompleter implements Completer {
        private final List<String> commands = List.of(
                ":help", ":mode", ":rules", ":namespaces", ":exit"
        );

        private final List<String> modes = List.of(
                "debug", "trace", "quiet", "step-by-step"
        );

        @Override
        public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
            String buffer = line.line();

            if (buffer.startsWith(":")) {
                // Vervollständige Kommandos
                if (buffer.startsWith(":mode ")) {
                    String partial = buffer.substring(6);
                    modes.stream()
                            .filter(mode -> mode.startsWith(partial))
                            .forEach(mode -> candidates.add(new Candidate(":mode " + mode)));
                } else if (buffer.startsWith(":rules ")) {
                    String partial = buffer.substring(7);
                    ruleSet.getNamespaces().stream()
                            .filter(ns -> ns.startsWith(partial))
                            .forEach(ns -> candidates.add(new Candidate(":rules " + ns)));
                } else {
                    commands.stream()
                            .filter(cmd -> cmd.startsWith(buffer))
                            .forEach(cmd -> candidates.add(new Candidate(cmd)));
                }
            } else {
                // Vervollständige Namespaces für Ausdrücke
                String word = line.word();
                if (word != null && !word.isEmpty()) {
                    ruleSet.getNamespaces().stream()
                            .filter(ns -> ns.startsWith(word))
                            .forEach(ns -> candidates.add(new Candidate(ns)));
                }
            }
        }
    }

    // Restliche Methoden bleiben unverändert...
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
        System.out.println("Navigation:");
        System.out.println("  ↑/↓                   Browse command history");
        System.out.println("  Tab                   Auto-complete commands/namespaces");
        System.out.println("  Ctrl+C                Cancel current input");
        System.out.println("  Ctrl+D                Exit");
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

        if (debugger.getMode() == Debugger.Mode.QUIET ||
                debugger.getMode() == Debugger.Mode.STEP_BY_STEP ||
                debugger.getMode() == Debugger.Mode.TRACE) {
            System.out.println(result);
        }
    }
}