package oclminus.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class Environment {

    public Set<String> variableNames() {return Set.copyOf(values.keySet());}

    private final Environment parent;
    private final Map<String, OclValue> values =
            new HashMap<>();

    public Environment() {
        this(null);
    }

    private Environment(Environment parent) {
        this.parent = parent;
    }

    public Environment createChild() {
        return new Environment(this);
    }

    // OclValue ist erlaubt, obwohl bei späteren Interpreterpfaden OclRelation vorausgesetzt wird, führt zu Fehlern
    public void define(String name, OclValue value) { 
        
        Objects.requireNonNull(name, "Variablenname darf nicht null sein.");

        Objects.requireNonNull(value, "OclValue darf nicht null sein.");

        if (name.isBlank()) {
            throw new IllegalArgumentException("Variablenname darf nicht leer sein.");
        }

        values.put(name, value);
    }

    public OclValue lookup(String name) {
        Objects.requireNonNull(
                name,
                "Variablenname darf nicht null sein."
        );

        OclValue value = values.get(name);

        if (value != null) {
            return value;
        }

        if (parent != null) {
            return parent.lookup(name);
        }

        throw new IllegalStateException(
                "Variable '"
                        + name
                        + "' ist nicht definiert."
        );
    }

    public boolean containsLocal(String name) {
        Objects.requireNonNull(
                name,
                "Variablenname darf nicht null sein."
        );

        return values.containsKey(name);
    }

    public boolean contains(String name) {
        Objects.requireNonNull(
                name,
                "Variablenname darf nicht null sein."
        );

        if (values.containsKey(name)) {
            return true;
        }

        return parent != null
                && parent.contains(name);
    }
}
