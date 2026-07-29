package oclminus.ast;

import java.util.Objects;

public record PropertyAccessExpression(
        Expression target,
        String propertyName
) implements Expression {

    public PropertyAccessExpression {
        Objects.requireNonNull(
                target,
                "Zielausdruck darf nicht null sein."
        );

        Objects.requireNonNull(
                propertyName,
                "Property-Name darf nicht null sein."
        );

        if (propertyName.isBlank()) {
            throw new IllegalArgumentException(
                    "Property-Name darf nicht leer sein."
            );
        }
    }
}
