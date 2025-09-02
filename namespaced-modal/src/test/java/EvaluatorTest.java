package core;

import io.TermParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import io.RuleParser;
import debug.Debugger;


public class EvaluatorTest {

    private RuleSet ruleSet;
    private Rewriter rewriter;
    private Debugger debugger;

    @BeforeEach
    public void setUp() {
        ruleSet = RuleParser.loadFromResource("rules/standard.modal");
        debugger = new Debugger(Debugger.Mode.QUIET);
        rewriter = new Rewriter(ruleSet, debugger);
    }

    private Term evaluate(String input) {
        Term term = TermParser.parse(input);
        Term rewritten = rewriter.rewrite(term);
        return Evaluator.evaluate(rewritten);
    }


    // ==========================================
    // Existing Basic Tests
    // ==========================================

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

    // ==========================================
    // Comparison Operators Tests
    // ==========================================

    @Test
    public void testEvaluateGreaterThan() {
        Term term = TermParser.parse("(: > 5 3)");
        Term result = Evaluator.evaluate(term);
        
        assertTrue(result instanceof Term.Atom);
        Term.Atom atom = (Term.Atom) result;
        assertTrue(atom.isBoolean());
        assertTrue(atom.asBoolean());
    }

    @Test
    public void testEvaluateLessThan() {
        Term term = TermParser.parse("(: < 3 5)");
        Term result = Evaluator.evaluate(term);
        
        assertTrue(result instanceof Term.Atom);
        assertEquals(true, ((Term.Atom) result).asBoolean());
    }

    @Test
    public void testEvaluateEquals() {
        Term term = TermParser.parse("(: == 5 5)");
        Term result = Evaluator.evaluate(term);
        
        assertTrue(result instanceof Term.Atom);
        assertEquals(true, ((Term.Atom) result).asBoolean());
    }

    @Test
    public void testEvaluateGreaterThanOrEqual() {
        Term term = TermParser.parse("(: >= 5 5)");
        Term result = Evaluator.evaluate(term);
        
        assertTrue(result instanceof Term.Atom);
        assertEquals(true, ((Term.Atom) result).asBoolean());
    }

    @Test
    public void testEvaluateLessThanOrEqual() {
        Term term = TermParser.parse("(: <= 3 5)");
        Term result = Evaluator.evaluate(term);
        
        assertTrue(result instanceof Term.Atom);
        assertEquals(true, ((Term.Atom) result).asBoolean());
    }

    @Test
    public void testComparisonWithNonNumbers() {
        Term term = TermParser.parse("(: > hello world)");
        assertThrows(RuntimeException.class, () -> Evaluator.evaluate(term));
    }

    // ==========================================
    // Mathematical Function Tests
    // ==========================================

    @Test
    public void testPowerFunction() {
        assertEquals(Term.number(1), evaluate("(pow 5 0)"));
        assertEquals(Term.number(5), evaluate("(pow 5 1)"));
        assertEquals(Term.number(25), evaluate("(pow 5 2)"));
        assertEquals(Term.number(8), evaluate("(pow 2 3)"));
    }

    @Test
    public void testFactorial() {
        assertEquals(Term.number(1), evaluate("(fact 0)"));
        assertEquals(Term.number(1), evaluate("(fact 1)"));
        assertEquals(Term.number(2), evaluate("(fact 2)"));
        assertEquals(Term.number(6), evaluate("(fact 3)"));
        assertEquals(Term.number(24), evaluate("(fact 4)"));
        assertEquals(Term.number(120), evaluate("(fact 5)"));
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
    }

    // ==========================================
    // List Operation Tests
    // ==========================================

    @Test
    public void testListBasicOperations() {
        assertEquals(Term.atom("a"), evaluate("(first (a b c))"));
        assertEquals(TermParser.parse("(b c)"), evaluate("(rest (a b c))"));
        assertEquals(TermParser.parse("(x a b c)"), evaluate("(cons x (a b c))"));
    }

    @Test
    public void testListPredicates() {
        assertEquals(Term.bool(true), evaluate("(empty? ())"));
        assertEquals(Term.bool(false), evaluate("(empty? (a b))"));
    }

    @Test
    public void testListLength() {
        assertEquals(Term.number(0), evaluate("(length ())"));
        assertEquals(Term.number(1), evaluate("(length (a))"));
        assertEquals(Term.number(3), evaluate("(length (a b c))"));
    }

    @Test
    public void testListAppend() {
        assertEquals(TermParser.parse("(a b c)"), evaluate("(append () (a b c))"));
        assertEquals(TermParser.parse("(a b c d e)"), evaluate("(append (a b) (c d e))"));
    }

    @Test
    public void testListReverse() {
        assertEquals(TermParser.parse("()"), evaluate("(reverse ())"));
        assertEquals(TermParser.parse("(a)"), evaluate("(reverse (a))"));
        assertEquals(TermParser.parse("(c b a)"), evaluate("(reverse (a b c))"));
    }

    @Test
    public void testListElementAccess() {
        assertEquals(Term.atom("b"), evaluate("(second (a b c))"));
        assertEquals(Term.atom("c"), evaluate("(third (a b c d))"));
    }

