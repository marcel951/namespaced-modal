package io;

import core.Rule;
import core.Term;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamicRuleParser {
    private static final Pattern ADD_RULE_PATTERN = Pattern.compile("^<([^.]+)\\.([^>]+)>\\s+(.+)$");
    private static final Pattern REMOVE_RULE_PATTERN = Pattern.compile("^>([^.]+)\\.([^<]+)<\\s*$");

    public static class ParseResult {
        public enum Type { ADD, REMOVE }

        private final Type type;
        private final Rule rule;
        private final String fullName;

        private ParseResult(Type type, Rule rule, String fullName) {
            this.type = type;
            this.rule = rule;
            this.fullName = fullName;
        }

        public static ParseResult addRule(Rule rule) {
            return new ParseResult(Type.ADD, rule, null);
        }

        public static ParseResult removeRule(String fullName) {
            return new ParseResult(Type.REMOVE, null, fullName);
        }

        public Type getType() { return type; }
        public Rule getRule() { return rule; }
        public String getFullName() { return fullName; }
    }

    public static ParseResult parse(String input) {
        input = input.trim();

        // Try parsing add rule syntax
        Matcher addMatcher = ADD_RULE_PATTERN.matcher(input);
        if (addMatcher.matches()) {
            return parseAddRule(addMatcher);
        }

        // Try parsing remove rule syntax
        Matcher removeMatcher = REMOVE_RULE_PATTERN.matcher(input);
        if (removeMatcher.matches()) {
            return parseRemoveRule(removeMatcher);
        }

        throw new IllegalArgumentException("Invalid rule syntax. Use <namespace.name>pattern replacement to add or >namespace.name< to remove.");
    }

    private static ParseResult parseAddRule(Matcher matcher) {
        String namespace = matcher.group(1);
        String name = matcher.group(2);
        String rest = matcher.group(3).trim();

        // Validate namespace and name
        if (namespace.isEmpty() || name.isEmpty()) {
            throw new IllegalArgumentException("Namespace und Name dürfen nicht leer sein.");
        }

        if (namespace.contains(" ") || name.contains(" ")) {
            throw new IllegalArgumentException("Namespace and name must not be empty.");
        }

        if (namespace.contains(" ") || name.contains(" ")) {
            throw new IllegalArgumentException("Namespace and name must not contain spaces.");
        }

        // Parse pattern and replacement
        String[] parts = parsePatternAndReplacement(rest);
        String patternStr = parts[0];
        String replacementStr = parts[1];

        if (patternStr.isEmpty() || replacementStr.isEmpty()) {
            throw new IllegalArgumentException("Pattern und Replacement dürfen nicht leer sein.");
        }

        try {
            Term pattern = TermParser.parse(patternStr);
            Term replacement = TermParser.parse(replacementStr);

            Rule rule = new Rule(namespace, name, pattern, replacement);
            return ParseResult.addRule(rule);
        } catch (Exception e) {
            throw new IllegalArgumentException("Syntaxfehler beim Parsen: " + e.getMessage(), e);
        }
    }

    private static ParseResult parseRemoveRule(Matcher matcher) {
        String namespace = matcher.group(1);
        String name = matcher.group(2);

        // Validate namespace and name
        if (namespace.isEmpty() || name.isEmpty()) {
            throw new IllegalArgumentException("Namespace und Name dürfen nicht leer sein.");
        }

        String fullName = namespace + "." + name;
        return ParseResult.removeRule(fullName);
    }

    private static String[] parsePatternAndReplacement(String rest) {
        if (rest.startsWith("(")) {
            // Pattern starts with '(' - find the matching closing parenthesis
            int depth = 0;
            int i = 0;

            while (i < rest.length()) {
                char c = rest.charAt(i);
                if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    depth--;
                    if (depth == 0) {
                        break;
                    }
                }
                i++;
            }

            if (depth != 0) {
                throw new IllegalArgumentException("Unausgeglichene Klammern im Pattern");
            }

            String pattern = rest.substring(0, i + 1);
            String replacement = rest.substring(i + 1).trim();

            if (replacement.isEmpty()) {
                throw new IllegalArgumentException("Replacement fehlt");
            }

            return new String[]{pattern, replacement};
        } else {
            // Pattern is a single atom - find the first space
            int spaceIndex = rest.indexOf(' ');
            if (spaceIndex == -1) {
                throw new IllegalArgumentException("Replacement fehlt");
            }

            String pattern = rest.substring(0, spaceIndex);
            String replacement = rest.substring(spaceIndex + 1).trim();

            return new String[]{pattern, replacement};
        }
    }

    public static boolean isDynamicRuleCommand(String input) {
        input = input.trim();
        return input.startsWith("<") || input.startsWith(">");
    }
}