package oclminus.ast;

import java.util.Objects;

public record VariableExpression(String name)
        implements Expression {

    public VariableExpression {
        Objects.requireNonNull(name, "name darf nicht null sein");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Der Variablenname darf nicht leer sein."
            );
        }
    }
}