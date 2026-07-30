package oclminus;

import org.junit.jupiter.api.Test;

import java.util.List;
import oclminus.runtime.OclRelation;
import oclminus.runtime.OclInteger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OclRelationTest {

    @Test
    void createsEmptyRelation() {
        OclRelation relation =
                new OclRelation(List.of());

        assertTrue(relation.elements().isEmpty());
    }

    @Test
    void createsRelationWithOneElement() {
        OclRelation relation =
                new OclRelation(
                        List.of(new OclInteger(42))
                );

        assertEquals(
                List.of(new OclInteger(42)),
                relation.elements()
        );
    }
}