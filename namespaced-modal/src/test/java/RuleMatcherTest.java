package core;

import io.TermParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import java.util.Optional;

public class RuleMatcherTest {

    @Test
    public void testMatchAtom() {
        Term pattern = TermParser.parse("hello");
        Term term = TermParser.parse("hello");

        Optional<Map<String, Term>> result = RuleMatcher.match(pattern, term);
        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }

    @Test
    public void testMatchAtomFail() {
        Term pattern = TermParser.parse("hello");
        Term term = TermParser.parse("world");

        Optional<Map<String, Term>> result = RuleMatcher.match(pattern, term);
        assertFalse(result.isPresent());
    }

    @Test
    public void testMatchVariable() {
        Term pattern = TermParser.parse("?x");
        Term term = TermParser.parse("hello");

        Optional<Map<String, Term>> result = RuleMatcher.match(pattern, term);
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals("hello", ((Term.Atom) result.get().get("?x")).value());
    }

    @Test
    public void testMatchVariableConsistency() {
        Term pattern = TermParser.parse("(f ?x ?x)");
        Term term = TermParser.parse("(f hello hello)");

        Optional<Map<String, Term>> result = RuleMatcher.match(pattern, term);
        assertTrue(result.isPresent());
        assertEquals("hello", ((Term.Atom) result.get().get("?x")).value());
    }

    @Test
    public void testMatchVariableInconsistency() {
        Term pattern = TermParser.parse("(f ?x ?x)");
        Term term = TermParser.parse("(f hello world)");

        Optional<Map<String, Term>> result = RuleMatcher.match(pattern, term);
        assertFalse(result.isPresent());
    }

    @Test
    public void testMatchList() {
        Term pattern = TermParser.parse("(+ ?a ?b)");
        Term term = TermParser.parse("(+ 1 2)");

        Optional<Map<String, Term>> result = RuleMatcher.match(pattern, term);
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        assertEquals("1", ((Term.Atom) result.get().get("?a")).value());
        assertEquals("2", ((Term.Atom) result.get().get("?b")).value());
    }

    @Test
    public void testMatchListSizeMismatch() {
        Term pattern = TermParser.parse("(+ ?a ?b)");
        Term term = TermParser.parse("(+ 1 2 3)");

        Optional<Map<String, Term>> result = RuleMatcher.match(pattern, term);
        assertFalse(result.isPresent());
    }

    @Test
    public void testMatchListPattern() {
        Term pattern = TermParser.parse("(?head . ?tail)");
        Term term = TermParser.parse("(a b c)");

        Optional<Map<String, Term>> result = RuleMatcher.match(pattern, term);
        assertTrue(result.isPresent());
        assertEquals(2, result.get().size());
        assertEquals("a", ((Term.Atom) result.get().get("?head")).value());

        Term.List tail = (Term.List) result.get().get("?tail");
        assertEquals(2, tail.elements().size());
        assertEquals("b", ((Term.Atom) tail.elements().get(0)).value());
        assertEquals("c", ((Term.Atom) tail.elements().get(1)).value());
    }

    @Test
    public void testMatchListPatternEmptyList() {
        Term pattern = TermParser.parse("(?head . ?tail)");
        Term term = TermParser.parse("()");

        Optional<Map<String, Term>> result = RuleMatcher.match(pattern, term);
        assertFalse(result.isPresent());
    }

    @Test
    public void testSubstitute() {
        Term template = TermParser.parse("(result ?x ?y)");
        Map<String, Term> bindings = Map.of(
                "?x", new Term.Atom("hello"),
                "?y", new Term.Atom("world")
        );

        Term result = RuleMatcher.substitute(template, bindings);
        assertEquals("(result hello world)", result.toString());
    }

    @Test
    public void testSubstituteNested() {
        Term template = TermParser.parse("(outer (inner ?x) ?y)");
        Map<String, Term> bindings = Map.of(
                "?x", new Term.Atom("test"),
                "?y", new Term.Atom("value")
        );

        Term result = RuleMatcher.substitute(template, bindings);
        assertEquals("(outer (inner test) value)", result.toString());
    }
}