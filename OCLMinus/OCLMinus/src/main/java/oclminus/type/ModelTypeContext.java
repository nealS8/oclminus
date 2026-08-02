package oclminus.type;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ModelTypeContext {

    private final Map<String, CType> properties =
            new HashMap<>();

    public void defineProperty(
            String className,
            String propertyName,
            CType type
    ) {
        validateName(
                className,
                "Klassenname"
        );

        validateName(
                propertyName,
                "Property-Name"
        );

        Objects.requireNonNull(
                type,
                "CType darf nicht null sein."
        );

        properties.put(
                qualifiedPropertyName(
                        className,
                        propertyName
                ),
                type
        );
    }

    public CType lookupProperty(
            String className,
            String propertyName
    ) {
        validateName(
                className,
                "Klassenname"
        );

        validateName(
                propertyName,
                "Property-Name"
        );

        CType type = properties.get(
                qualifiedPropertyName(
                        className,
                        propertyName
                )
        );

        if (type == null) {
            throw new TypeCheckException(
                    "Für die Property '"
                            + className
                            + "."
                            + propertyName
                            + "' ist kein CType definiert."
            );
        }

        return type;
    }

    public boolean containsProperty(
            String className,
            String propertyName
    ) {
        validateName(
                className,
                "Klassenname"
        );

        validateName(
                propertyName,
                "Property-Name"
        );

        return properties.containsKey(
                qualifiedPropertyName(
                        className,
                        propertyName
                )
        );
    }

    private String qualifiedPropertyName(
            String className,
            String propertyName
    ) {
        return className + "." + propertyName;
    }

    private void validateName(
            String name,
            String description
    ) {
        Objects.requireNonNull(
                name,
                description + " darf nicht null sein."
        );

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    description + " darf nicht leer sein."
            );
        }
    }
}