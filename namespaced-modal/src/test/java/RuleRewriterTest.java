package core;

import debug.Debugger;
import io.TermParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;

public class RuleRewriterTest {
    private RuleRewriter rewriter;
    private RuleSet ruleSet;

    @BeforeEach
    public void setUp() {
        ruleSet = new RuleSet();
        Debugger debugger = new Debugger(Debugger.Mode.QUIET);
        rewriter = new RuleRewriter(ruleSet, debugger);
    }

    @Test
    public void testNoRuleMatch() {
        Term term = TermParser.parse("(unknown function)");
        Optional<Term> result = rewriter.tryRewrite(term);
        assertFalse(result.isPresent());
    }

    @Test
    public void testSimpleRuleMatch() {
        Rule rule = new Rule("test", "simple",
                TermParser.parse("(hello ?x)"),
                TermParser.parse("(world ?x)"));
        ruleSet.addRule(rule);

        Term term = TermParser.parse("(hello there)");
        Optional<Term> result = rewriter.tryRewrite(term);

        assertTrue(result.isPresent());
        assertEquals("(world there)", result.get().toString());
    }

    @Test
    public void testBaseCasePriority() {
        // Add recursive rule first
        Rule recursiveRule = new Rule("math", "factn",
                TermParser.parse("(fact ?n)"),
                TermParser.parse("(* ?n (fact (- ?n 1)))"));

        // Add base case after
        Rule baseRule = new Rule("math", "fact0",
                TermParser.parse("(fact 0)"),
                TermParser.parse("1"));

        ruleSet.addRule(recursiveRule);
        ruleSet.addRule(baseRule);

        // Base case should be applied first
        Term term = TermParser.parse("(fact 0)");
        Optional<Term> result = rewriter.tryRewrite(term);

        assertTrue(result.isPresent());
        assertEquals("1", result.get().toString());
    }

    @Test
    public void testMultipleRulesFirstMatch() {
        Rule rule1 = new Rule("test", "first",
                TermParser.parse("(test ?x)"),
                TermParser.parse("(first ?x)"));
        Rule rule2 = new Rule("test", "second",
                TermParser.parse("(test ?x)"),
                TermParser.parse("(second ?x)"));

        ruleSet.addRule(rule1);
        ruleSet.addRule(rule2);

        Term term = TermParser.parse("(test value)");
        Optional<Term> result = rewriter.tryRewrite(term);

        assertTrue(result.isPresent());
        // Should match the first rule
        assertEquals("(first value)", result.get().toString());
    }

    @Test
    public void testAtomNoRewrite() {
        Term atom = TermParser.parse("atom");
        Optional<Term> result = rewriter.tryRewrite(atom);
        assertFalse(result.isPresent());
    }

    @Test
    public void testEmptyListNoRewrite() {
        Term emptyList = TermParser.parse("()");
        Optional<Term> result = rewriter.tryRewrite(emptyList);
        assertFalse(result.isPresent());
    }
}