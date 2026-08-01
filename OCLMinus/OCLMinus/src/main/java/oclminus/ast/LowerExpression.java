package oclminus.ast;

import java.util.Objects;

public record LowerExpression(
        Expression operand
) implements Expression {

    public LowerExpression {
        Objects.requireNonNull(
                operand,
                "Operand darf nicht null sein."
        );
    }
}
