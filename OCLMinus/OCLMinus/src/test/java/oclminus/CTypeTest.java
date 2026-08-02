package oclminus;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import oclminus.type.CType;
import oclminus.type.PrimitiveType;
import oclminus.type.ClassType;
import oclminus.type.CollectionKind;
final class CTypeTest {

    @Test
    void createsSingletonType() {
        CType type =
                CType.singletonOf(
                        PrimitiveType.INTEGER
                );

        assertEquals(
                PrimitiveType.INTEGER,
                type.memberType()
        );

        assertEquals(1, type.lowerBound());
        assertEquals(1, type.upperBound());

        assertTrue(type.isSingleton());
        assertFalse(type.isOption());
        assertFalse(type.isCollectionKind());
    }

    @Test
    void createsOptionType() {
        CType type =
                CType.optionOf(
                        new ClassType("Person")
                );

        assertEquals(0, type.lowerBound());
        assertEquals(1, type.upperBound());

        assertTrue(type.isOption());
        assertFalse(type.isSingleton());
        assertFalse(type.isCollectionKind());
    }

    @Test
    void createsSetType() {
        CType type =
                CType.setOf(
                        PrimitiveType.INTEGER
                );

        assertEquals(0, type.lowerBound());
        assertTrue(type.hasUnboundedUpperBound());

        assertEquals(
                CollectionKind.SET,
                type.collectionKind()
        );
    }

    @Test
    void createsNestedType() {
        CType inner =
                CType.setOf(
                        PrimitiveType.INTEGER
                );

        CType outer =
                CType.sequenceOf(inner);

        assertEquals(
                inner,
                outer.memberType()
        );

        assertEquals(
                CollectionKind.SEQUENCE,
                outer.collectionKind()
        );
    }

    @Test
    void rejectsInvalidBounds() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CType(
                        PrimitiveType.INTEGER,
                        2,
                        1,
                        null,
                        null
                )
        );
    }

    @Test
        void createsNullableVersion() {
        CType singleton =
                CType.singletonOf(
                        PrimitiveType.INTEGER
                );

        assertEquals(
                CType.optionOf(
                        PrimitiveType.INTEGER
                ),
                singleton.nullable()
        );
        }
}