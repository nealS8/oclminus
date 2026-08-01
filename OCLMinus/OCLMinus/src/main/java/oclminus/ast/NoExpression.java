package oclminus.ast;

import java.util.Objects;

public record NoExpression(
        String typeName
) implements Expression {

    public NoExpression {
        Objects.requireNonNull(
                typeName,
                "Typname darf nicht null sein."
        );

        if (typeName.isBlank()) {
            throw new IllegalArgumentException(
                    "Typname darf nicht leer sein."
            );
        }
    }
}