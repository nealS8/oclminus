package oclminus.ast;

import java.util.Objects;

public record LiftExpression(
        Expression operand
) implements Expression {

    public LiftExpression {
        Objects.requireNonNull(
                operand,
                "Operand darf nicht null sein."
        );
    }
}
