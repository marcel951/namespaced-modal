package core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TermTest {

    @Test
    public void testAtomCreation() {
        Term.Atom atom = new Term.Atom("hello");
        assertEquals("hello", atom.value());
        assertEquals("hello", atom.toString());
    }

    @Test
    public void testAtomNumbers() {
        Term.Atom num = new Term.Atom("42");
        assertTrue(num.isNumber());
        assertEquals(42, num.asNumber());
        assertFalse(num.isBoolean());
        assertFalse(num.isVariable());
    }

    @Test
    public void testAtomBooleans() {
        Term.Atom trueAtom = new Term.Atom("true");
        Term.Atom falseAtom = new Term.Atom("false");

        assertTrue(trueAtom.isBoolean());
        assertTrue(falseAtom.isBoolean());
        assertTrue(trueAtom.asBoolean());
        assertFalse(falseAtom.asBoolean());
    }

    @Test
    public void testAtomVariables() {
        Term.Atom var = new Term.Atom("?x");
        assertTrue(var.isVariable());
        assertFalse(var.isNumber());
        assertFalse(var.isBoolean());
    }

    @Test
    public void testListCreation() {
        Term.List list = new Term.List(
                new Term.Atom("hello"),
                new Term.Atom("world")
        );

        assertEquals(2, list.elements().size());
        assertEquals("hello", ((Term.Atom) list.elements().get(0)).value());
        assertEquals("world", ((Term.Atom) list.elements().get(1)).value());
    }

    @Test
    public void testListOperations() {
        Term.List list = new Term.List(
                new Term.Atom("first"),
                new Term.Atom("second"),
                new Term.Atom("third")
        );

        assertFalse(list.isEmpty());
        assertEquals("first", ((Term.Atom) list.head()).value());

        Term.List tail = list.tail();
        assertEquals(2, tail.elements().size());
        assertEquals("second", ((Term.Atom) tail.head()).value());
    }

    @Test
    public void testEmptyList() {
        Term.List empty = new Term.List();
        assertTrue(empty.isEmpty());

        assertThrows(IllegalStateException.class, empty::head);
        assertThrows(IllegalStateException.class, empty::tail);
    }

    @Test
    public void testFunctionSymbol() {
        Term.List list = new Term.List(
                new Term.Atom("+"),
                new Term.Atom("1"),
                new Term.Atom("2")
        );

        assertEquals("+", list.getFunctionSymbol());
    }

    @Test
    public void testStaticFactoryMethods() {
        Term atom = Term.atom("test");
        Term number = Term.number(42);
        Term bool = Term.bool(true);
        Term list = Term.list(atom, number, bool);

        assertEquals("test", ((Term.Atom) atom).value());
        assertEquals("42", ((Term.Atom) number).value());
        assertEquals("true", ((Term.Atom) bool).value());
        assertEquals(3, ((Term.List) list).elements().size());
    }
}