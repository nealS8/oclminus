package oclminus.type;

public enum CollectionKind {

    SET(
            true,
            false
    ),

    BAG(
            false,
            false
    ),

    ORDERED_SET(
            true,
            true
    ),

    SEQUENCE(
            false,
            true
    );

    private final boolean unique;
    private final boolean ordered;

    CollectionKind(
            boolean unique,
            boolean ordered
    ) {
        this.unique = unique;
        this.ordered = ordered;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isOrdered() {
        return ordered;
    }
}