package io;

import core.Rule;
import core.RuleSet;
import core.Term;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleParser {
    private static final Pattern RULE_HEADER_PATTERN = Pattern.compile("^<([^.]+)\\.([^>]+)>\\s+(.*)$");

    public static RuleSet loadFromResource(String resourcePath) {
        try (InputStream is = RuleParser.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }

            return loadFromStream(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load rules from resource: " + resourcePath, e);
        }
    }

    private static RuleSet loadFromStream(InputStream is) throws IOException {
        RuleSet ruleSet = new RuleSet();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                try {
                    Rule rule = parseRule(line);
                    ruleSet.addRule(rule);
                } catch (Exception e) {
                    throw new RuntimeException("Error parsing rule at line " + lineNumber + ": " + line, e);
                }
            }
        }

        return ruleSet;
    }

    private static Rule parseRule(String line) {
        Matcher matcher = RULE_HEADER_PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid rule syntax: " + line);
        }

        String namespace = matcher.group(1);
        String name = matcher.group(2);
        String rest = matcher.group(3);

        String[] parts = parsePatternAndReplacement(rest);
        String patternStr = parts[0];
        String replacementStr = parts[1];

        Term pattern = TermParser.parse(patternStr);
        Term replacement = TermParser.parse(replacementStr);

        return new Rule(namespace, name, pattern, replacement);
    }

    private static String[] parsePatternAndReplacement(String rest) {
        rest = rest.trim();

        if (rest.startsWith("(")) {
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
                throw new IllegalArgumentException("Unbalanced parentheses in pattern");
            }

            String pattern = rest.substring(0, i + 1);
            String replacement = rest.substring(i + 1).trim();

            return new String[]{pattern, replacement};
        } else {
            // Pattern is a single atom - find the first space
            int spaceIndex = rest.indexOf(' ');
            if (spaceIndex == -1) {
                throw new IllegalArgumentException("Missing replacement in rule");
            }

            String pattern = rest.substring(0, spaceIndex);
            String replacement = rest.substring(spaceIndex + 1).trim();

            return new String[]{pattern, replacement};
        }
    }
}