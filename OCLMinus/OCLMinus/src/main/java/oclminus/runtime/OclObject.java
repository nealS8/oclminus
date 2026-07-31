package oclminus.runtime;

import java.util.Map;
import java.util.Objects;

public final class OclObject implements OclValue {

    private final String id;
    private final String className;
    private final Map<String, OclRelation> properties;

    public OclObject(
            String id,
            String className,
            Map<String, OclRelation> properties
    ) {
        this.id = Objects.requireNonNull(
                id,
                "Objekt-ID darf nicht null sein."
        );

        this.className = Objects.requireNonNull(
                className,
                "Klassenname darf nicht null sein."
        );

        Objects.requireNonNull(
                properties,
                "Properties dürfen nicht null sein."
        );

        if (id.isBlank()) {
            throw new IllegalArgumentException(
                    "Objekt-ID darf nicht leer sein."
            );
        }

        if (className.isBlank()) {
            throw new IllegalArgumentException(
                    "Klassenname darf nicht leer sein."
            );
        }

        this.properties = Map.copyOf(properties);
    }

    public String id() {
        return id;
    }

    public String className() {
        return className;
    }

    public OclRelation property(String propertyName) {
        Objects.requireNonNull(
                propertyName,
                "Property-Name darf nicht null sein."
        );

        OclRelation value = properties.get(propertyName);

        if (value == null) {
            throw new IllegalStateException(
                    "Objekt '"
                            + id
                            + "' der Klasse '"
                            + className
                            + "' besitzt keine Property '"
                            + propertyName
                            + "'."
            );
        }

        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof OclObject object)) {
            return false;
        }

        return id.equals(object.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return className + "(" + id + ")";
    }
}