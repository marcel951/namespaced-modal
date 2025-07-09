package io;

import core.Term;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TermParserTest {

    @Test
    public void testParseAtom() {
        Term term = TermParser.parse("hello");
        assertTrue(term instanceof Term.Atom);
        assertEquals("hello", ((Term.Atom) term).value());
    }

    @Test
    public void testParseNumber() {
        Term term = TermParser.parse("42");
        assertTrue(term instanceof Term.Atom);
        Term.Atom atom = (Term.Atom) term;
        assertTrue(atom.isNumber());
        assertEquals(42, atom.asNumber());
    }

    @Test
    public void testParseVariable() {
        Term term = TermParser.parse("?x");
        assertTrue(term instanceof Term.Atom);
        Term.Atom atom = (Term.Atom) term;
        assertTrue(atom.isVariable());
        assertEquals("?x", atom.value());
    }

    @Test
    public void testParseEmptyList() {
        Term term = TermParser.parse("()");
        assertTrue(term instanceof Term.List);
        assertTrue(((Term.List) term).isEmpty());
    }

    @Test
    public void testParseSimpleList() {
        Term term = TermParser.parse("(+ 1 2)");
        assertTrue(term instanceof Term.List);

        Term.List list = (Term.List) term;
        assertEquals(3, list.elements().size());
        assertEquals("+", ((Term.Atom) list.elements().get(0)).value());
        assertEquals("1", ((Term.Atom) list.elements().get(1)).value());
        assertEquals("2", ((Term.Atom) list.elements().get(2)).value());
    }

    @Test
    public void testParseNestedList() {
        Term term = TermParser.parse("(+ (* 2 3) 4)");
        assertTrue(term instanceof Term.List);

        Term.List list = (Term.List) term;
        assertEquals(3, list.elements().size());
        assertEquals("+", ((Term.Atom) list.elements().get(0)).value());

        // Second element should be a nested list
        assertTrue(list.elements().get(1) instanceof Term.List);
        Term.List nested = (Term.List) list.elements().get(1);
        assertEquals(3, nested.elements().size());
        assertEquals("*", ((Term.Atom) nested.elements().get(0)).value());
    }

    @Test
    public void testParseWithWhitespace() {
        Term term = TermParser.parse("  (  +   1   2  )  ");
        assertTrue(term instanceof Term.List);

        Term.List list = (Term.List) term;
        assertEquals(3, list.elements().size());
        assertEquals("+", ((Term.Atom) list.elements().get(0)).value());
    }

}