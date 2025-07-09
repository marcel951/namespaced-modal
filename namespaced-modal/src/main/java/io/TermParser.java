package io;

import core.Term;

import java.util.ArrayList;
import java.util.List;

public class TermParser {
    private String input;
    private int pos;

    public static Term parse(String input) {
        return new TermParser(input).parseTerm();
    }

    private TermParser(String input) {
        this.input = input.trim();
        this.pos = 0;
    }

    private Term parseTerm() {
        skipWhitespace();

        if (pos >= input.length()) {
            throw new IllegalArgumentException("Unexpected end of input");
        }

        char c = input.charAt(pos);

        if (c == '(') {
            return parseList();
        } else {
            return parseAtom();
        }
    }

    private Term parseList() {
        expect('(');
        skipWhitespace();

        List<Term> elements = new ArrayList<>();

        while (pos < input.length() && input.charAt(pos) != ')') {
            elements.add(parseTerm());
            skipWhitespace();
        }

        expect(')');
        return new Term.List(elements);
    }

    private Term parseAtom() {
        StringBuilder sb = new StringBuilder();

        while (pos < input.length() && !Character.isWhitespace(input.charAt(pos)) &&
                input.charAt(pos) != '(' && input.charAt(pos) != ')') {
            sb.append(input.charAt(pos));
            pos++;
        }

        if (sb.length() == 0) {
            throw new IllegalArgumentException("Expected atom at position " + pos);
        }

        return new Term.Atom(sb.toString());
    }

    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
    }

    private void expect(char expected) {
        if (pos >= input.length() || input.charAt(pos) != expected) {
            throw new IllegalArgumentException("Expected '" + expected + "' at position " + pos);
        }
        pos++;
    }
}