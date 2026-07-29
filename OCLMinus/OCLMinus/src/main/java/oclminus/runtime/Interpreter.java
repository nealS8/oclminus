package oclminus.runtime;

import oclminus.ast.BinaryExpression;
import oclminus.ast.BinaryOperator;
import oclminus.ast.BooleanLiteral;
import oclminus.ast.Expression;
import oclminus.ast.IntegerLiteral;

public final class Interpreter {

    public OclValue evaluate(Expression expression) {
        if (expression == null) {
            throw new IllegalArgumentException(
                    "Expression darf nicht null sein."
            );
        }

        if (expression instanceof IntegerLiteral integerLiteral) {
            return new OclInteger(integerLiteral.value());
        }

        if (expression instanceof BooleanLiteral booleanLiteral) {
            return new OclBoolean(booleanLiteral.value());
        }

        if (expression instanceof BinaryExpression binaryExpression) {
            return evaluateBinaryExpression(binaryExpression);
        }

        throw new IllegalStateException(
                "Unbekannter Ausdruck: "
                        + expression.getClass().getSimpleName()
        );
    }

    private OclValue evaluateBinaryExpression(
            BinaryExpression expression
    ) {
        OclValue leftValue = evaluate(expression.left());
        OclValue rightValue = evaluate(expression.right());

        return switch (expression.operator()) {
            case PLUS -> add(leftValue, rightValue);
            case MULTIPLY -> multiply(leftValue, rightValue);
        };
    }

    private OclValue add(
            OclValue leftValue,
            OclValue rightValue
    ) {
        if (leftValue instanceof OclInteger leftInteger
                && rightValue instanceof OclInteger rightInteger) {

            return new OclInteger(
                    leftInteger.value()
                            + rightInteger.value()
            );
        }

        throw new IllegalStateException(
                "Der Operator '+' erwartet zwei Ganzzahlen."
        );
    }

    private OclValue multiply(
            OclValue leftValue,
            OclValue rightValue
    ) {
        if (leftValue instanceof OclInteger leftInteger
                && rightValue instanceof OclInteger rightInteger) {

            return new OclInteger(
                    leftInteger.value()
                            * rightInteger.value()
            );
        }

        throw new IllegalStateException(
                "Der Operator '*' erwartet zwei Ganzzahlen."
        );
    }
}