package integration;

import core.*;
import debug.Debugger;
import io.TermParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {
    private TermEvaluator evaluator;
    private RuleSet ruleSet;

    @BeforeEach
    public void setUp() {
        ruleSet = new RuleSet();
        Debugger debugger = new Debugger(Debugger.Mode.QUIET);
        evaluator = new TermEvaluator(ruleSet, debugger);

        // Add common rules
        addMathRules();
        addListRules();
    }

    private void addMathRules() {
        // Factorial
        ruleSet.addRule(new Rule("math", "fact0",
                TermParser.parse("(fact 0)"),
                TermParser.parse("1")));
        ruleSet.addRule(new Rule("math", "factn",
                TermParser.parse("(fact ?n)"),
                TermParser.parse("(if (= ?n 0) 1 (* ?n (fact (- ?n 1))))")));
    }

    private void addListRules() {
        // Length
        ruleSet.addRule(new Rule("list", "length0",
                TermParser.parse("(length ())"),
                TermParser.parse("0")));
        ruleSet.addRule(new Rule("list", "lengthn",
                TermParser.parse("(length (?head . ?tail))"),
                TermParser.parse("(+ 1 (length ?tail))")));
    }

    @Test
    public void testComplexArithmetic() {
        Term expr = TermParser.parse("(+ (* 3 4) (/ 15 3))");
        Term result = evaluator.evaluate(expr);
        assertEquals("17", result.toString());
    }

    @Test
    public void testFactorial() {
        Term expr = TermParser.parse("(fact 5)");
        Term result = evaluator.evaluate(expr);
        assertEquals("120", result.toString());
    }

    @Test
    public void testListLength() {
        Term expr = TermParser.parse("(length (a b c d))");
        Term result = evaluator.evaluate(expr);
        assertEquals("4", result.toString());
    }

    @Test
    public void testNestedConditionals() {
        Term expr = TermParser.parse("(if (> 5 3) (if (< 2 4) inner-true inner-false) outer-false)");
        Term result = evaluator.evaluate(expr);
        assertEquals("inner-true", result.toString());
    }

    @Test
    public void testMixedOperations() {
        Term expr = TermParser.parse("(if (= (+ 2 3) 5) (fact 3) (length (a b)))");
        Term result = evaluator.evaluate(expr);
        assertEquals("6", result.toString());
    }
}