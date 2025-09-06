package core;

import java.util.HashMap;
import java.util.Map;

public class Evaluator {

    private final Map<Term, Term> evaluationCache = new HashMap<>();

    public static class Thunk {
        private final Term expression;
        private final TermEvaluator evaluator;
        private Term cachedResult = null;
        private boolean evaluated = false;

        public Thunk(Term expression, TermEvaluator evaluator) {
            this.expression = expression;
            this.evaluator = evaluator;
        }

        public Term force() {
            if (!evaluated) {
                cachedResult = evaluator.evaluate(expression);
                evaluated = true;
            }
            return cachedResult;
        }
    }

    public boolean isSpecialOperator(String operator) {
        return switch (operator) {
            case ":", "if", "+", "-", "*", "/", "%",
                 ">", "<", ">=", "<=", "=", "!=" -> true;
            default -> false;
        };
    }

    public Term evaluate(Term.List list, TermEvaluator evaluator) {
        if (evaluationCache.containsKey(list)) {
            return evaluationCache.get(list);
        }

        String funcSymbol = list.getFunctionSymbol();

        Term result = switch (funcSymbol) {
            case ":" -> evaluateArithmeticLazy(list, evaluator);
            case "if" -> evaluateIfLazy(list, evaluator);
            case "+", "-", "*", "/", "%", ">", "<", ">=", "<=", "=", "!=" ->
                    evaluateInfixOperatorLazy(list, evaluator);
            default -> throw new IllegalArgumentException("Not a special operator: " + funcSymbol);
        };

        evaluationCache.put(list, result);
        return result;
    }

    private Term evaluateIfLazy(Term.List list, TermEvaluator evaluator) {
        if (list.elements().size() != 4) {
            throw new IllegalArgumentException("if requires (if condition then else)");
        }

        Term condition = evaluator.evaluate(list.elements().get(1));

        if (condition instanceof Term.Atom atom && atom.isBoolean()) {
            if (atom.asBoolean()) {
                // Lazy: Nur den then-Zweig auswerten
                return evaluator.evaluate(list.elements().get(2));
            } else {
                // Lazy: Nur den else-Zweig auswerten
                return evaluator.evaluate(list.elements().get(3));
            }
        }

        throw new IllegalArgumentException("if condition must be boolean, got: " + condition);
    }

    private Term evaluateInfixOperatorLazy(Term.List list, TermEvaluator evaluator) {
        if (list.elements().size() != 3) {
            throw new IllegalArgumentException("Binary operator requires 2 arguments");
        }

        String op = list.getFunctionSymbol();

        Thunk arg1Thunk = new Thunk(list.elements().get(1), evaluator);
        Thunk arg2Thunk = new Thunk(list.elements().get(2), evaluator);

        Term colonExpr = new Term.List(
                Term.atom(":"),
                Term.atom(op),
                Term.atom("thunk1"),
                Term.atom("thunk2")
        );

        return evaluateArithmeticWithThunks((Term.List) colonExpr, arg1Thunk, arg2Thunk);
    }

    private Term evaluateArithmeticLazy(Term.List list, TermEvaluator evaluator) {
        if (list.elements().size() != 4) {
            throw new IllegalArgumentException("Arithmetic requires (: op arg1 arg2)");
        }

        Term operator = list.elements().get(1);

        // Erstelle Thunks für die Argumente
        Thunk arg1Thunk = new Thunk(list.elements().get(2), evaluator);
        Thunk arg2Thunk = new Thunk(list.elements().get(3), evaluator);

        if (!(operator instanceof Term.Atom opAtom)) {
            throw new IllegalArgumentException("Operator must be atom");
        }

        return evaluateArithmeticWithThunks(list, arg1Thunk, arg2Thunk);
    }

    private Term evaluateArithmeticWithThunks(Term.List list, Thunk arg1Thunk, Thunk arg2Thunk) {
        Term operator = list.elements().get(1);

        if (!(operator instanceof Term.Atom opAtom)) {
            throw new IllegalArgumentException("Operator must be atom");
        }

        Term arg1 = arg1Thunk.force();
        Term arg2 = arg2Thunk.force();

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
}