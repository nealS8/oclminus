package oclminus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import oclminus.type.PrimitiveType;
import oclminus.type.TypedExpression;
import oclminus.type.CType;
import oclminus.ast.VariableExpression;

final class TypedExpressionTest {

    @Test
    void combinesExpressionAndType() {
        VariableExpression expression =
                new VariableExpression("value");

        CType type =
                CType.setOf(
                        PrimitiveType.INTEGER
                );

        TypedExpression typedExpression =
                new TypedExpression(
                        expression,
                        type
                );

        assertEquals(
                expression,
                typedExpression.expression()
        );

        assertEquals(
                type,
                typedExpression.type()
        );
    }

    @Test
    void rejectsNullExpression() {
        assertThrows(
                NullPointerException.class,
                () -> new TypedExpression(
                        null,
                        CType.setOf(
                                PrimitiveType.INTEGER
                        )
                )
        );
    }

    @Test
    void rejectsNullType() {
        assertThrows(
                NullPointerException.class,
                () -> new TypedExpression(
                        new VariableExpression("value"),
                        null
                )
        );
    }
}