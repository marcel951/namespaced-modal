package core;

import io.TermParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SpecialOperatorEvaluatorTest {
    private SpecialOperatorEvaluator evaluator;
    private TermEvaluator mockEvaluator;

    @BeforeEach
    public void setUp() {
        evaluator = new SpecialOperatorEvaluator();
        // Create a simple mock evaluator that just returns atoms unchanged
        mockEvaluator = new TermEvaluator(new RuleSet(), new debug.Debugger(debug.Debugger.Mode.QUIET)) {
            @Override
            public Term evaluate(Term term) {
                return term; // Simple identity function for testing
            }
        };
    }

    @Test
    public void testIsSpecialOperator() {
        assertTrue(evaluator.isSpecialOperator("+"));
        assertTrue(evaluator.isSpecialOperator("-"));
        assertTrue(evaluator.isSpecialOperator("*"));
        assertTrue(evaluator.isSpecialOperator("/"));
        assertTrue(evaluator.isSpecialOperator("if"));
        assertTrue(evaluator.isSpecialOperator(":"));
        assertTrue(evaluator.isSpecialOperator(">"));
        assertTrue(evaluator.isSpecialOperator("="));

        assertFalse(evaluator.isSpecialOperator("custom"));
        assertFalse(evaluator.isSpecialOperator("factorial"));
    }

    @Test
    public void testArithmeticAddition() {
        Term expr = TermParser.parse("(: + 3 4)");
        Term result = evaluator.evaluate((Term.List) expr, mockEvaluator);
        assertEquals("7", result.toString());
    }

    @Test
    public void testArithmeticSubtraction() {
        Term expr = TermParser.parse("(: - 10 3)");
        Term result = evaluator.evaluate((Term.List) expr, mockEvaluator);
        assertEquals("7", result.toString());
    }

    @Test
    public void testArithmeticMultiplication() {
        Term expr = TermParser.parse("(: * 4 5)");
        Term result = evaluator.evaluate((Term.List) expr, mockEvaluator);
        assertEquals("20", result.toString());
    }

    @Test
    public void testArithmeticDivision() {
        Term expr = TermParser.parse("(: / 15 3)");
        Term result = evaluator.evaluate((Term.List) expr, mockEvaluator);
        assertEquals("5", result.toString());
    }

    @Test
    public void testDivisionByZero() {
        Term expr = TermParser.parse("(: / 5 0)");
        assertThrows(ArithmeticException.class, () -> {
            evaluator.evaluate((Term.List) expr, mockEvaluator);
        });
    }

    @Test
    public void testComparison() {
        Term greaterExpr = TermParser.parse("(: > 5 3)");
        Term result1 = evaluator.evaluate((Term.List) greaterExpr, mockEvaluator);
        assertEquals("true", result1.toString());

        Term lessExpr = TermParser.parse("(: < 5 3)");
        Term result2 = evaluator.evaluate((Term.List) lessExpr, mockEvaluator);
        assertEquals("false", result2.toString());
    }

    @Test
    public void testEquality() {
        Term equalExpr = TermParser.parse("(: = hello hello)");
        Term result1 = evaluator.evaluate((Term.List) equalExpr, mockEvaluator);
        assertEquals("true", result1.toString());

        Term notEqualExpr = TermParser.parse("(: != hello world)");
        Term result2 = evaluator.evaluate((Term.List) notEqualExpr, mockEvaluator);
        assertEquals("true", result2.toString());
    }

    @Test
    public void testInfixOperator() {
        Term expr = TermParser.parse("(+ 2 3)");
        Term result = evaluator.evaluate((Term.List) expr, mockEvaluator);
        assertEquals("5", result.toString());
    }

    @Test
    public void testIfTrue() {
        // Create a mock evaluator that evaluates conditions
        TermEvaluator condEvaluator = new TermEvaluator(new RuleSet(), new debug.Debugger(debug.Debugger.Mode.QUIET)) {
            @Override
            public Term evaluate(Term term) {
                if (term.toString().equals("true")) return Term.bool(true);
                if (term.toString().equals("false")) return Term.bool(false);
                return term;
            }
        };

        Term expr = TermParser.parse("(if true yes no)");
        Term result = evaluator.evaluate((Term.List) expr, condEvaluator);
        assertEquals("yes", result.toString());
    }

    @Test
    public void testIfFalse() {
        TermEvaluator condEvaluator = new TermEvaluator(new RuleSet(), new debug.Debugger(debug.Debugger.Mode.QUIET)) {
            @Override
            public Term evaluate(Term term) {
                if (term.toString().equals("true")) return Term.bool(true);
                if (term.toString().equals("false")) return Term.bool(false);
                return term;
            }
        };

        Term expr = TermParser.parse("(if false yes no)");
        Term result = evaluator.evaluate((Term.List) expr, condEvaluator);
        assertEquals("no", result.toString());
    }

    @Test
    public void testIfInvalidCondition() {
        Term expr = TermParser.parse("(if maybe yes no)");
        assertThrows(IllegalArgumentException.class, () -> {
            evaluator.evaluate((Term.List) expr, mockEvaluator);
        });
    }

    @Test
    public void testInvalidArithmeticArgs() {
        Term expr = TermParser.parse("(: + hello world)");
        assertThrows(IllegalArgumentException.class, () -> {
            evaluator.evaluate((Term.List) expr, mockEvaluator);
        });
    }
}