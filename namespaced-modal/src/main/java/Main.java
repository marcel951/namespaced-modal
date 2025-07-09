import core.RuleSet;
import io.REPL;
import io.RuleParser;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("Loading rules...");
            RuleSet ruleSet = RuleParser.loadFromResource("rules/standard.modal");
            System.out.println("Rules loaded successfully!");

            new REPL(ruleSet).run();
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}