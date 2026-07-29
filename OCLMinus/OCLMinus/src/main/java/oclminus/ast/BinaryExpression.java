package oclminus.ast;

import java.util.Objects;

public record BinaryExpression(
        Expression left,
        BinaryOperator operator,
        Expression right
) implements Expression {

    public BinaryExpression {
        Objects.requireNonNull(left, "Linker Ausdruck darf nicht null sein.");
        Objects.requireNonNull(operator, "Operator darf nicht null sein.");
        Objects.requireNonNull(right, "Rechter Ausdruck darf nicht null sein.");
    }
}
