package oclminus.type;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class TypeEnvironment {

    private final Map<String, CType> types =
            new HashMap<>();

    public void define(
            String name,
            CType type
    ) {
        Objects.requireNonNull(
                name,
                "Variablenname darf nicht null sein."
        );

        Objects.requireNonNull(
                type,
                "CType darf nicht null sein."
        );

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Variablenname darf nicht leer sein."
            );
        }

        types.put(name, type);
    }

    public CType lookup(String name) {
        Objects.requireNonNull(
                name,
                "Variablenname darf nicht null sein."
        );

        CType type = types.get(name);

        if (type == null) {
            throw new IllegalStateException(
                    "Für die Variable '"
                            + name
                            + "' ist kein CType definiert."
            );
        }

        return type;
    }

    public boolean contains(String name) {
        Objects.requireNonNull(
                name,
                "Variablenname darf nicht null sein."
        );

        return types.containsKey(name);
    }
}