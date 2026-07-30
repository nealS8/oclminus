package oclminus.runtime;

import java.util.List;
import java.util.Objects;

public record OclRelation(List<OclValue> elements)
        implements OclValue {

    public OclRelation {
        Objects.requireNonNull(
                elements,
                "Elementliste darf nicht null sein."
        );

        elements = List.copyOf(elements);
    }
}
