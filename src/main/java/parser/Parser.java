package parser;

import core.Term;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private String input;
    private int pos;

    public Parser(String input) {
        this.input = input.trim();
        this.pos = 0;
    }

    public Term parse() {
        skipWhitespace();
        if (pos >= input.length()) {
            return null;
        }

        if (current() == '(') {
            return parseList();
        } else {
            return parseAtom();
        }
    }

    private Term parseList() {
        expect('(');
        List<Term> children = new ArrayList<>();

        skipWhitespace();
        while (pos < input.length() && current() != ')') {
            Term child = parse();
            if (child != null) {
                children.add(child);
            }
            skipWhitespace();
        }

        expect(')');
        return new Term(children);
    }

    private Term parseAtom() {
        StringBuilder sb = new StringBuilder();

        while (pos < input.length() && !isDelimiter(current())) {
            sb.append(current());
            pos++;
        }

        return new Term(sb.toString());
    }

    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(current())) {
            pos++;
        }
    }

    private char current() {
        return input.charAt(pos);
    }

    private void expect(char expected) {
        if (pos >= input.length() || current() != expected) {
            throw new RuntimeException("Expected '" + expected + "' at position " + pos);
        }
        pos++;
    }

    private boolean isDelimiter(char c) {
        return Character.isWhitespace(c) || c == '(' || c == ')';
    }

    public static Term parseString(String s) {
        return new Parser(s).parse();
    }
}