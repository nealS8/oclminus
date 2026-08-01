package oclminus;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import oclminus.type.CollectionKind;
import org.junit.jupiter.api.Test;

final class CollectionKindTest {

    @Test
    void setIsUniqueAndUnordered() {
        assertTrue(
                CollectionKind.SET.isUnique()
        );

        assertFalse(
                CollectionKind.SET.isOrdered()
        );
    }

    @Test
    void bagIsNonUniqueAndUnordered() {
        assertFalse(
                CollectionKind.BAG.isUnique()
        );

        assertFalse(
                CollectionKind.BAG.isOrdered()
        );
    }

    @Test
    void orderedSetIsUniqueAndOrdered() {
        assertTrue(
                CollectionKind.ORDERED_SET.isUnique()
        );

        assertTrue(
                CollectionKind.ORDERED_SET.isOrdered()
        );
    }

    @Test
    void sequenceIsNonUniqueAndOrdered() {
        assertFalse(
                CollectionKind.SEQUENCE.isUnique()
        );

        assertTrue(
                CollectionKind.SEQUENCE.isOrdered()
        );
    }


}