package oclminus.runtime;

import oclminus.ast.BinaryExpression;
import oclminus.ast.BinaryOperator;
import oclminus.ast.BooleanLiteral;
import oclminus.ast.Expression;
import oclminus.ast.IntegerLiteral;
import oclminus.ast.VariableExpression;
import java.util.List;
import oclminus.ast.UnaryExpression;
import oclminus.ast.UnaryOperator;

public final class Interpreter {

    private final Environment environment;

    public Interpreter() {
        this(new Environment());
    }

    public Interpreter(Environment environment) {
        if (environment == null) {
            throw new IllegalArgumentException(
                    "Environment darf nicht null sein."
            );
        }

        this.environment = environment;
    }

    public OclValue evaluate(Expression expression) {
        if (expression == null) {
            throw new IllegalArgumentException(
                    "Expression darf nicht null sein."
            );
        }

        if (expression instanceof IntegerLiteral integerLiteral) {
            return new OclRelation(
                java.util.List.of(
                    new OclInteger(integerLiteral.value())
                )
            );
        }

        if (expression instanceof BooleanLiteral booleanLiteral) {
            return new OclRelation(
                java.util.List.of(
                    new OclBoolean(booleanLiteral.value())
                )
            );
        }

        if (expression instanceof VariableExpression variableExpression) {
            return environment.lookup(variableExpression.name());
        }

        if (expression instanceof UnaryExpression unaryExpression) {
            return evaluateUnaryExpression(unaryExpression);
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
            case EQUAL -> equal(leftValue, rightValue);
        };
    }

    private OclValue add(
        OclValue leftValue,
        OclValue rightValue
) {
    OclInteger leftInteger =
            requireSingleInteger(
                    leftValue,
                    BinaryOperator.PLUS
            );

    OclInteger rightInteger =
            requireSingleInteger(
                    rightValue,
                    BinaryOperator.PLUS
            );

    OclInteger result = new OclInteger(
            leftInteger.value()
                    + rightInteger.value()
    );

    return new OclRelation(
            List.of(result)
    );
}

    private OclValue multiply(
            OclValue leftValue,
            OclValue rightValue
    ) {
        OclInteger leftInteger =
                requireSingleInteger(
                        leftValue,
                        BinaryOperator.MULTIPLY
                );

        OclInteger rightInteger =
                requireSingleInteger(
                        rightValue,
                        BinaryOperator.MULTIPLY
                );

        OclInteger result = new OclInteger(
                leftInteger.value()
                        * rightInteger.value()
        );

        return new OclRelation(
                List.of(result)
        );
    }

    private OclValue equal(
            OclValue leftValue,
            OclValue rightValue
    ) {
        OclValue leftElement =
                requireSingleElement(
                        leftValue,
                        BinaryOperator.EQUAL
                );

        OclValue rightElement =
                requireSingleElement(
                        rightValue,
                        BinaryOperator.EQUAL
                );

        boolean result =
                leftElement.equals(rightElement);

        return new OclRelation(
                List.of(new OclBoolean(result))
        );
    }

    private OclValue evaluateUnaryExpression(
        UnaryExpression expression
    ) {
        OclValue operandValue = evaluate(expression.operand());

        return switch (expression.operator()) {
            case NOT -> not(operandValue);
            case NEGATE -> negate(operandValue);
        };
    }

    private OclValue negate(OclValue operandValue) {
        OclInteger integer =
                requireSingleInteger(
                        operandValue,
                        UnaryOperator.NEGATE
                );

        OclInteger result =
                new OclInteger(-integer.value());

        return new OclRelation(
                List.of(result)
        );
    }

    private OclValue not(OclValue operandValue) {
        OclBoolean booleanValue =
                requireSingleBoolean(
                        operandValue,
                        UnaryOperator.NOT
                );

        OclBoolean result =
                new OclBoolean(!booleanValue.value());

        return new OclRelation(
                List.of(result)
        );
    }

    private OclBoolean requireSingleBoolean(
        OclValue value,
        UnaryOperator operator
    ) {
        OclValue element =
                requireSingleElement(value, operator);

        if (!(element instanceof OclBoolean booleanValue)) {
            throw new IllegalStateException(
                    "Der Operator '"
                            + operator
                            + "' erwartet einen Boolean."
            );
        }

        return booleanValue;
    }

    private OclInteger requireSingleInteger(
        OclValue value,
        UnaryOperator operator
    ) {
        OclValue element =
                requireSingleElement(value, operator);

        if (!(element instanceof OclInteger integer)) {
            throw new IllegalStateException(
                    "Der Operator '"
                            + operator
                            + "' erwartet einen Integer."
            );
        }

        return integer;
    }

    private OclInteger requireSingleInteger(
        OclValue value,
        BinaryOperator operator
    ) {
        OclValue element =
                requireSingleElement(value, operator);

        if (!(element instanceof OclInteger integer)) {
            throw new IllegalStateException(
                    "Der Operator '"
                            + operator
                            + "' erwartet einen Integer."
            );
        }

        return integer;
    }

    private OclValue requireSingleElement(
        OclValue value,
        UnaryOperator operator
    ) {
        if (!(value instanceof OclRelation relation)) {
            throw new IllegalStateException(
                    "Der Operator '"
                            + operator
                            + "' erwartet eine Relation."
            );
        }

        if (relation.elements().size() != 1) {
            throw new IllegalStateException(
                    "Der Operator '"
                            + operator
                            + "' erwartet genau einen Wert."
            );
        }

        return relation.elements().get(0);
    }

    private OclValue requireSingleElement(
        OclValue value,
        BinaryOperator operator
    ) {
        if (!(value instanceof OclRelation relation)) {
            throw new IllegalStateException(
                    "Der Operator '"
                            + operator
                            + "' erwartet eine Relation."
            );
        }

        if (relation.elements().size() != 1) {
            throw new IllegalStateException(
                    "Der Operator '"
                            + operator
                            + "' erwartet genau einen Wert."
            );
        }

        return relation.elements().get(0);
    }
}