    @Test
    public void testListAggregation() {
        assertEquals(Term.number(0), evaluate("(sum ())"));
        assertEquals(Term.number(6), evaluate("(sum (1 2 3))"));
        assertEquals(Term.number(1), evaluate("(product ())"));
        assertEquals(Term.number(24), evaluate("(product (2 3 4))"));
    }

    @Test
    public void testListSubsets() {
        assertEquals(TermParser.parse("()"), evaluate("(take 0 (a b c))"));
        assertEquals(TermParser.parse("(a b)"), evaluate("(take 2 (a b c d))"));
        assertEquals(TermParser.parse("(a b c)"), evaluate("(drop 0 (a b c))"));
        assertEquals(TermParser.parse("(c d)"), evaluate("(drop 2 (a b c d))"));
    }

    // ==========================================
    // Boolean Logic Tests
    // ==========================================

    @Test
    public void testBooleanOperations() {
        assertEquals(Term.bool(false), evaluate("(and false true)"));
        assertEquals(Term.bool(true), evaluate("(and true true)"));
        assertEquals(Term.bool(true), evaluate("(or true false)"));
        assertEquals(Term.bool(false), evaluate("(or false false)"));
        assertEquals(Term.bool(false), evaluate("(not true)"));
        assertEquals(Term.bool(true), evaluate("(not false)"));
    }

    // ==========================================
    // Conditional Expression Tests
    // ==========================================

    @Test
    public void testConditionals() {
        assertEquals(Term.atom("yes"), evaluate("(if true yes no)"));
        assertEquals(Term.atom("no"), evaluate("(if false yes no)"));
    }

    @Test
    public void testConditionalWithComparisons() {
        assertEquals(Term.atom("bigger"), evaluate("(if (> 5 3) bigger smaller)"));
        assertEquals(Term.atom("equal"), evaluate("(if (== 5 5) equal different)"));
    }

    // ==========================================
    // Unit System Tests
    // ==========================================

    @Test
    public void testUnitCreation() {
        assertEquals(TermParser.parse("(unit 5 m)"), evaluate("(unit 5 m)"));
        assertEquals(Term.number(5), evaluate("(unit 5 1)"));
    }

    @Test
    public void testLengthConversions() {
        assertEquals(TermParser.parse("(unit 5 m)"), evaluate("(to-base (unit 5 m))"));
        assertEquals(TermParser.parse("(unit 5000 m)"), evaluate("(to-base (unit 5 km))"));
        assertEquals(TermParser.parse("(unit 0.05 m)"), evaluate("(to-base (unit 5 cm))"));
    }

    @Test
    public void testLengthArithmetic() {
        assertEquals(TermParser.parse("(unit 8 m)"), evaluate("(+ (unit 3 m) (unit 5 m))"));
        assertEquals(TermParser.parse("(unit 2 m)"), evaluate("(- (unit 5 m) (unit 3 m))"));
    }

    @Test
    public void testMassConversions() {
        assertEquals(TermParser.parse("(unit 2 kg)"), evaluate("(to-base (unit 2 kg))"));
        assertEquals(TermParser.parse("(unit 2 kg)"), evaluate("(to-base (unit 2000 g))"));
        assertEquals(TermParser.parse("(unit 2000 kg)"), evaluate("(to-base (unit 2 t))"));
    }

    @Test
    public void testTimeConversions() {
        assertEquals(TermParser.parse("(unit 60 s)"), evaluate("(to-base (unit 1 min))"));
        assertEquals(TermParser.parse("(unit 3600 s)"), evaluate("(to-base (unit 1 h))"));
        assertEquals(TermParser.parse("(unit 86400 s)"), evaluate("(to-base (unit 1 day))"));
    }

    @Test
    public void testVelocityConversions() {
        assertEquals(TermParser.parse("(unit 10 m/s)"), evaluate("(to-base (unit 10 m/s))"));
        // 36 km/h = 10 m/s
        assertEquals(TermParser.parse("(unit 10 m/s)"), evaluate("(to-base (unit 36 km/h))"));
    }

    @Test
    public void testUnitArithmetic() {
        // Skalar-Multiplikation
        assertEquals(TermParser.parse("(unit 15 m)"), evaluate("(* 3 (unit 5 m))"));
        assertEquals(TermParser.parse("(unit 15 m)"), evaluate("(* (unit 5 m) 3)"));
        
        // Division
        assertEquals(TermParser.parse("(unit 5 m)"), evaluate("(/ (unit 15 m) 3)"));
    }

    @Test
    public void testUnitMultiplication() {
        // Fläche = Länge * Länge
        assertEquals(TermParser.parse("(unit 15 (* m m))"), 
                    evaluate("(* (unit 3 m) (unit 5 m))"));
    }

    @Test
    public void testUnitDivision() {
        // Geschwindigkeit = Strecke / Zeit
        assertEquals(TermParser.parse("(unit 5 (/ m s))"), 
                    evaluate("(/ (unit 10 m) (unit 2 s))"));
    }

