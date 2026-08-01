package oclminus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import oclminus.ast.BooleanLiteral;
import oclminus.ast.CoercionExpression;
import oclminus.ast.IntegerLiteral;
import oclminus.ast.LiftExpression;
import oclminus.ast.LowerExpression;
import oclminus.ast.NoExpression;
import oclminus.ast.VariableExpression;
import oclminus.type.TypeChecker;
import oclminus.type.CType;
import oclminus.type.PrimitiveType;
import oclminus.type.*;


final class TypeCheckerTest {

    @Test
    void determinesIntegerLiteralType() {
        TypeChecker checker = new TypeChecker();

        assertEquals(
                CType.singletonOf(
                        PrimitiveType.INTEGER
                ),
                checker.determineType(
                        new IntegerLiteral(42)
                )
        );
    }

    @Test
    void determinesBooleanLiteralType() {
        TypeChecker checker = new TypeChecker();

        assertEquals(
                CType.singletonOf(
                        PrimitiveType.BOOLEAN
                ),
                checker.determineType(
                        new BooleanLiteral(true)
                )
        );
    }

    @Test
    void looksUpVariableType() {
        TypeEnvironment environment =
                new TypeEnvironment();

        CType variableType =
                CType.sequenceOf(
                        PrimitiveType.INTEGER
                );

        environment.define(
                "values",
                variableType
        );

        TypeChecker checker =
                new TypeChecker(environment);

        assertEquals(
                variableType,
                checker.determineType(
                        new VariableExpression("values")
                )
        );
    }

    @Test
    void determinesNoExpressionType() {
        TypeChecker checker = new TypeChecker();

        assertEquals(
                CType.optionOf(
                        new ClassType("Person")
                ),
                checker.determineType(
                        new NoExpression("Person")
                )
        );
    }

    @Test
    void liftCreatesNestedSingletonType() {
        TypeEnvironment environment =
                new TypeEnvironment();

        CType originalType =
                CType.setOf(
                        PrimitiveType.INTEGER
                );

        environment.define(
                "value",
                originalType
        );

        TypeChecker checker =
                new TypeChecker(environment);

        assertEquals(
                CType.singletonOf(originalType),
                checker.determineType(
                        new LiftExpression(
                                new VariableExpression("value")
                        )
                )
        );
    }

    @Test
    void lowerRemovesSingletonLevel() {
        CType innerType =
                CType.setOf(
                        PrimitiveType.INTEGER
                );

        TypeEnvironment environment =
                new TypeEnvironment();

        environment.define(
                "value",
                CType.singletonOf(innerType)
        );

        TypeChecker checker =
                new TypeChecker(environment);

        assertEquals(
                innerType,
                checker.determineType(
                        new LowerExpression(
                                new VariableExpression("value")
                        )
                )
        );
    }

    @Test
    void lowerRejectsFlatSingleton() {
        TypeChecker checker = new TypeChecker();

        assertThrows(
                TypeCheckException.class,
                () -> checker.determineType(
                        new LowerExpression(
                                new IntegerLiteral(1)
                        )
                )
        );
    }

    @Test
    void coercionChangesCollectionKind() {
        TypeChecker checker = new TypeChecker();

        assertEquals(
                CType.setOf(
                        PrimitiveType.INTEGER
                ),
                checker.determineType(
                        new CoercionExpression(
                                new IntegerLiteral(1),
                                CollectionKind.SET
                        )
                )
        );
    }
}