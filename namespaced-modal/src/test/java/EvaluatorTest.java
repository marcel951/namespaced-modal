
package core;

import io.TermParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import io.RuleParser;
import debug.Debugger;

public class EvaluatorTest {

    private RuleSet ruleSet;
    private UnifiedInterpreter interpreter;
    private Debugger debugger;

    @BeforeEach
    public void setUp() {
        try {
            System.out.println("Loading rules from: rules/standard.modal");
            ruleSet = RuleParser.loadFromResource("rules/standard.modal");
            System.out.println("Loaded " + ruleSet.getAllRules().size() + " rules");

            debugger = new Debugger(Debugger.Mode.QUIET);
            interpreter = new UnifiedInterpreter(ruleSet, debugger);
            System.out.println("Setup completed successfully");
        } catch (Exception e) {
            System.err.println("Error in setUp: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to load rules for test", e);
        }
    }

    private Term evaluate(String input) {
        Term term = TermParser.parse(input);
        return interpreter.evaluate(term);
    }

    // ==========================================
    // Basistests - Arithmetik
    // ==========================================

    @Test
    public void testEvaluateNonArithmetic() {
        Term term = TermParser.parse("(hello world)");
        Term result = interpreter.evaluate(term);
        assertEquals(term, result);
    }

    @Test
    public void testArithmeticViaRules() {
        assertEquals(Term.number(7), evaluate("(+ 3 4)"));
        assertEquals(Term.number(7), evaluate("(- 10 3)"));
        assertEquals(Term.number(42), evaluate("(* 6 7)"));
        assertEquals(Term.number(5), evaluate("(/ 15 3)"));
        assertEquals(Term.number(2), evaluate("(% 17 5)"));
    }

    @Test
    public void testNestedArithmetic() {
        assertEquals(Term.number(10), evaluate("(+ (* 2 3) 4)"));
        assertEquals(Term.number(15), evaluate("(- (* (+ 2 3) 4) 5)"));
        assertEquals(Term.number(144), evaluate("(* (+ 10 2) (+ 10 2))"));
    }

    @Test
    public void testDivisionByZero() {
        assertThrows(ArithmeticException.class, () -> evaluate("(/ 5 0)"));
        assertThrows(ArithmeticException.class, () -> evaluate("(% 5 0)"));
    }

    // ==========================================
    // Rekursive Mathematik-Funktionen - JETZT MÖGLICH!
    // ==========================================

    @Test
    public void testFactorial() {
        assertEquals(Term.number(1), evaluate("(fact 0)"));
        assertEquals(Term.number(1), evaluate("(fact 1)"));
        assertEquals(Term.number(2), evaluate("(fact 2)"));
        assertEquals(Term.number(6), evaluate("(fact 3)"));
        assertEquals(Term.number(24), evaluate("(fact 4)"));
        assertEquals(Term.number(120), evaluate("(fact 5)"));
        assertEquals(Term.number(720), evaluate("(fact 6)"));
        assertEquals(Term.number(5040), evaluate("(fact 7)"));
    }

    @Test
    public void testFibonacci() {
        assertEquals(Term.number(0), evaluate("(fib 0)"));
        assertEquals(Term.number(1), evaluate("(fib 1)"));
        assertEquals(Term.number(1), evaluate("(fib 2)"));
        assertEquals(Term.number(2), evaluate("(fib 3)"));
        assertEquals(Term.number(3), evaluate("(fib 4)"));
        assertEquals(Term.number(5), evaluate("(fib 5)"));
        assertEquals(Term.number(8), evaluate("(fib 6)"));
        assertEquals(Term.number(13), evaluate("(fib 7)"));
        assertEquals(Term.number(21), evaluate("(fib 8)"));
        assertEquals(Term.number(55), evaluate("(fib 10)"));
    }

    @Test
    public void testLargeMathFunctions() {
        // Größere Berechnungen die früher nicht möglich waren
        assertEquals(Term.number(40320), evaluate("(fact 8)"));
        assertEquals(Term.number(89), evaluate("(fib 11)"));
    }

    // ==========================================
    // Vergleiche und Logik
    // ==========================================

    @Test
    public void testComparisons() {
        assertEquals(Term.bool(true), evaluate("(> 5 3)"));
        assertEquals(Term.bool(true), evaluate("(< 3 5)"));
        assertEquals(Term.bool(true), evaluate("(= 5 5)"));
        assertEquals(Term.bool(false), evaluate("(= 5 6)"));
        assertEquals(Term.bool(true), evaluate("(>= 5 5)"));
        assertEquals(Term.bool(true), evaluate("(<= 3 5)"));
        assertEquals(Term.bool(true), evaluate("(!= 7 8)"));
        assertEquals(Term.bool(false), evaluate("(!= 5 5)"));
    }

