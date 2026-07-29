package oclminus.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Environment {

    private final Map<String, OclValue> values = new HashMap<>();

    public void define(String variableName, OclValue value) {
        Objects.requireNonNull(
                variableName,
                "Variablenname darf nicht null sein."
        );

        Objects.requireNonNull(
                value,
                "Variablenwert darf nicht null sein."
        );

        if (variableName.isBlank()) {
            throw new IllegalArgumentException(
                    "Variablenname darf nicht leer sein."
            );
        }

        values.put(variableName, value);
    }

    public OclValue lookup(String variableName) {
        Objects.requireNonNull(
                variableName,
                "Variablenname darf nicht null sein."
        );

        OclValue value = values.get(variableName);

        if (value == null) {
            throw new IllegalStateException(
                    "Unbekannte Variable: " + variableName
            );
        }

        return value;
    }
}
