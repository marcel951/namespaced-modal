package integration;

import core.*;
import debug.Debugger;
import io.RuleParser;
import io.TermParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StandardRulesTest {
    private TermEvaluator evaluator;
    private RuleSet ruleSet;

    @BeforeEach
    public void setUp() {
        // Load all rules from standard.modal
        ruleSet = RuleParser.loadFromResource("rules/standard.modal");
        Debugger debugger = new Debugger(Debugger.Mode.QUIET);
        evaluator = new TermEvaluator(ruleSet, debugger);
    }

    // ==========================================
    // Mathematical Operations Tests
    // ==========================================

    @Test
    public void testBasicArithmetic() {
        assertEquals("7", evaluator.evaluate(TermParser.parse("(+ 3 4)")).toString());
        assertEquals("12", evaluator.evaluate(TermParser.parse("(* 3 4)")).toString());
        assertEquals("5", evaluator.evaluate(TermParser.parse("(- 8 3)")).toString());
        assertEquals("3", evaluator.evaluate(TermParser.parse("(/ 15 5)")).toString());
        assertEquals("2", evaluator.evaluate(TermParser.parse("(% 17 5)")).toString());
    }

    @Test
    public void testNestedArithmetic() {
        assertEquals("14", evaluator.evaluate(TermParser.parse("(+ (* 2 3) (* 2 4))")).toString());
        assertEquals("50", evaluator.evaluate(TermParser.parse("(* (+ 2 3) (- 12 2))")).toString());
    }

    @Test
    public void testFactorial() {
        assertEquals("1", evaluator.evaluate(TermParser.parse("(fact 0)")).toString());
        assertEquals("1", evaluator.evaluate(TermParser.parse("(fact 1)")).toString());
        assertEquals("6", evaluator.evaluate(TermParser.parse("(fact 3)")).toString());
        assertEquals("24", evaluator.evaluate(TermParser.parse("(fact 4)")).toString());
        assertEquals("120", evaluator.evaluate(TermParser.parse("(fact 5)")).toString());
    }

    @Test
    public void testFibonacci() {
        assertEquals("0", evaluator.evaluate(TermParser.parse("(fib 0)")).toString());
        assertEquals("1", evaluator.evaluate(TermParser.parse("(fib 1)")).toString());
        assertEquals("1", evaluator.evaluate(TermParser.parse("(fib 2)")).toString());
        assertEquals("2", evaluator.evaluate(TermParser.parse("(fib 3)")).toString());
        assertEquals("3", evaluator.evaluate(TermParser.parse("(fib 4)")).toString());
        assertEquals("5", evaluator.evaluate(TermParser.parse("(fib 5)")).toString());
        assertEquals("8", evaluator.evaluate(TermParser.parse("(fib 6)")).toString());
    }

    // ==========================================
    // List Operations Tests
    // ==========================================

    @Test
    public void testCarCdr() {
        assertEquals("a", evaluator.evaluate(TermParser.parse("(car (a b c))")).toString());
        assertEquals("(b c)", evaluator.evaluate(TermParser.parse("(cdr (a b c))")).toString());
        assertEquals("42", evaluator.evaluate(TermParser.parse("(car (42))")).toString());
        assertEquals("()", evaluator.evaluate(TermParser.parse("(cdr (42))")).toString());

        // Edge cases - should return themselves for empty lists
        assertEquals("(car ())", evaluator.evaluate(TermParser.parse("(car ())")).toString());
        assertEquals("(cdr ())", evaluator.evaluate(TermParser.parse("(cdr ())")).toString());
    }

    @Test
    public void testListLength() {
        assertEquals("0", evaluator.evaluate(TermParser.parse("(length ())")).toString());
        assertEquals("1", evaluator.evaluate(TermParser.parse("(length (a))")).toString());
        assertEquals("3", evaluator.evaluate(TermParser.parse("(length (a b c))")).toString());
        assertEquals("5", evaluator.evaluate(TermParser.parse("(length (1 2 3 4 5))")).toString());
    }

    @Test
    public void testNullPredicate() {
        assertEquals("true", evaluator.evaluate(TermParser.parse("(null? ())")).toString());
        assertEquals("false", evaluator.evaluate(TermParser.parse("(null? (a))")).toString());
        assertEquals("false", evaluator.evaluate(TermParser.parse("(null? (a b c))")).toString());
    }

    //Probleme mit der Darstellung
    @Test
    public void testReverse() {
        assertEquals("()", evaluator.evaluate(TermParser.parse("(reverse ())")).toString());
        assertEquals("(a . ())", evaluator.evaluate(TermParser.parse("(reverse (a))")).toString());
        assertEquals("(c . (b . (a . ())))", evaluator.evaluate(TermParser.parse("(reverse (a b c))")).toString());
        assertEquals("(5 . (4 . (3 . (2 . (1 . ())))))", evaluator.evaluate(TermParser.parse("(reverse (1 2 3 4 5))")).toString());
    }

    // ==========================================
    // Boolean Logic Tests
    // ==========================================

    @Test
    public void testAndOperator() {
        assertEquals("false", evaluator.evaluate(TermParser.parse("(and false true)")).toString());
        assertEquals("false", evaluator.evaluate(TermParser.parse("(and true false)")).toString());
        assertEquals("false", evaluator.evaluate(TermParser.parse("(and false false)")).toString());
        assertEquals("true", evaluator.evaluate(TermParser.parse("(and true true)")).toString());
        assertEquals("hello", evaluator.evaluate(TermParser.parse("(and true hello)")).toString());
    }

    @Test
    public void testOrOperator() {
        assertEquals("true", evaluator.evaluate(TermParser.parse("(or false true)")).toString());
        assertEquals("true", evaluator.evaluate(TermParser.parse("(or true false)")).toString());
        assertEquals("true", evaluator.evaluate(TermParser.parse("(or true true)")).toString());
        assertEquals("world", evaluator.evaluate(TermParser.parse("(or false world)")).toString());
    }

    @Test
    public void testNotOperator() {
        assertEquals("false", evaluator.evaluate(TermParser.parse("(not true)")).toString());
        assertEquals("true", evaluator.evaluate(TermParser.parse("(not false)")).toString());
    }

    // ==========================================
    // Comparison Operations Tests
    // ==========================================

    @Test
    public void testComparisons() {
        assertEquals("true", evaluator.evaluate(TermParser.parse("(= 5 5)")).toString());
        assertEquals("false", evaluator.evaluate(TermParser.parse("(= 5 6)")).toString());
        assertEquals("true", evaluator.evaluate(TermParser.parse("(!= 5 6)")).toString());
        assertEquals("false", evaluator.evaluate(TermParser.parse("(!= 5 5)")).toString());

        assertEquals("true", evaluator.evaluate(TermParser.parse("(< 3 5)")).toString());
        assertEquals("false", evaluator.evaluate(TermParser.parse("(< 5 3)")).toString());
        assertEquals("true", evaluator.evaluate(TermParser.parse("(> 5 3)")).toString());
        assertEquals("false", evaluator.evaluate(TermParser.parse("(> 3 5)")).toString());

        assertEquals("true", evaluator.evaluate(TermParser.parse("(<= 3 5)")).toString());
        assertEquals("true", evaluator.evaluate(TermParser.parse("(<= 5 5)")).toString());
        assertEquals("false", evaluator.evaluate(TermParser.parse("(<= 5 3)")).toString());

        assertEquals("true", evaluator.evaluate(TermParser.parse("(>= 5 3)")).toString());
        assertEquals("true", evaluator.evaluate(TermParser.parse("(>= 5 5)")).toString());
        assertEquals("false", evaluator.evaluate(TermParser.parse("(>= 3 5)")).toString());
    }

    // ==========================================
    // Conditional Expressions Tests
    // ==========================================

    @Test
    public void testConditionals() {
        assertEquals("yes", evaluator.evaluate(TermParser.parse("(if true yes no)")).toString());
        assertEquals("no", evaluator.evaluate(TermParser.parse("(if false yes no)")).toString());

        // Test with computed conditions
        assertEquals("greater", evaluator.evaluate(TermParser.parse("(if (> 10 5) greater lesser)")).toString());
        assertEquals("lesser", evaluator.evaluate(TermParser.parse("(if (< 10 5) greater lesser)")).toString());
    }

    // ==========================================
    // Utility Functions Tests
    // ==========================================

    @Test
    public void testMinMax() {
        assertEquals("3", evaluator.evaluate(TermParser.parse("(min 3 7)")).toString());
        assertEquals("3", evaluator.evaluate(TermParser.parse("(min 7 3)")).toString());
        assertEquals("7", evaluator.evaluate(TermParser.parse("(max 3 7)")).toString());
        assertEquals("7", evaluator.evaluate(TermParser.parse("(max 7 3)")).toString());
    }

    @Test
    public void testAbsoluteValue() {
        assertEquals("5", evaluator.evaluate(TermParser.parse("(abs 5)")).toString());
        assertEquals("5", evaluator.evaluate(TermParser.parse("(abs -5)")).toString());
        assertEquals("0", evaluator.evaluate(TermParser.parse("(abs 0)")).toString());
    }

    @Test
    public void testEvenOdd() {
        assertEquals("true", evaluator.evaluate(TermParser.parse("(even? 4)")).toString());
        assertEquals("false", evaluator.evaluate(TermParser.parse("(even? 5)")).toString());
        assertEquals("true", evaluator.evaluate(TermParser.parse("(even? 0)")).toString());

        assertEquals("false", evaluator.evaluate(TermParser.parse("(odd? 4)")).toString());
        assertEquals("true", evaluator.evaluate(TermParser.parse("(odd? 5)")).toString());
        assertEquals("false", evaluator.evaluate(TermParser.parse("(odd? 0)")).toString());
    }

    // ==========================================
    // Advanced Functions Tests
    // ==========================================

    @Test
    public void testSum() {
        assertEquals("0", evaluator.evaluate(TermParser.parse("(sum ())")).toString());
        assertEquals("15", evaluator.evaluate(TermParser.parse("(sum (1 2 3 4 5))")).toString());
        assertEquals("10", evaluator.evaluate(TermParser.parse("(sum (2 3 5))")).toString());
    }

    @Test
    public void testProduct() {
        assertEquals("1", evaluator.evaluate(TermParser.parse("(product ())")).toString());
        assertEquals("120", evaluator.evaluate(TermParser.parse("(product (1 2 3 4 5))")).toString());
        assertEquals("30", evaluator.evaluate(TermParser.parse("(product (2 3 5))")).toString());
    }


    @Test
    public void testDrop() {
        assertEquals("(a b c)", evaluator.evaluate(TermParser.parse("(drop 0 (a b c))")).toString());
        assertEquals("()", evaluator.evaluate(TermParser.parse("(drop 5 ())")).toString());
        assertEquals("(c . (d))", evaluator.evaluate(TermParser.parse("(drop 2 (a b c d))")).toString());
        assertEquals("(4 . (5))", evaluator.evaluate(TermParser.parse("(drop 3 (1 2 3 4 5))")).toString());
        assertEquals("()", evaluator.evaluate(TermParser.parse("(drop 10 (a b c))")).toString()); // Drop more than length
    }

    @Test
    public void testRange() {
        assertEquals("()", evaluator.evaluate(TermParser.parse("(range 5 5)")).toString());
        assertEquals("(1 . (2 . (3 . (4 . ()))))", evaluator.evaluate(TermParser.parse("(range 1 5)")).toString());
        assertEquals("(0 . (1 . (2 . ())))", evaluator.evaluate(TermParser.parse("(range 0 3)")).toString());
        assertEquals("()", evaluator.evaluate(TermParser.parse("(range 5 3)")).toString()); // End < Start
    }

    @Test
    public void testMember() {
        assertEquals("false", evaluator.evaluate(TermParser.parse("(member? x ())")).toString());
        assertEquals("true", evaluator.evaluate(TermParser.parse("(member? a (a b c))")).toString());
        assertEquals("true", evaluator.evaluate(TermParser.parse("(member? b (a b c))")).toString());
        assertEquals("true", evaluator.evaluate(TermParser.parse("(member? c (a b c))")).toString());
        assertEquals("false", evaluator.evaluate(TermParser.parse("(member? d (a b c))")).toString());
        assertEquals("true", evaluator.evaluate(TermParser.parse("(member? 3 (1 2 3 4))")).toString());
    }

    // ==========================================
    // Mathematical Functions Tests
    // ==========================================

    @Test
    public void testSquareCube() {
        assertEquals("25", evaluator.evaluate(TermParser.parse("(square 5)")).toString());
        assertEquals("9", evaluator.evaluate(TermParser.parse("(square 3)")).toString());
        assertEquals("0", evaluator.evaluate(TermParser.parse("(square 0)")).toString());

        assertEquals("125", evaluator.evaluate(TermParser.parse("(cube 5)")).toString());
        assertEquals("27", evaluator.evaluate(TermParser.parse("(cube 3)")).toString());
        assertEquals("0", evaluator.evaluate(TermParser.parse("(cube 0)")).toString());
    }

    @Test
    public void testPower() {
        assertEquals("1", evaluator.evaluate(TermParser.parse("(pow 5 0)")).toString());
        assertEquals("1", evaluator.evaluate(TermParser.parse("(pow 0 0)")).toString()); // 0^0 = 1 by convention
        assertEquals("5", evaluator.evaluate(TermParser.parse("(pow 5 1)")).toString());
        assertEquals("25", evaluator.evaluate(TermParser.parse("(pow 5 2)")).toString());
        assertEquals("8", evaluator.evaluate(TermParser.parse("(pow 2 3)")).toString());
    }

    // ==========================================
    // Complex Integration Tests
    // ==========================================

    @Test
    public void testComplexExpressions() {
        // Factorial of fibonacci number
        assertEquals("6", evaluator.evaluate(TermParser.parse("(fact (fib 4))")).toString()); // fact(fib(4)) = fact(3) = 6

        // Power of factorial
        assertEquals("36", evaluator.evaluate(TermParser.parse("(square (fact 3))")).toString()); // square(6) = 36

        // Conditional with complex expressions
        assertEquals("even", evaluator.evaluate(TermParser.parse("(if (even? (+ 2 4)) even odd)")).toString());
    }


    @Test
    public void testEdgeCases() {
        // Zero and negative numbers
        assertEquals("1", evaluator.evaluate(TermParser.parse("(fact 0)")).toString());
        assertEquals("0", evaluator.evaluate(TermParser.parse("(fib 0)")).toString());
        assertEquals("5", evaluator.evaluate(TermParser.parse("(abs -5)")).toString());
    }

}