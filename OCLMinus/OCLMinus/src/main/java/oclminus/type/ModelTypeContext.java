package oclminus.type;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ModelTypeContext {
    
    // Wichtig fuer Vererbung
    private final Map<String, String> superClasses = new HashMap<>();

    public void defineSuperClass(String className, String superClassName) {
        
        validateName(className, "Klassenname");

        validateName(superClassName, "Name der Oberklasse");

        superClasses.put(className, superClassName);
    }

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

    public CType lookupProperty(String className, String propertyName) {
        
        validateName(className, "Klassenname");

        validateName(propertyName, "Property-Name");

        String currentClass = className;

        while (currentClass != null) {

            CType type = properties.get(qualifiedPropertyName(currentClass, propertyName));

            if (type != null) {
                return type;
            }

            currentClass = superClasses.get(currentClass);
        }

        throw new TypeCheckException(
            "Für die Property '"
                    + className
                    + "."
                    + propertyName
                    + "' ist kein CType definiert."
        );
    }

    public boolean containsProperty(String className, String propertyName) {
        
        validateName(className, "Klassenname");

        validateName(propertyName, "Property-Name");

        String currentClass = className;

        while (currentClass != null) {

            if (properties.containsKey(qualifiedPropertyName(currentClass, propertyName))) {
                return true;
            }

            currentClass = superClasses.get(currentClass);
        }

        return false;
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

    public boolean isSameOrSubclass(String className, String expectedClassName) {
        
        validateName(className, "Klassenname");

        validateName(expectedClassName, "Erwarteter Klassenname");

        String currentClass = className;

        while (currentClass != null) {

            if (currentClass.equals(expectedClassName)) {
                return true;
            }

            currentClass = superClasses.get(currentClass);
        }

        return false;
    }
}