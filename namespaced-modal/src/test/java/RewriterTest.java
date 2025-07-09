package core;

import debug.Debugger;
import io.TermParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RewriterTest {

    private RuleSet ruleSet;
    private Rewriter rewriter;

    @BeforeEach
    public void setUp() {
        ruleSet = new RuleSet();
        Debugger debugger = new Debugger(Debugger.Mode.QUIET);
        rewriter = new Rewriter(ruleSet, debugger);
    }

    @Test
    public void testRewriteWithoutRules() {
        Term term = TermParser.parse("(unknown 1 2)");
        Term result = rewriter.rewrite(term);
        assertEquals(term, result);
    }

    @Test
    public void testRewriteArithmetic() {
        // Add math rules
        Rule addRule = new Rule("math", "+",
                TermParser.parse("(+ ?a ?b)"),
                TermParser.parse("(: + ?a ?b)"));
        ruleSet.addRule(addRule);

        Term term = TermParser.parse("(+ 3 4)");
        Term result = rewriter.rewrite(term);

        assertTrue(result instanceof Term.Atom);
        assertEquals(7, ((Term.Atom) result).asNumber());
    }

    @Test
    public void testRewriteNested() {
        // Add math rules
        Rule addRule = new Rule("math", "+",
                TermParser.parse("(+ ?a ?b)"),
                TermParser.parse("(: + ?a ?b)"));
        Rule mulRule = new Rule("math", "*",
                TermParser.parse("(* ?a ?b)"),
                TermParser.parse("(: * ?a ?b)"));
        ruleSet.addRule(addRule);
        ruleSet.addRule(mulRule);

        Term term = TermParser.parse("(+ (* 2 3) 4)");
        Term result = rewriter.rewrite(term);

        assertTrue(result instanceof Term.Atom);
        assertEquals(10, ((Term.Atom) result).asNumber());
    }

    @Test
    public void testRewriteCustomRule() {
        // Add custom rule: (double ?x) -> (+ ?x ?x)
        Rule doubleRule = new Rule("custom", "double",
                TermParser.parse("(double ?x)"),
                TermParser.parse("(+ ?x ?x)"));
        Rule addRule = new Rule("math", "+",
                TermParser.parse("(+ ?a ?b)"),
                TermParser.parse("(: + ?a ?b)"));
        ruleSet.addRule(doubleRule);
        ruleSet.addRule(addRule);

        Term term = TermParser.parse("(double 5)");
        Term result = rewriter.rewrite(term);

        assertTrue(result instanceof Term.Atom);
        assertEquals(10, ((Term.Atom) result).asNumber());
    }

    @Test
    public void testRewriteListPattern() {
        // Add rule: (first (?head . ?tail)) -> ?head
        Rule firstRule = new Rule("list", "first",
                TermParser.parse("(first (?head . ?tail))"),
                TermParser.parse("?head"));
        ruleSet.addRule(firstRule);

        Term term = TermParser.parse("(first (a b c))");
        Term result = rewriter.rewrite(term);

        assertTrue(result instanceof Term.Atom);
        assertEquals("a", ((Term.Atom) result).value());
    }
}