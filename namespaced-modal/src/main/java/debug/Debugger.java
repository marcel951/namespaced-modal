package debug;

import core.Rule;
import core.Term;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Debugger {
    public enum Mode {
        QUIET, DEBUG, TRACE, STEP_BY_STEP
    }

    private final Mode mode;
    private final BufferedReader reader;
    private boolean shouldContinue = true;
    private boolean runToEnd = false;

    public Debugger(Mode mode) {
        this.mode = mode;
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public Mode getMode() {
        return mode;
    }

    public void onStepStart(Term term) {
        if (mode == Mode.TRACE) {
            System.out.println("Step: " + term);
        }
    }

    public void onStepEnd(Term term) {
        if (mode == Mode.TRACE) {
            System.out.println("Final: " + term);
        }
    }

    public void onRuleApplied(Rule rule, Term before, Term after) {
        switch (mode) {
            case DEBUG -> {
                // KORRIGIERT: Regel-Namespace und -Name anzeigen
                System.out.println("Applied " + rule.fullName() + ": " + before + " -> " + after);
            }
            case TRACE -> {
                // KORRIGIERT: Regel-Namespace und -Name anzeigen
                System.out.println("Rule " + rule.fullName() + ": " + before + " -> " + after);
            }
            case STEP_BY_STEP -> {
                if (!runToEnd) {
                    // KORRIGIERT: Regel-Namespace und -Name anzeigen
                    System.out.println("Rule " + rule.fullName() + ": " + before + " -> " + after);
                    System.out.print("Continue? (y/n/r): ");
                    try {
                        String input = reader.readLine().toLowerCase();
                        switch (input) {
                            case "n" -> shouldContinue = false;
                            case "r" -> {
                                runToEnd = true;
                                System.out.println("Running to end...");
                            }
                            default -> { /* continue */ }
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading input: " + e.getMessage());
                    }
                }
            }
        }
    }

    public void onEvaluation(Term before, Term after) {
        if (mode == Mode.DEBUG || mode == Mode.TRACE) {
            System.out.println("Evaluated: " + before + " -> " + after);
        }
    }

    public boolean shouldContinue() {
        return shouldContinue;
    }
}