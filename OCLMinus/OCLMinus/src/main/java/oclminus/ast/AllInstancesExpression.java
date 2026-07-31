package oclminus.ast;

import java.util.Objects;

public record AllInstancesExpression(
        String className
) implements Expression {

    public AllInstancesExpression {
        Objects.requireNonNull(
                className,
                "Klassenname darf nicht null sein."
        );

        if (className.isBlank()) {
            throw new IllegalArgumentException(
                    "Klassenname darf nicht leer sein."
            );
        }
    }
}
