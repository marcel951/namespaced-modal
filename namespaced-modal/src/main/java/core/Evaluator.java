package core;

public class Evaluator {

    public static Term evaluate(Term term) {
        if (term instanceof Term.Cons cons) {
            var asList = cons.toList();
            if (asList.isPresent()) {
                return evaluate(asList.get());
            }
            return Term.cons(evaluate(cons.car()), evaluate(cons.cdr()));
        }

        if (term instanceof Term.List list && !list.isEmpty()) {
            Term head = list.head();

            if (head instanceof Term.Atom atom && ":".equals(atom.value())) {
                return evaluateArithmetic(list);
            }

            java.util.List<Term> evaluatedElements = new java.util.ArrayList<>();
            boolean changed = false;

            for (Term element : list.elements()) {
                Term evaluatedElement = evaluate(element);
                evaluatedElements.add(evaluatedElement);
                if (!evaluatedElement.equals(element)) {
                    changed = true;
                }
            }

            if (changed) {
                return new Term.List(evaluatedElements);
            }
        }
        return term;
    }

    private static Term evaluateArithmetic(Term.List list) {
        if (list.elements().size() != 4) {
            throw new IllegalArgumentException("Arithmetic evaluation requires exactly 4 elements (: op arg1 arg2)");
        }
        Term operator = list.elements().get(1);
        Term arg1 = list.elements().get(2);
        Term arg2 = list.elements().get(3);
        if (!(operator instanceof Term.Atom opAtom)) {
            throw new IllegalArgumentException("Operator must be an atom");
        }

        Term evaluatedArg1 = evaluate(arg1);
        Term evaluatedArg2 = evaluate(arg2);

        return evaluateBinaryOp(opAtom.value(), evaluatedArg1, evaluatedArg2);
    }

    private static Term evaluateBinaryOp(String op, Term arg1, Term arg2) {
        if (!(arg1 instanceof Term.Atom a1) || !(arg2 instanceof Term.Atom a2)) {
            throw new IllegalArgumentException("Arithmetic arguments must be atoms: " + arg1 + ", " + arg2);
        }

        switch (op) {
            case "=" -> {
                return Term.bool(arg1.equals(arg2));
            }
            case "==" -> {
                return Term.bool(arg1.equals(arg2));
            }
            case "!=" -> {
                return Term.bool(!arg1.equals(arg2));
            }
        }

        if (!a1.isNumber() || !a2.isNumber()) {
            throw new IllegalArgumentException("Arithmetic arguments must be numbers: " + arg1 + ", " + arg2);
        }

        double val1 = a1.asDouble();
        double val2 = a2.asDouble();

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
            case "mod" -> {
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