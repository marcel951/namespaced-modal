package core;

import io.TermParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EvaluatorTest {

    @Test
    public void testEvaluateNonArithmetic() {
        Term term = TermParser.parse("(hello world)");
        Term result = Evaluator.evaluate(term);
        assertEquals(term, result); // Should be unchanged
    }

    @Test
    public void testEvaluateAddition() {
        Term term = TermParser.parse("(: + 3 4)");
        Term result = Evaluator.evaluate(term);

        assertTrue(result instanceof Term.Atom);
        Term.Atom atom = (Term.Atom) result;
        assertTrue(atom.isNumber());
        assertEquals(7, atom.asNumber());
    }

    @Test
    public void testEvaluateSubtraction() {
        Term term = TermParser.parse("(: - 10 3)");
        Term result = Evaluator.evaluate(term);

        assertTrue(result instanceof Term.Atom);
        assertEquals(7, ((Term.Atom) result).asNumber());
    }

    @Test
    public void testEvaluateMultiplication() {
        Term term = TermParser.parse("(: * 6 7)");
        Term result = Evaluator.evaluate(term);

        assertTrue(result instanceof Term.Atom);
        assertEquals(42, ((Term.Atom) result).asNumber());
    }

    @Test
    public void testEvaluateDivision() {
        Term term = TermParser.parse("(: / 15 3)");
        Term result = Evaluator.evaluate(term);

        assertTrue(result instanceof Term.Atom);
        assertEquals(5, ((Term.Atom) result).asNumber());
    }

    @Test
    public void testEvaluateModulo() {
        Term term = TermParser.parse("(: % 17 5)");
        Term result = Evaluator.evaluate(term);

        assertTrue(result instanceof Term.Atom);
        assertEquals(2, ((Term.Atom) result).asNumber());
    }

    @Test
    public void testEvaluateDivisionByZero() {
        Term term = TermParser.parse("(: / 5 0)");
        assertThrows(ArithmeticException.class, () -> Evaluator.evaluate(term));
    }

    @Test
    public void testEvaluateInvalidOperator() {
        Term term = TermParser.parse("(: ^ 2 3)");
        assertThrows(IllegalArgumentException.class, () -> Evaluator.evaluate(term));
    }

    @Test
    public void testEvaluateWrongNumberOfArgs() {
        Term term = TermParser.parse("(: + 1)");
        assertThrows(IllegalArgumentException.class, () -> Evaluator.evaluate(term));
    }

    @Test
    public void testEvaluateNonNumbers() {
        Term term = TermParser.parse("(: + hello world)");
        assertThrows(IllegalArgumentException.class, () -> Evaluator.evaluate(term));
    }
}