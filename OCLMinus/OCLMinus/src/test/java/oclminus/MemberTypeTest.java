package oclminus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import oclminus.type.ClassType;
import oclminus.type.PrimitiveType;
import org.junit.jupiter.api.Test;

final class MemberTypeTest {

    @Test
    void primitiveTypesExist() {
        assertEquals(
                PrimitiveType.BOOLEAN,
                PrimitiveType.valueOf("BOOLEAN")
        );

        assertEquals(
                PrimitiveType.INTEGER,
                PrimitiveType.valueOf("INTEGER")
        );

        assertEquals(
                PrimitiveType.ANY,
                PrimitiveType.valueOf("ANY")
        );
    }

    @Test
    void createsClassType() {
        ClassType type =
                new ClassType("Person");

        assertEquals(
                "Person",
                type.className()
        );
    }

    @Test
    void rejectsBlankClassName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ClassType("")
        );
    }

    @Test
    void rejectsNullClassName() {
        assertThrows(
                NullPointerException.class,
                () -> new ClassType(null)
        );
    }
}
