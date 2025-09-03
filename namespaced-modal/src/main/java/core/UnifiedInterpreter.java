
package core;

import debug.Debugger;
import java.util.*;


public class UnifiedInterpreter {
    private final RuleSet ruleSet;
    private final Debugger debugger;
    private final Map<Term, Term> memoCache = new HashMap<>();
    private final Set<Term> activeEvaluations = new HashSet<>();

    public UnifiedInterpreter(RuleSet ruleSet, Debugger debugger) {
        this.ruleSet = ruleSet;
        this.debugger = debugger;
    }

    public Term evaluate(Term term) {
        memoCache.clear();
        activeEvaluations.clear();
        if (debugger.getMode() == Debugger.Mode.DEBUG) {
            System.out.println("DEBUG: Starting unified evaluation of: " + term);
        }
        Term result = evaluateRecursive(term);

        if (result instanceof Term.Cons cons) {
            var asList = cons.toList();
            if (asList.isPresent()) {
                return asList.get();
            }
        }

        return result;
    }



    private Term evaluateRecursive(Term term) {
        // Memoization Check
        if (memoCache.containsKey(term)) {
            return memoCache.get(term);
        }

        if (activeEvaluations.contains(term)) {
            return term;
        }

        activeEvaluations.add(term);
        try {
            Term result = evaluateCore(term);
            if (!result.equals(term)) {
                memoCache.put(term, result);
            }
            return result;
        } finally {
            activeEvaluations.remove(term);
        }
    }

    private Term evaluateCore(Term term) {
        if (term instanceof Term.Atom) {
            return term;
        }

        if (!(term instanceof Term.List list) || list.isEmpty()) {
            return term;
        }

        debugger.onStepStart(term);

        String funcSymbol = list.getFunctionSymbol();

        Term specialResult = handleSpecialOperators(list, funcSymbol);
        if (specialResult != null) {
            debugger.onStepEnd(specialResult);
            return specialResult;
        }

        Term ruleResult = tryApplyRulesWithPriority(list);
        if (!ruleResult.equals(list)) {
            Term finalResult = evaluateRecursive(ruleResult);
            debugger.onStepEnd(finalResult);
            return finalResult;
        }

        Term subResult = evaluateSubterms(list);
        debugger.onStepEnd(subResult);
        return subResult;
    }

    private Term handleSpecialOperators(Term.List list, String funcSymbol) {
        switch (funcSymbol) {
            case ":" -> {
                return evaluateArithmetic(list);
            }
            case "if" -> {
                return evaluateIf(list);
            }
            case "+", "-", "*", "/", "%", ">", "<", ">=", "<=", "=", "!=" -> {
                return evaluateInfixOperator(list);
            }
        }
        return null;
    }

    private Term evaluateIf(Term.List list) {
        if (list.elements().size() != 4) {
            throw new IllegalArgumentException("if requires (if condition then else)");
        }

        Term condition = evaluateRecursive(list.elements().get(1));

        if (condition instanceof Term.Atom atom && atom.isBoolean()) {
            if (atom.asBoolean()) {
                return evaluateRecursive(list.elements().get(2)); // then
            } else {
                return evaluateRecursive(list.elements().get(3)); // else
            }
        }

        throw new IllegalArgumentException("if condition must be boolean, got: " + condition);
    }

    private Term evaluateInfixOperator(Term.List list) {
        if (list.elements().size() != 3) {
            throw new IllegalArgumentException("Binary operator requires 2 arguments");
        }

        String op = list.getFunctionSymbol();
        Term arg1 = evaluateRecursive(list.elements().get(1));
        Term arg2 = evaluateRecursive(list.elements().get(2));

        Term colonExpr = new Term.List(
                Term.atom(":"),
                Term.atom(op),
                arg1,
                arg2
        );

        return evaluateArithmetic((Term.List) colonExpr);
    }

    private Term evaluateArithmetic(Term.List list) {
        if (list.elements().size() != 4) {
            throw new IllegalArgumentException("Arithmetic requires (: op arg1 arg2)");
        }

        Term operator = list.elements().get(1);
        Term arg1 = evaluateRecursive(list.elements().get(2));
        Term arg2 = evaluateRecursive(list.elements().get(3));

        if (!(operator instanceof Term.Atom opAtom)) {
            throw new IllegalArgumentException("Operator must be atom");
        }

        if (!(arg1 instanceof Term.Atom a1) || !(arg2 instanceof Term.Atom a2)) {
            throw new IllegalArgumentException("Args must be atoms: " + arg1 + ", " + arg2);
        }

        return evaluateBinaryOp(opAtom.value(), a1, a2);
    }