    @Test
    public void testBooleanOperations() {
        assertEquals(Term.bool(false), evaluate("(and false true)"));
        assertEquals(Term.bool(true), evaluate("(and true true)"));
        assertEquals(Term.bool(false), evaluate("(and false false)"));
        assertEquals(Term.bool(true), evaluate("(or true false)"));
        assertEquals(Term.bool(false), evaluate("(or false false)"));
        assertEquals(Term.bool(true), evaluate("(or true true)"));
        assertEquals(Term.bool(false), evaluate("(not true)"));
        assertEquals(Term.bool(true), evaluate("(not false)"));
        assertEquals(Term.bool(true), evaluate("(not (not true))"));
    }

    @Test
    public void testConditionals() {
        assertEquals(Term.atom("yes"), evaluate("(if true yes no)"));
        assertEquals(Term.atom("no"), evaluate("(if false yes no)"));
        assertEquals(Term.atom("bigger"), evaluate("(if (> 5 3) bigger smaller)"));
        assertEquals(Term.atom("equal"), evaluate("(if (= 5 5) equal different)"));
        assertEquals(Term.atom("positive"), evaluate("(if (> 10 0) positive negative)"));
    }

    // ==========================================
    // Utility-Funktionen
    // ==========================================

    @Test
    public void testMinMax() {
        assertEquals(Term.number(3), evaluate("(min 5 3)"));
        assertEquals(Term.number(5), evaluate("(max 5 3)"));
        assertEquals(Term.number(-2), evaluate("(min -2 0)"));
        assertEquals(Term.number(0), evaluate("(max -2 0)"));
        assertEquals(Term.number(1), evaluate("(min 1 100)"));
        assertEquals(Term.number(100), evaluate("(max 1 100)"));
    }

    @Test
    public void testAbsoluteValue() {
        assertEquals(Term.number(5), evaluate("(abs 5)"));
        assertEquals(Term.number(5), evaluate("(abs -5)"));
        assertEquals(Term.number(0), evaluate("(abs 0)"));
        assertEquals(Term.number(42), evaluate("(abs -42)"));
    }

    @Test
    public void testEvenOdd() {
        assertEquals(Term.bool(true), evaluate("(even? 4)"));
        assertEquals(Term.bool(false), evaluate("(even? 3)"));
        assertEquals(Term.bool(true), evaluate("(odd? 3)"));
        assertEquals(Term.bool(false), evaluate("(odd? 4)"));
        assertEquals(Term.bool(true), evaluate("(even? 0)"));
        assertEquals(Term.bool(true), evaluate("(odd? 1)"));
        assertEquals(Term.bool(false), evaluate("(odd? 100)"));
    }

    // ==========================================
    // Listen-Operationen - JETZT MÖGLICH!
    // ==========================================

    @Test
    public void testListPredicates() {
        assertEquals(Term.bool(true), evaluate("(null? ())"));
        assertEquals(Term.bool(false), evaluate("(null? (a))"));
        assertEquals(Term.bool(false), evaluate("(null? (1 2 3))"));
    }

    @Test
    public void testListAccess() {
        assertEquals(Term.atom("a"), evaluate("(car (a b c))"));
        assertEquals(TermParser.parse("(b c)"), evaluate("(cdr (a b c))"));
        assertEquals(Term.number(1), evaluate("(car (1 2 3))"));
        assertEquals(TermParser.parse("(2 3)"), evaluate("(cdr (1 2 3))"));
        assertEquals(Term.atom("hello"), evaluate("(car (hello world))"));
    }

    @Test
    public void testListLength() {
        assertEquals(Term.number(0), evaluate("(length ())"));
        assertEquals(Term.number(1), evaluate("(length (a))"));
        assertEquals(Term.number(3), evaluate("(length (a b c))"));
        assertEquals(Term.number(4), evaluate("(length (1 2 3 4))"));
        assertEquals(Term.number(5), evaluate("(length (hello world this is test))"));
        assertEquals(Term.number(10), evaluate("(length (1 2 3 4 5 6 7 8 9 10))"));
    }

    @Test
    public void testListConstruction() {
        assertEquals(TermParser.parse("(a)"), evaluate("(cons a ())"));
        assertEquals(TermParser.parse("(a b c)"), evaluate("(cons a (b c))"));
        assertEquals(TermParser.parse("(0 1 2 3)"), evaluate("(cons 0 (1 2 3))"));
    }

