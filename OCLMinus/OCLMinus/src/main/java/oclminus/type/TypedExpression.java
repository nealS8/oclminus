package oclminus.type;

import java.util.Objects;

import oclminus.ast.Expression;

public record TypedExpression(
        Expression expression,
        CType type
) {

    public TypedExpression {
        Objects.requireNonNull(
                expression,
                "Expression darf nicht null sein."
        );

        Objects.requireNonNull(
                type,
                "CType darf nicht null sein."
        );
    }
}