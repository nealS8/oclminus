package oclminus.type;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class TypeEnvironment {

    private final TypeEnvironment parent;
    private final Map<String, CType> types =
            new HashMap<>();

    public TypeEnvironment() {
        this(null);
    }

    private TypeEnvironment(
            TypeEnvironment parent
    ) {
        this.parent = parent;
    }

    public TypeEnvironment createChild() {
        return new TypeEnvironment(this);
    }

    public void define(
            String name,
            CType type
    ) {
        validateName(name);

        Objects.requireNonNull(
                type,
                "CType darf nicht null sein."
        );

        types.put(name, type);
    }

    public CType lookup(String name) {
        validateName(name);

        CType type = types.get(name);

        if (type != null) {
            return type;
        }

        if (parent != null) {
            return parent.lookup(name);
        }

        throw new IllegalStateException(
                "Für die Variable '"
                        + name
                        + "' ist kein CType definiert."
        );
    }

    public boolean containsLocal(String name) {
        validateName(name);

        return types.containsKey(name);
    }

    public boolean contains(String name) {
        validateName(name);

        if (types.containsKey(name)) {
            return true;
        }

        return parent != null
                && parent.contains(name);
    }

    private void validateName(String name) {
        Objects.requireNonNull(
                name,
                "Variablenname darf nicht null sein."
        );

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Variablenname darf nicht leer sein."
            );
        }
    }
}