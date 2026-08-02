package oclminus.ast;

import java.util.Objects;

public record ConditionalExpression(
        Expression condition,
        Expression thenBranch,
        Expression elseBranch
) implements Expression {

    public ConditionalExpression {
        Objects.requireNonNull(
                condition,
                "Bedingung darf nicht null sein."
        );

        Objects.requireNonNull(
                thenBranch,
                "Then-Branch darf nicht null sein."
        );

        Objects.requireNonNull(
                elseBranch,
                "Else-Branch darf nicht null sein."
        );
    }
}