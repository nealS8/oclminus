package oclminus.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import oclminus.type.ModelTypeContext;

public final class ObjectStore {

    private final ModelTypeContext modelTypeContext;

    public ObjectStore(ModelTypeContext modelTypeContext) {
        this.modelTypeContext =
            Objects.requireNonNull(modelTypeContext, "ModelTypeContext darf nicht null sein.");
    }

    private final List<OclObject> objects = new ArrayList<>();

    public void add(OclObject object) {
        objects.add(
                Objects.requireNonNull(
                        object,
                        "Objekt darf nicht null sein."
                )
        );
    }

    public OclRelation allInstances(String className) {
        
        Objects.requireNonNull(className, "Klassenname darf nicht null sein.");

        if (className.isBlank()) {
            throw new IllegalArgumentException("Klassenname darf nicht leer sein.");
        }

        List<OclValue> matchingObjects = new ArrayList<>();

        for (OclObject object : objects) {

            if (modelTypeContext.isSameOrSubclass(object.className(), className)) {
                matchingObjects.add(object);
            }
        }

        return new OclRelation(matchingObjects);
    }
}