    @Test
    public void testListAppend() {
        assertEquals(TermParser.parse("()"), evaluate("(append () ())"));
        assertEquals(TermParser.parse("(a b)"), evaluate("(append () (a b))"));
        assertEquals(TermParser.parse("(a b)"), evaluate("(append (a b) ())"));
        assertEquals(TermParser.parse("(a b c d)"), evaluate("(append (a b) (c d))"));
        assertEquals(TermParser.parse("(1 2 3 4 5)"), evaluate("(append (1 2) (3 4 5))"));
        assertEquals(TermParser.parse("(x y z a b c)"), evaluate("(append (x y z) (a b c))"));
    }

    @Test
    public void testListReverse() {
        assertEquals(TermParser.parse("()"), evaluate("(reverse ())"));
        assertEquals(TermParser.parse("(a)"), evaluate("(reverse (a))"));
        assertEquals(TermParser.parse("(c b a)"), evaluate("(reverse (a b c))"));
        assertEquals(TermParser.parse("(3 2 1)"), evaluate("(reverse (1 2 3))"));
        assertEquals(TermParser.parse("(4 3 2 1)"), evaluate("(reverse (1 2 3 4))"));
        assertEquals(TermParser.parse("(5 4 3 2 1)"), evaluate("(reverse (1 2 3 4 5))"));
    }

    // ==========================================
    // NEUE Mathematik-Funktionen
    // ==========================================

    @Test
    public void testNewMathFunctions() {
        assertEquals(Term.number(25), evaluate("(square 5)"));
        assertEquals(Term.number(9), evaluate("(square 3)"));
        assertEquals(Term.number(0), evaluate("(square 0)"));

        assertEquals(Term.number(125), evaluate("(cube 5)"));
        assertEquals(Term.number(27), evaluate("(cube 3)"));
        assertEquals(Term.number(8), evaluate("(cube 2)"));

        assertEquals(Term.number(1), evaluate("(pow 5 0)"));
        assertEquals(Term.number(5), evaluate("(pow 5 1)"));
        assertEquals(Term.number(25), evaluate("(pow 5 2)"));
        assertEquals(Term.number(8), evaluate("(pow 2 3)"));
        assertEquals(Term.number(16), evaluate("(pow 2 4)"));
    }

    // ==========================================
    // NEUE Aggregations-Funktionen
    // ==========================================

    @Test
    public void testSumFunction() {
        assertEquals(Term.number(0), evaluate("(sum ())"));
        assertEquals(Term.number(5), evaluate("(sum (5))"));
        assertEquals(Term.number(6), evaluate("(sum (1 2 3))"));
        assertEquals(Term.number(10), evaluate("(sum (1 2 3 4))"));
        assertEquals(Term.number(15), evaluate("(sum (1 2 3 4 5))"));
        assertEquals(Term.number(55), evaluate("(sum (1 2 3 4 5 6 7 8 9 10))"));
    }

    @Test
    public void testProductFunction() {
        assertEquals(Term.number(1), evaluate("(product ())"));
        assertEquals(Term.number(5), evaluate("(product (5))"));
        assertEquals(Term.number(6), evaluate("(product (1 2 3))"));
        assertEquals(Term.number(24), evaluate("(product (2 3 4))"));
        assertEquals(Term.number(120), evaluate("(product (1 2 3 4 5))"));
    }

    // ==========================================
    // NEUE Utility-Funktionen
    // ==========================================

    @Test
    public void testTakeDropFunctions() {
        assertEquals(TermParser.parse("()"), evaluate("(take 0 (a b c))"));
        assertEquals(TermParser.parse("(a)"), evaluate("(take 1 (a b c))"));
        assertEquals(TermParser.parse("(a b)"), evaluate("(take 2 (a b c))"));
        assertEquals(TermParser.parse("(a b c)"), evaluate("(take 3 (a b c))"));
        assertEquals(TermParser.parse("()"), evaluate("(take 5 ())"));

        assertEquals(TermParser.parse("(a b c)"), evaluate("(drop 0 (a b c))"));
        assertEquals(TermParser.parse("(b c)"), evaluate("(drop 1 (a b c))"));
        assertEquals(TermParser.parse("(c)"), evaluate("(drop 2 (a b c))"));
        assertEquals(TermParser.parse("()"), evaluate("(drop 3 (a b c))"));
        assertEquals(TermParser.parse("()"), evaluate("(drop 5 ())"));
    }

