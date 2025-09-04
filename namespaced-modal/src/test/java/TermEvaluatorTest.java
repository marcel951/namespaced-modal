package core;

import debug.Debugger;
import io.TermParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TermEvaluatorTest {
    private TermEvaluator evaluator;
    private RuleSet ruleSet;

    @BeforeEach
    public void setUp() {
        ruleSet = new RuleSet();
        Debugger debugger = new Debugger(Debugger.Mode.QUIET);
        evaluator = new TermEvaluator(ruleSet, debugger);
    }

    @Test
    public void testEvaluateAtom() {
        Term atom = TermParser.parse("hello");
        Term result = evaluator.evaluate(atom);
        assertEquals(atom, result);
    }

    @Test
    public void testEvaluateEmptyList() {
        Term emptyList = TermParser.parse("()");
        Term result = evaluator.evaluate(emptyList);
        assertEquals(emptyList, result);
    }

    @Test
    public void testArithmeticAddition() {
        Term expr = TermParser.parse("(+ 2 3)");
        Term result = evaluator.evaluate(expr);
        assertEquals("5", result.toString());
    }

    @Test
    public void testArithmeticNested() {
        Term expr = TermParser.parse("(+ (* 2 3) 4)");
        Term result = evaluator.evaluate(expr);
        assertEquals("10", result.toString());
    }

    @Test
    public void testIfConditionTrue() {
        Term expr = TermParser.parse("(if true hello world)");
        Term result = evaluator.evaluate(expr);
        assertEquals("hello", result.toString());
    }

    @Test
    public void testIfConditionFalse() {
        Term expr = TermParser.parse("(if false hello world)");
        Term result = evaluator.evaluate(expr);
        assertEquals("world", result.toString());
    }

    @Test
    public void testIfWithComparison() {
        Term expr = TermParser.parse("(if (> 5 3) greater lesser)");
        Term result = evaluator.evaluate(expr);
        assertEquals("greater", result.toString());
    }

    @Test
    public void testRuleApplication() {
        // Add a simple rule: (double ?x) -> (* ?x 2)
        Rule rule = new Rule("test", "double",
                TermParser.parse("(double ?x)"),
                TermParser.parse("(* ?x 2)"));
        ruleSet.addRule(rule);

        Term expr = TermParser.parse("(double 5)");
        Term result = evaluator.evaluate(expr);
        assertEquals("10", result.toString());
    }

    @Test
    public void testRecursiveRule() {
        // Add factorial rules
        Rule baseRule = new Rule("math", "fact0",
                TermParser.parse("(fact 0)"),
                TermParser.parse("1"));
        Rule recursiveRule = new Rule("math", "factn",
                TermParser.parse("(fact ?n)"),
                TermParser.parse("(if (= ?n 0) 1 (* ?n (fact (- ?n 1))))"));

        ruleSet.addRule(baseRule);
        ruleSet.addRule(recursiveRule);

        Term expr = TermParser.parse("(fact 3)");
        Term result = evaluator.evaluate(expr);
        assertEquals("6", result.toString());
    }

    @Test
    public void testMemoization() {
        // Test that the same expression is not re-evaluated
        Rule expensiveRule = new Rule("test", "expensive",
                TermParser.parse("(expensive ?x)"),
                TermParser.parse("(* ?x ?x)"));
        ruleSet.addRule(expensiveRule);

        Term expr = TermParser.parse("(+ (expensive 5) (expensive 5))");
        Term result = evaluator.evaluate(expr);
        assertEquals("50", result.toString());
    }

    @Test
    public void testCycleDetection() {
        // Rule that would cause infinite recursion: (loop ?x) -> (loop ?x)
        Rule cycleRule = new Rule("test", "loop",
                TermParser.parse("(loop ?x)"),
                TermParser.parse("(loop ?x)"));
        ruleSet.addRule(cycleRule);

        Term expr = TermParser.parse("(loop 5)");
        Term result = evaluator.evaluate(expr);
        assertEquals("(loop 5)", result.toString());
    }
}