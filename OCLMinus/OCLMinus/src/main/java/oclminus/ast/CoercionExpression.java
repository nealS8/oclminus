package oclminus.ast;

import java.util.Objects;

import oclminus.type.CollectionKind;

public record CoercionExpression(
        Expression operand,
        CollectionKind collectionKind
) implements Expression {

    public CoercionExpression {
        Objects.requireNonNull(
                operand,
                "Operand darf nicht null sein."
        );

        Objects.requireNonNull(
                collectionKind,
                "CollectionKind darf nicht null sein."
        );
    }
}