    @Test
    public void testRangeFunction() {
        assertEquals(TermParser.parse("()"), evaluate("(range 5 5)"));
        assertEquals(TermParser.parse("(1 2 3 4)"), evaluate("(range 1 5)"));
        assertEquals(TermParser.parse("(0 1 2)"), evaluate("(range 0 3)"));
        assertEquals(TermParser.parse("(3 4 5 6 7)"), evaluate("(range 3 8)"));
    }

    @Test
    public void testMemberFunction() {
        assertEquals(Term.bool(false), evaluate("(member? a ())"));
        assertEquals(Term.bool(true), evaluate("(member? a (a b c))"));
        assertEquals(Term.bool(true), evaluate("(member? b (a b c))"));
        assertEquals(Term.bool(true), evaluate("(member? c (a b c))"));
        assertEquals(Term.bool(false), evaluate("(member? d (a b c))"));
        assertEquals(Term.bool(true), evaluate("(member? 2 (1 2 3))"));
        assertEquals(Term.bool(false), evaluate("(member? 5 (1 2 3))"));
    }

    // ==========================================
    // Direkte Evaluator-Tests (: Syntax)
    // ==========================================

    @Test
    public void testDirectArithmeticEvaluation() {
        assertEquals(Term.number(7), interpreter.evaluate(TermParser.parse("(: + 3 4)")));
        assertEquals(Term.number(12), interpreter.evaluate(TermParser.parse("(: * 3 4)")));
        assertEquals(Term.bool(true), interpreter.evaluate(TermParser.parse("(: > 5 3)")));
        assertEquals(Term.bool(false), interpreter.evaluate(TermParser.parse("(: = 5 3)")));
    }

    @Test
    public void testArithmeticErrors() {
        assertThrows(ArithmeticException.class,
                () -> interpreter.evaluate(TermParser.parse("(: / 5 0)")));

        assertThrows(IllegalArgumentException.class,
                () -> interpreter.evaluate(TermParser.parse("(: + hello world)")));
    }

    // ==========================================
    // Edge Cases und Robustheit
    // ==========================================

    @Test
    public void testEmptyListOperations() {
        // Empty list handling
        assertEquals(TermParser.parse("(car ())"), evaluate("(car ())"));
        assertEquals(TermParser.parse("(cdr ())"), evaluate("(cdr ())"));
        assertEquals(Term.number(0), evaluate("(length ())"));
        assertEquals(Term.bool(true), evaluate("(null? ())"));
    }

    @Test
    public void testComplexNestedExpressions() {
        // Komplexe verschachtelte Ausdrücke
        assertEquals(Term.number(55), evaluate("(fib (length (1 2 3 4 5 6 7 8 9 10)))"));
        assertEquals(Term.number(6), evaluate("(+ (length (a b c)) (length (x y z)))"));
        assertEquals(Term.number(24), evaluate("(fact (length (a b c d)))"));
        assertEquals(Term.number(30), evaluate("(sum (range 1 6))"));
        assertEquals(Term.number(120), evaluate("(product (range 1 6))"));
    }

    @Test
    public void testRecursivePerformance() {
        // Test dass große rekursive Berechnungen funktionieren
        assertDoesNotThrow(() -> evaluate("(fact 10)"));
        assertDoesNotThrow(() -> evaluate("(fib 15)"));
        assertDoesNotThrow(() -> evaluate("(length (range 1 50))"));
        assertDoesNotThrow(() -> evaluate("(sum (range 1 100))"));
    }

    // ==========================================
    // Regression Tests
    // ==========================================

    @Test
    public void testOldFunctionalityStillWorks() {
        // Stelle sicher, dass alte Funktionalität noch funktioniert
        assertEquals(Term.number(3), evaluate("(+ 1 2)"));
        assertEquals(Term.bool(true), evaluate("(> 5 3)"));
        assertEquals(Term.bool(false), evaluate("(and true false)"));
        assertEquals(Term.atom("yes"), evaluate("(if true yes no)"));
        assertEquals(Term.number(5), evaluate("(abs -5)"));
        assertEquals(Term.number(3), evaluate("(min 5 3)"));
    }

    @Test
    public void testSystemIntegrity() {
        // Grundlegende System-Integrität
        assertNotNull(interpreter);
        assertNotNull(ruleSet);
        assertTrue(ruleSet.getAllRules().size() > 0);

        // Einfache Evaluation sollte immer funktionieren
        assertEquals(Term.atom("hello"), evaluate("hello"));
        assertEquals(Term.number(42), evaluate("42"));
        assertEquals(TermParser.parse("()"), evaluate("()"));
    }
}