    private Term evaluateBinaryOp(String op, Term.Atom arg1, Term.Atom arg2) {
        switch (op) {
            case "=", "==" -> {
                return Term.bool(arg1.equals(arg2));
            }
            case "!=" -> {
                return Term.bool(!arg1.equals(arg2));
            }
        }

        if (!arg1.isNumber() || !arg2.isNumber()) {
            throw new IllegalArgumentException("Arithmetic args must be numbers: " + arg1 + ", " + arg2);
        }

        double val1 = arg1.asDouble();
        double val2 = arg2.asDouble();

        double result = switch (op) {
            case "+" -> val1 + val2;
            case "-" -> val1 - val2;
            case "*" -> val1 * val2;
            case "/" -> {
                if (val2 == 0.0) throw new ArithmeticException("Division by zero");
                yield val1 / val2;
            }
            case "%" -> {
                if (val2 == 0.0) throw new ArithmeticException("Division by zero");
                yield val1 % val2;
            }
            case ">" -> (val1 > val2) ? 1.0 : 0.0;
            case "<" -> (val1 < val2) ? 1.0 : 0.0;
            case ">=" -> (val1 >= val2) ? 1.0 : 0.0;
            case "<=" -> (val1 <= val2) ? 1.0 : 0.0;
            default -> throw new IllegalArgumentException("Unknown operator: " + op);
        };

        if (op.equals(">") || op.equals("<") || op.equals(">=") || op.equals("<=")) {
            return Term.bool(result != 0.0);
        }

        return Term.number(result);
    }

    private Term tryApplyRulesWithPriority(Term.List term) {
        String funcSymbol = term.getFunctionSymbol();
        var rules = ruleSet.getRulesForFunction(funcSymbol);

        for (Rule rule : rules) {
            if (isBaseCase(rule)) {
                Optional<Map<String, Term>> match = RuleMatcher.match(rule.pattern(), term);
                if (match.isPresent()) {
                    Term result = RuleMatcher.substitute(rule.replacement(), match.get());
                    debugger.onRuleApplied(rule, term, result);
                    return result;
                }
            }
        }

        for (Rule rule : rules) {
            if (!isBaseCase(rule)) {
                Optional<Map<String, Term>> match = RuleMatcher.match(rule.pattern(), term);
                if (match.isPresent()) {
                    Term result = RuleMatcher.substitute(rule.replacement(), match.get());
                    debugger.onRuleApplied(rule, term, result);
                    return result;
                }
            }
        }

        return term;
    }

    private boolean isBaseCase(Rule rule) {
        String ruleId = rule.namespace() + "." + rule.name(); // ← KORRIGIERT: name() statt symbol()
        if (ruleId.contains("base") || ruleId.contains("empty") || ruleId.contains("zero")) {
            return true;
        }

        String pattern = rule.pattern().toString();
        if (pattern.matches(".*\\b[0-9]+\\b.*") || pattern.contains("()")) {
            return true;
        }

        String replacement = rule.replacement().toString();
        if (rule.pattern() instanceof Term.List patternList && !patternList.isEmpty()) {
            String funcName = patternList.getFunctionSymbol();
            return !replacement.contains(funcName);
        }

        return false;
    }



    private boolean isRecursiveFunction(String funcName) {
        return "fib".equals(funcName) || "fact".equals(funcName) ||
                "length".equals(funcName) || "reverse".equals(funcName) ||
                "reverse-helper".equals(funcName) ||
                "append".equals(funcName);
    }


    private Term evaluateSubterms(Term.List list) {
        List<Term> newElements = new ArrayList<>();
        boolean changed = false;

        for (Term element : list.elements()) {
            Term evaluatedElement = evaluateRecursive(element);
            newElements.add(evaluatedElement);
            if (!evaluatedElement.equals(element)) {
                changed = true;
            }
        }

        return changed ? new Term.List(newElements) : list;
    }
}