    @Test
    public void testEnergyConversions() {
        assertEquals(TermParser.parse("(unit 1000 J)"), evaluate("(to-base (unit 1 kJ))"));
        assertEquals(TermParser.parse("(unit 3600 J)"), evaluate("(to-base (unit 1 Wh))"));
        assertEquals(TermParser.parse("(unit 4184 J)"), evaluate("(to-base (unit 1 kcal))"));
    }

    @Test
    public void testForceConversions() {
        assertEquals(TermParser.parse("(unit 1000 N)"), evaluate("(to-base (unit 1 kN))"));
    }

    @Test
    public void testPowerConversions() {
        assertEquals(TermParser.parse("(unit 1000 W)"), evaluate("(to-base (unit 1 kW))"));
        assertEquals(TermParser.parse("(unit 745.7 W)"), evaluate("(to-base (unit 1 hp))"));
    }

    @Test
    public void testTemperatureConversions() {
        assertEquals(TermParser.parse("(unit 273.15 K)"), evaluate("(to-base (unit 0 °C))"));
        assertEquals(TermParser.parse("(unit 373.15 K)"), evaluate("(to-base (unit 100 °C))"));
    }

    @Test
    public void testUnitExtraction() {
        assertEquals(Term.number(5), evaluate("(value (unit 5 m))"));
        assertEquals(Term.atom("m"), evaluate("(unit-of (unit 5 m))"));
    }

    @Test
    public void testUnitCompatibility() {
        assertEquals(Term.bool(true), evaluate("(compatible? (unit 5 m) (unit 3 m))"));
    }

    @Test
    public void testUnitConversionChain() {
        // Konvertiere 1 km zu cm über Basiseinheit
        assertEquals(TermParser.parse("(unit 100000 cm)"), 
                    evaluate("(convert (unit 1 km) cm)"));
        
        // Konvertiere 1 kg zu g
        assertEquals(TermParser.parse("(unit 1000 g)"), 
                    evaluate("(convert (unit 1 kg) g)"));
        
        // Konvertiere 1 h zu min
        assertEquals(TermParser.parse("(unit 60 min)"), 
                    evaluate("(convert (unit 1 h) min)"));
    }

    // ==========================================
    // Complex Calculation Tests
    // ==========================================

    @Test
    public void testPhysicsCalculations() {
        // Kraft = Masse * Beschleunigung
        assertEquals(TermParser.parse("(unit 20 N)"), 
                    evaluate("(* (unit 2 kg) (unit 10 m/s²))"));
        
        // Energie = Kraft * Weg
        assertEquals(TermParser.parse("(unit 200 J)"), 
                    evaluate("(* (unit 20 N) (unit 10 m))"));
        
        // Leistung = Energie / Zeit
        assertEquals(TermParser.parse("(unit 20 (/ J s))"), 
                    evaluate("(/ (unit 200 J) (unit 10 s))"));
    }

    @Test
    public void testComplexListCalculations() {
        // Summe der ersten 5 Fakultäten
        assertEquals(Term.number(153), // 1+1+2+6+24+120
                    evaluate("(sum ((fact 0) (fact 1) (fact 2) (fact 3) (fact 4) (fact 5)))"));
        
        // Produkt der ersten 4 Fibonacci-Zahlen (ohne 0)
        assertEquals(Term.number(6), // 1*1*2*3
                    evaluate("(product ((fib 1) (fib 2) (fib 3) (fib 4)))"));
    }

    @Test
    public void testNestedConditionals() {
        assertEquals(Term.atom("positive"), 
                    evaluate("(if (> 5 0) positive (if (< 5 0) negative zero))"));
        
        assertEquals(Term.atom("zero"), 
                    evaluate("(if (> 0 0) positive (if (< 0 0) negative zero))"));
    }

    @Test
    public void testComplexBooleanExpressions() {
        // (5 > 3) AND (2 < 4)
        assertEquals(Term.bool(true), 
                    evaluate("(and (> 5 3) (< 2 4))"));
        
        // NOT((5 < 3) OR (2 > 4))
        assertEquals(Term.bool(true), 
                    evaluate("(not (or (< 5 3) (> 2 4)))"));
    }

    // ==========================================
    // Error Handling Tests
    // ==========================================

    @Test
    public void testListOperationErrors() {
        // first auf leere Liste sollte einen Fehler geben (je nach Implementierung)
        // assertEquals(null, evaluate("(first ()))")); // oder Exception
        
        // take mit negativer Zahl
        assertThrows(RuntimeException.class, () -> evaluate("(take -1 (a b c))"));
    }

    @Test
    public void testUnitOperationErrors() {
        // Inkompatible Einheiten addieren
        assertThrows(RuntimeException.class, () -> evaluate("(+ (unit 5 m) (unit 3 kg))"));
    }

    @Test
    public void testMathematicalEdgeCases() {
        // Fakultät von negativer Zahl
        assertThrows(RuntimeException.class, () -> evaluate("(fact -1)"));
        
        // Fibonacci von negativer Zahl
        assertThrows(RuntimeException.class, () -> evaluate("(fib -1)"));
        
        // Potenz mit sehr großen Zahlen (Stack Overflow möglich)
        // assertEquals(?, evaluate("(pow 2 1000)"));
    }
}
