package core;

public class SpecialOperatorEvaluator {

    public boolean isSpecialOperator(String operator) {
        return switch (operator) {
            case ":", "if", "+", "-", "*", "/", "%",
                 ">", "<", ">=", "<=", "=", "!=" -> true;
            default -> false;
        };
    }

    public Term evaluate(Term.List list, TermEvaluator evaluator) {
        String funcSymbol = list.getFunctionSymbol();

        return switch (funcSymbol) {
            case ":" -> evaluateArithmetic(list, evaluator);
            case "if" -> evaluateIf(list, evaluator);
            case "+", "-", "*", "/", "%", ">", "<", ">=", "<=", "=", "!=" ->
                    evaluateInfixOperator(list, evaluator);
            default -> throw new IllegalArgumentException("Not a special operator: " + funcSymbol);
        };
    }

    private Term evaluateIf(Term.List list, TermEvaluator evaluator) {
        if (list.elements().size() != 4) {
            throw new IllegalArgumentException("if requires (if condition then else)");
        }

        Term condition = evaluator.evaluate(list.elements().get(1));

        if (condition instanceof Term.Atom atom && atom.isBoolean()) {
            if (atom.asBoolean()) {
                return evaluator.evaluate(list.elements().get(2)); // then
            } else {
                return evaluator.evaluate(list.elements().get(3)); // else
            }
        }

        throw new IllegalArgumentException("if condition must be boolean, got: " + condition);
    }

    private Term evaluateInfixOperator(Term.List list, TermEvaluator evaluator) {
        if (list.elements().size() != 3) {
            throw new IllegalArgumentException("Binary operator requires 2 arguments");
        }

        String op = list.getFunctionSymbol();
        Term arg1 = evaluator.evaluate(list.elements().get(1));
        Term arg2 = evaluator.evaluate(list.elements().get(2));

        Term colonExpr = new Term.List(
                Term.atom(":"),
                Term.atom(op),
                arg1,
                arg2
        );

        return evaluateArithmetic((Term.List) colonExpr, evaluator);
    }

    private Term evaluateArithmetic(Term.List list, TermEvaluator evaluator) {
        if (list.elements().size() != 4) {
            throw new IllegalArgumentException("Arithmetic requires (: op arg1 arg2)");
        }

        Term operator = list.elements().get(1);
        Term arg1 = evaluator.evaluate(list.elements().get(2));
        Term arg2 = evaluator.evaluate(list.elements().get(3));

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
}