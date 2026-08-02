package oclminus.ast;

import java.util.Objects;

public record IterationExpression(
        Expression source,
        String iteratorVariable,
        String accumulatorVariable,
        Expression initialValue,
        Expression body
) implements Expression {

    public IterationExpression {
        Objects.requireNonNull(
                source,
                "Source darf nicht null sein."
        );

        Objects.requireNonNull(
                iteratorVariable,
                "Iteratorvariable darf nicht null sein."
        );

        Objects.requireNonNull(
                accumulatorVariable,
                "Akkumulatorvariable darf nicht null sein."
        );

        Objects.requireNonNull(
                initialValue,
                "Initialwert darf nicht null sein."
        );

        Objects.requireNonNull(
                body,
                "Body darf nicht null sein."
        );

        if (iteratorVariable.isBlank()) {
            throw new IllegalArgumentException(
                    "Iteratorvariable darf nicht leer sein."
            );
        }

        if (accumulatorVariable.isBlank()) {
            throw new IllegalArgumentException(
                    "Akkumulatorvariable darf nicht leer sein."
            );
        }
    }
}