package oclminus.type;

import java.util.Objects;

public record CType(
        MemberType memberType,
        int lowerBound,
        Integer upperBound,
        Boolean unique,
        Boolean ordered
) implements MemberType {

    public CType {
        Objects.requireNonNull(
                memberType,
                "Membertyp darf nicht null sein."
        );

        if (lowerBound < 0) {
            throw new IllegalArgumentException(
                    "Die untere Grenze darf nicht negativ sein."
            );
        }

        if (upperBound != null && upperBound < 0) {
            throw new IllegalArgumentException(
                    "Die obere Grenze darf nicht negativ sein."
            );
        }

        if (upperBound != null && lowerBound > upperBound) {
            throw new IllegalArgumentException(
                    "Die untere Grenze darf nicht größer "
                            + "als die obere Grenze sein."
            );
        }
    }

    public boolean hasUnboundedUpperBound() {
        return upperBound == null;
    }

    public boolean isSingleton() {
        return lowerBound == 1
                && Integer.valueOf(1).equals(upperBound);
    }

    public boolean isOption() {
        return lowerBound == 0
                && Integer.valueOf(1).equals(upperBound);
    }

    public boolean isCollectionKind() {
        return unique != null
                && ordered != null;
    }

    public CollectionKind collectionKind() {
        if (!isCollectionKind()) {
            throw new IllegalStateException(
                    "Singleton- und Option-Typen besitzen "
                            + "keinen CollectionKind."
            );
        }

        if (unique && !ordered) {
            return CollectionKind.SET;
        }

        if (!unique && !ordered) {
            return CollectionKind.BAG;
        }

        if (unique) {
            return CollectionKind.ORDERED_SET;
        }

        return CollectionKind.SEQUENCE;
    }

    public static CType singletonOf(
            MemberType memberType
    ) {
        return new CType(
                memberType,
                1,
                1,
                null,
                null
        );
    }

    public static CType optionOf(
            MemberType memberType
    ) {
        return new CType(
                memberType,
                0,
                1,
                null,
                null
        );
    }

    public static CType setOf(
            MemberType memberType
    ) {
        return collectionOf(
                memberType,
                CollectionKind.SET
        );
    }

    public static CType bagOf(
            MemberType memberType
    ) {
        return collectionOf(
                memberType,
                CollectionKind.BAG
        );
    }

    public static CType orderedSetOf(
            MemberType memberType
    ) {
        return collectionOf(
                memberType,
                CollectionKind.ORDERED_SET
        );
    }

    public static CType sequenceOf(
            MemberType memberType
    ) {
        return collectionOf(
                memberType,
                CollectionKind.SEQUENCE
        );
    }

    public static CType collectionOf(
            MemberType memberType,
            CollectionKind kind
    ) {
        Objects.requireNonNull(
                kind,
                "CollectionKind darf nicht null sein."
        );

        return new CType(
                memberType,
                0,
                null,
                kind.isUnique(),
                kind.isOrdered()
        );
    }

    public CType nullable() {
        return new CType(
                memberType,
                0,
                upperBound,
                unique,
                ordered
        );
        }
}