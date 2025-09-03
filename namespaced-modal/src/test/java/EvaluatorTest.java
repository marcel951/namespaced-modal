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
        try {
            System.out.println("Loading rules from: rules/standard.modal");
            ruleSet = RuleParser.loadFromResource("rules/standard.modal");
            System.out.println("Loaded " + ruleSet.getAllRules().size() + " rules");
            
            debugger = new Debugger(Debugger.Mode.QUIET);
            rewriter = new Rewriter(ruleSet, debugger);
            System.out.println("Setup completed successfully");
        } catch (Exception e) {
            System.err.println("Error in setUp: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to load rules for test", e);
        }
    }

    private Term evaluate(String input) {
        Term term = TermParser.parse(input);
        Term rewritten = rewriter.rewrite(term);
        return Evaluator.evaluate(rewritten);
    }

    // ==========================================
    // Basistests
    // ==========================================

    @Test
    public void testEvaluateNonArithmetic() {
        Term term = TermParser.parse("(hello world)");
        Term result = Evaluator.evaluate(term);
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
    public void testDivisionByZero() {
        assertThrows(ArithmeticException.class, () -> evaluate("(/ 5 0)"));
    }

    @Test
    public void testMathExtensions() {
        // Potenz-Tests
        assertEquals(Term.number(1), evaluate("(pow 5 0)"));
        assertEquals(Term.number(5), evaluate("(pow 5 1)"));
        assertEquals(Term.number(25), evaluate("(pow 5 2)"));
        assertEquals(Term.number(125), evaluate("(pow 5 3)"));
        
        // Fakultät-Tests
        assertEquals(Term.number(1), evaluate("(fact 0)"));
        assertEquals(Term.number(1), evaluate("(fact 1)"));
        assertEquals(Term.number(2), evaluate("(fact 2)"));
        assertEquals(Term.number(6), evaluate("(fact 3)"));
        assertEquals(Term.number(24), evaluate("(fact 4)"));
        assertEquals(Term.number(120), evaluate("(fact 5)"));
    }

    // ==========================================
    // Vergleiche und Logik
    // ==========================================

    @Test
    public void testComparisons() {
        assertEquals(Term.bool(true),  evaluate("(> 5 3)"));
        assertEquals(Term.bool(true),  evaluate("(< 3 5)"));
        assertEquals(Term.bool(true),  evaluate("(= 5 5)"));
        assertEquals(Term.bool(true),  evaluate("(>= 5 5)"));
        assertEquals(Term.bool(true),  evaluate("(<= 3 5)"));
        assertEquals(Term.bool(true),  evaluate("(!= 7 8)"));
    }

    @Test
    public void testBooleanOperations() {
        assertEquals(Term.bool(false), evaluate("(and false true)"));
        assertEquals(Term.bool(true),  evaluate("(and true true)"));
        assertEquals(Term.bool(true),  evaluate("(or true false)"));
        assertEquals(Term.bool(false), evaluate("(or false false)"));
        assertEquals(Term.bool(false), evaluate("(not true)"));
        assertEquals(Term.bool(true),  evaluate("(not false)"));
        assertEquals(Term.bool(true),  evaluate("(not (not true))"));
    }

    @Test
    public void testConditionals() {
        assertEquals(Term.atom("yes"), evaluate("(if true yes no)"));
        assertEquals(Term.atom("no"),  evaluate("(if false yes no)"));
        assertEquals(Term.atom("bigger"), evaluate("(if (> 5 3) bigger smaller)"));
        assertEquals(Term.atom("equal"),  evaluate("(if (= 5 5) equal different)"));
    }

    // ==========================================
    // Zusätzliche Mathe-Funktionen
    // ==========================================

    @Test
    public void testMinMax() {
        assertEquals(Term.number(3), evaluate("(min 5 3)"));
        assertEquals(Term.number(5), evaluate("(max 5 3)"));
        assertEquals(Term.number(-2), evaluate("(min -2 0)"));
        assertEquals(Term.number(0), evaluate("(max -2 0)"));
    }

    @Test
    public void testAbsoluteValue() {
        assertEquals(Term.number(5), evaluate("(abs 5)"));
        assertEquals(Term.number(5), evaluate("(abs -5)"));
        assertEquals(Term.number(0), evaluate("(abs 0)"));
    }

    @Test
    public void testEvenOdd() {
        assertEquals(Term.bool(true), evaluate("(even? 4)"));
        assertEquals(Term.bool(false), evaluate("(even? 3)"));
        assertEquals(Term.bool(true), evaluate("(odd? 3)"));
        assertEquals(Term.bool(false), evaluate("(odd? 4)"));
        assertEquals(Term.bool(true), evaluate("(even? 0)"));
    }

    // ==========================================
    // Listen-Tests mit Standard-Format 
    // ==========================================

    @Test
    public void testListPredicates() {
        assertEquals(Term.bool(true),  evaluate("(empty? ())"));
        assertEquals(Term.bool(false), evaluate("(empty? (a))"));
        assertEquals(Term.bool(false), evaluate("(empty? (1 2 3))"));
    }

    
    @Test
    public void testListAccess() {
        // Test first, second, third
        assertEquals(Term.atom("a"), evaluate("(first (a b c))"));
        assertEquals(Term.atom("b"), evaluate("(second (a b c))"));
        assertEquals(Term.atom("c"), evaluate("(third (a b c d))"));
        
        assertEquals(Term.number(1), evaluate("(first (1 2 3))"));
        assertEquals(Term.number(2), evaluate("(second (1 2 3))"));
        assertEquals(Term.number(3), evaluate("(third (1 2 3))"));
    }
    
    
    @Test
    public void testListRest() {
        // Test rest (alle Elemente außer dem ersten)
        assertEquals(TermParser.parse("(b c)"), evaluate("(rest (a b c))"));
        assertEquals(TermParser.parse("(2 3)"), evaluate("(rest (1 2 3))"));
        assertEquals(TermParser.parse("()"), evaluate("(rest (single))"));
    }
    
    @Test
    public void testListLength() {
        // Test length Funktion
        assertEquals(Term.number(0), evaluate("(length ())"));
        assertEquals(Term.number(1), evaluate("(length (a))"));
        assertEquals(Term.number(3), evaluate("(length (a b c))"));
        assertEquals(Term.number(4), evaluate("(length (1 2 3 4))"));
        assertEquals(Term.number(5), evaluate("(length (hello world this is test))"));
    }

    @Test
    public void testListReverse() {
        // Test reverse Funktion
        assertEquals(TermParser.parse("()"), evaluate("(reverse ())"));
        assertEquals(TermParser.parse("(a)"), evaluate("(reverse (a))"));
        assertEquals(TermParser.parse("(c b a)"), evaluate("(reverse (a b c))"));
        assertEquals(TermParser.parse("(3 2 1)"), evaluate("(reverse (1 2 3))"));
        assertEquals(TermParser.parse("(4 3 2 1)"), evaluate("(reverse (1 2 3 4))"));
    }

    
    @Test
    public void testListAppend() {
        // Test append Funktion
        assertEquals(TermParser.parse("()"), evaluate("(append () ())"));
        assertEquals(TermParser.parse("(a b)"), evaluate("(append () (a b))"));
        assertEquals(TermParser.parse("(a b)"), evaluate("(append (a b) ())"));
        assertEquals(TermParser.parse("(a b c d)"), evaluate("(append (a b) (c d))"));
        assertEquals(TermParser.parse("(1 2 3 4 5)"), evaluate("(append (1 2) (3 4 5))"));
    }
    

    
    @Test
    public void testListCons() {
        // Test cons Funktion (Element hinzufügen)
        assertEquals(TermParser.parse("(a)"), evaluate("(cons a ())"));
        assertEquals(TermParser.parse("(a b c)"), evaluate("(cons a (b c))"));
        assertEquals(TermParser.parse("(0 1 2 3)"), evaluate("(cons 0 (1 2 3))"));
    }
    
    @Test
    public void testListAggregation() {
        // Test sum und product
        assertEquals(Term.number(0), evaluate("(sum ())"));
        assertEquals(Term.number(6), evaluate("(sum (1 2 3))"));
        assertEquals(Term.number(10), evaluate("(sum (1 2 3 4))"));
        assertEquals(Term.number(15), evaluate("(sum (5 10))"));

        assertEquals(Term.number(1), evaluate("(product ())"));
        assertEquals(Term.number(6), evaluate("(product (1 2 3))"));
        assertEquals(Term.number(24), evaluate("(product (2 3 4))"));
        assertEquals(Term.number(120), evaluate("(product (1 2 3 4 5))"));
    }

    
    @Test
    public void testListTakeDrop() {
        // Test take
        assertEquals(TermParser.parse("()"), evaluate("(take 0 (a b c))"));
        assertEquals(TermParser.parse("()"), evaluate("(take 5 ())"));
        assertEquals(TermParser.parse("(a)"), evaluate("(take 1 (a b c))"));
        assertEquals(TermParser.parse("(1 2)"), evaluate("(take 2 (1 2 3 4))"));

        // Test drop
        assertEquals(TermParser.parse("(a b c)"), evaluate("(drop 0 (a b c))"));
        assertEquals(TermParser.parse("()"), evaluate("(drop 5 ())"));
        assertEquals(TermParser.parse("(b c)"), evaluate("(drop 1 (a b c))"));
        assertEquals(TermParser.parse("(3 4)"), evaluate("(drop 2 (1 2 3 4))"));
    }
    

    // ==========================================
    // Direkte Evaluator-Tests
    // ==========================================

    @Test
    public void testDirectArithmeticEvaluation() {
        assertEquals(Term.number(7), Evaluator.evaluate(TermParser.parse("(: + 3 4)")));
        assertEquals(Term.number(12), Evaluator.evaluate(TermParser.parse("(: * 3 4)")));
        assertEquals(Term.bool(true), Evaluator.evaluate(TermParser.parse("(: > 5 3)")));
    }

    @Test
    public void testArithmeticErrors() {
        assertThrows(ArithmeticException.class, 
                    () -> Evaluator.evaluate(TermParser.parse("(: / 5 0)")));
        
        assertThrows(IllegalArgumentException.class,
                    () -> Evaluator.evaluate(TermParser.parse("(: + hello world)")));
    }

    // ==========================================
    // Edge Cases und Fehlerbehandlung
    // ==========================================

    @Test
    public void testListOperationErrors() {
        // Tests für Operationen auf leeren Listen oder ungültigen Zuständen
        // Diese sollten unverändert bleiben, da keine passenden Regeln existieren
        assertEquals(TermParser.parse("(first ())"), evaluate("(first ())"));
        assertEquals(TermParser.parse("(second ())"), evaluate("(second ())"));
        assertEquals(TermParser.parse("(second (a))"), evaluate("(second (a))"));
    }

    
    @Test
    public void testComplexListOperations() {
        // Komplexere Kombinationen von Listen-Operationen
        assertEquals(Term.number(6), evaluate("(length (reverse (a b c d e f)))"));
        assertEquals(Term.number(10), evaluate("(sum (reverse (1 2 3 4)))"));
        
        // Verschachtelte Operationen
        assertEquals(TermParser.parse("(1 2 3 4 5)"), 
                    evaluate("(append (1 2) (append (3) (4 5)))"));
        
        assertEquals(Term.atom("c"), 
                    evaluate("(first (reverse (a b c)))"));
        
        /* 
        // Kombinationen mit Mathe-Funktionen
        assertEquals(Term.number(25), evaluate("(pow (length (a b c d e)) 2)"));
        assertEquals(Term.bool(true), evaluate("(even? (sum (2 4 6)))"));
        assertEquals(Term.number(10), evaluate("(max (sum (1 2 3)) (product (2 5)))"));
        */
    }
    
    /*
    @Test
    public void testUnknownFunctions() {
        // Funktionen die nicht in den Regeln definiert sind
        assertEquals(TermParser.parse("(take 2 (a b c))"), evaluate("(take 2 (a b c))"));
        assertEquals(TermParser.parse("(drop 1 (a b c))"), evaluate("(drop 1 (a b c))"));
        assertEquals(TermParser.parse("(filter even (1 2 3 4))"), evaluate("(filter even (1 2 3 4))"));
    }
    */
}