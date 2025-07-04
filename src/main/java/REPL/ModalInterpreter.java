package REPL;

import core.Rule;
import core.Term;
import engine.RuleManager;
import engine.Rewriter;
import parser.Parser;

public class ModalInterpreter {
    private RuleManager ruleManager;
    private Rewriter rewriter;

    public ModalInterpreter() {
        this.ruleManager = new RuleManager();
        this.rewriter = new Rewriter(ruleManager);
    }

    public void processInput(String input) {
        String trimmed = input.trim();

        if (trimmed.startsWith("<>")) {
            handleRuleDefinition(trimmed);
        } else if (trimmed.startsWith("><")) {
            handleRuleRemoval(trimmed);
        } else {
            handleTermRewriting(trimmed);
        }
    }

    private void handleRuleDefinition(String input) {
        // Format: <> (pattern) (replacement) oder <name> (pattern) (replacement)
        int firstSpace = input.indexOf(' ');
        if (firstSpace == -1) return;

        String rulePart = input.substring(0, firstSpace).trim();
        String restPart = input.substring(firstSpace + 1).trim();

        // Parse die zwei Terme
        Parser parser = new Parser(restPart);
        Term pattern = parser.parse();
        Term replacement = parser.parse();

        if (pattern == null || replacement == null) {
            System.err.println("Fehler beim Parsen der Regel");
            return;
        }

        // Extrahiere Regelname
        String ruleName = null;
        if (rulePart.length() > 2) {
            ruleName = rulePart.substring(1, rulePart.length() - 1);
        }

        Rule rule = new Rule(ruleName, pattern, replacement);
        ruleManager.addRule(rule);
        System.out.println("Regel hinzugefügt: " + rule);
    }

    private void handleRuleRemoval(String input) {
        // Format: >< (pattern)
        String patternStr = input.substring(2).trim();
        Term pattern = Parser.parseString(patternStr);

        if (pattern != null && ruleManager.removeRule(pattern)) {
            System.out.println("Regel entfernt: " + pattern);
        } else {
            System.out.println("Regel nicht gefunden: " + pattern);
        }
    }

    private void handleTermRewriting(String input) {
        Term term = Parser.parseString(input);
        if (term == null) {
            System.err.println("Fehler beim Parsen des Terms");
            return;
        }

        Term result = rewriter.rewriteMultiple(term, 1000);
        System.out.println(result);
    }

    public void showRules() {
        System.out.println("Aktuelle Regeln:");
        for (Rule rule : ruleManager.getAllRules()) {
            System.out.println("  " + rule);
        }
    }

    public void showStats() {
        System.out.println("Statistiken:");
        System.out.println("  Regeln gesamt: " + ruleManager.getRuleCount());
        System.out.println("  Unbenutzte Regeln: " + ruleManager.getUnusedRules().size());
    }

    public static void main(String[] args) {
        ModalInterpreter interpreter = new ModalInterpreter();

        // Beispiel-Usage
        interpreter.processInput("<math.+> (?x ?y) (+ ?x ?y)");
        interpreter.processInput("<> (?x ?x) ?x");
        interpreter.processInput("(math.+ 5 3)");
        interpreter.processInput("(a a)");

        interpreter.showRules();
        interpreter.showStats();
    }
}