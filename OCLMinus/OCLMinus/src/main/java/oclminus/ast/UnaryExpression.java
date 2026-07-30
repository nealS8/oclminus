package oclminus.ast;

public record UnaryExpression(
        UnaryOperator operator,
        Expression operand
) implements Expression {

    public UnaryExpression {
        if (operator == null) {
            throw new IllegalArgumentException(
                    "Operator darf nicht null sein."
            );
        }

        if (operand == null) {
            throw new IllegalArgumentException(
                    "Operand darf nicht null sein."
            );
        }
    }
}