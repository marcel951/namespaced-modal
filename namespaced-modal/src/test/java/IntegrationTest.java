package integration;

import core.*;
import debug.Debugger;
import io.RuleParser;
import io.TermParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {

    @Test
    public void testFullArithmeticRewriting() {
        // Load standard rules
        RuleSet ruleSet = RuleParser.loadFromResource("rules/standard.modal");
        Rewriter rewriter = new Rewriter(ruleSet, new Debugger(Debugger.Mode.QUIET));

        // Test basic arithmetic
        Term term = TermParser.parse("(+ 1 2)");
        Term result = rewriter.rewrite(term);
        assertEquals("3", result.toString());

        // Test nested arithmetic
        term = TermParser.parse("(+ (* 2 3) 4)");
        result = rewriter.rewrite(term);
        assertEquals("10", result.toString());

        // Test complex expression
        term = TermParser.parse("(- (* (+ 2 3) 4) 5)");
        result = rewriter.rewrite(term);
        assertEquals("15", result.toString());
    }

    @Test
    public void testListOperations() {
        RuleSet ruleSet = RuleParser.loadFromResource("rules/standard.modal");
        Rewriter rewriter = new Rewriter(ruleSet, new Debugger(Debugger.Mode.QUIET));

        // Test length
        Term term = TermParser.parse("(length (a b c))");
        Term result = rewriter.rewrite(term);
        assertEquals("3", result.toString());

        // Test first
        term = TermParser.parse("(first (hello world))");
        result = rewriter.rewrite(term);
        assertEquals("hello", result.toString());

        // Test rest
        term = TermParser.parse("(rest (a b c))");
        result = rewriter.rewrite(term);
        assertEquals("(b c)", result.toString());
    }

    @Test
    public void testBooleanOperations() {
        RuleSet ruleSet = RuleParser.loadFromResource("rules/standard.modal");
        Rewriter rewriter = new Rewriter(ruleSet, new Debugger(Debugger.Mode.QUIET));

        // Test and
        Term term = TermParser.parse("(and true false)");
        Term result = rewriter.rewrite(term);
        assertEquals("false", result.toString());

        // Test or
        term = TermParser.parse("(or true false)");
        result = rewriter.rewrite(term);
        assertEquals("true", result.toString());

        // Test not
        term = TermParser.parse("(not true)");
        result = rewriter.rewrite(term);
        assertEquals("false", result.toString());
    }

    @Test
    public void testRuleNamespaces() {
        RuleSet ruleSet = RuleParser.loadFromResource("rules/standard.modal");

        // Test that we have rules in different namespaces
        assertFalse(ruleSet.getRulesForNamespace("math").isEmpty());
        assertFalse(ruleSet.getRulesForNamespace("list").isEmpty());
        assertFalse(ruleSet.getRulesForNamespace("bool").isEmpty());

        // Test that we can get rules by function symbol
        assertFalse(ruleSet.getRulesForFunction("+").isEmpty());
        assertFalse(ruleSet.getRulesForFunction("length").isEmpty());
        assertFalse(ruleSet.getRulesForFunction("and").isEmpty());
    }
}