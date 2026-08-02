package oclminus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import oclminus.ast.AllInstancesExpression;
import oclminus.ast.BinaryExpression;
import oclminus.ast.BooleanLiteral;
import oclminus.ast.CoercionExpression;
import oclminus.ast.IntegerLiteral;
import oclminus.ast.LiftExpression;
import oclminus.ast.LowerExpression;
import oclminus.ast.NoExpression;
import oclminus.ast.PropertyAccessExpression;
import oclminus.ast.UnaryExpression;
import oclminus.ast.VariableExpression;
import oclminus.type.*;
import oclminus.ast.Expression;
import oclminus.ast.BinaryOperator;
import oclminus.ast.UnaryOperator;

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

    @Test
    void determinesMergeTypeForSetAndSingleton() {
        TypeChecker checker =
                new TypeChecker();

        Expression expression =
                new BinaryExpression(
                        new CoercionExpression(
                                new IntegerLiteral(1),
                                CollectionKind.SET
                        ),
                        BinaryOperator.MERGE,
                        new IntegerLiteral(2)
                );

        assertEquals(
                new CType(
                        PrimitiveType.INTEGER,
                        1,
                        null,
                        true,
                        false
                ),
                checker.determineType(expression)
        );
    }

    @Test
    void determinesMergeTypeForBagAndSingleton() {
        TypeChecker checker =
                new TypeChecker();

        Expression expression =
                new BinaryExpression(
                        new CoercionExpression(
                                new IntegerLiteral(1),
                                CollectionKind.BAG
                        ),
                        BinaryOperator.MERGE,
                        new IntegerLiteral(2)
                );

        assertEquals(
                new CType(
                        PrimitiveType.INTEGER,
                        1,
                        null,
                        false,
                        false
                ),
                checker.determineType(expression)
        );
    }

    @Test
    void mergeRejectsSingletonAsLeftOperand() {
        TypeChecker checker =
                new TypeChecker();

        Expression expression =
                new BinaryExpression(
                        new IntegerLiteral(1),
                        BinaryOperator.MERGE,
                        new IntegerLiteral(2)
                );

        assertThrows(
                TypeCheckException.class,
                () -> checker.determineType(expression)
        );
    }

    @Test
    void mergeRejectsIncompatibleMemberTypes() {
        TypeChecker checker =
                new TypeChecker();

        Expression expression =
                new BinaryExpression(
                        new CoercionExpression(
                                new IntegerLiteral(1),
                                CollectionKind.SET
                        ),
                        BinaryOperator.MERGE,
                        new BooleanLiteral(true)
                );

        assertThrows(
                TypeCheckException.class,
                () -> checker.determineType(expression)
        );
    }

@Test
        void determinesAdditionType() {
        TypeChecker checker =
                new TypeChecker();

        Expression expression =
                new BinaryExpression(
                        new IntegerLiteral(1),
                        BinaryOperator.PLUS,
                        new IntegerLiteral(2)
                );

        assertEquals(
                CType.singletonOf(
                        PrimitiveType.INTEGER
                ),
                checker.determineType(expression)
        );
        }

@Test
        void additionWithOptionalOperandProducesOptionType() {
        TypeEnvironment environment =
                new TypeEnvironment();

        environment.define(
                "optionalValue",
                CType.optionOf(
                        PrimitiveType.INTEGER
                )
        );

        TypeChecker checker =
                new TypeChecker(environment);

        Expression expression =
                new BinaryExpression(
                        new VariableExpression("optionalValue"),
                        BinaryOperator.PLUS,
                        new IntegerLiteral(2)
                );

        assertEquals(
                CType.optionOf(
                        PrimitiveType.INTEGER
                ),
                checker.determineType(expression)
        );
        }

@Test
        void additionRejectsBooleanOperand() {
        TypeChecker checker =
                new TypeChecker();

        Expression expression =
                new BinaryExpression(
                        new BooleanLiteral(true),
                        BinaryOperator.PLUS,
                        new IntegerLiteral(2)
                );

        assertThrows(
                TypeCheckException.class,
                () -> checker.determineType(expression)
        );
        }

@Test
        void additionRejectsIntegerCollection() {
        TypeEnvironment environment =
                new TypeEnvironment();

        environment.define(
                "values",
                CType.sequenceOf(
                        PrimitiveType.INTEGER
                )
        );

        TypeChecker checker =
                new TypeChecker(environment);

        Expression expression =
                new BinaryExpression(
                        new VariableExpression("values"),
                        BinaryOperator.PLUS,
                        new IntegerLiteral(2)
                );

        assertThrows(
                TypeCheckException.class,
                () -> checker.determineType(expression)
        );
        }

@Test
        void determinesLessThanType() {
        TypeChecker checker =
                new TypeChecker();

        Expression expression =
                new BinaryExpression(
                        new IntegerLiteral(1),
                        BinaryOperator.LESS_THAN,
                        new IntegerLiteral(2)
                );

        assertEquals(
                CType.singletonOf(
                        PrimitiveType.BOOLEAN
                ),
                checker.determineType(expression)
        );
        }

@Test
        void comparisonWithOptionalIntegerProducesOptionalBoolean() {
        TypeEnvironment environment =
                new TypeEnvironment();

        environment.define(
                "optionalValue",
                CType.optionOf(
                        PrimitiveType.INTEGER
                )
        );

        TypeChecker checker =
                new TypeChecker(environment);

        Expression expression =
                new BinaryExpression(
                        new VariableExpression("optionalValue"),
                        BinaryOperator.LESS_THAN,
                        new IntegerLiteral(10)
                );

        assertEquals(
                CType.optionOf(
                        PrimitiveType.BOOLEAN
                ),
                checker.determineType(expression)
        );
        }

@Test
        void lessThanRejectsBooleanOperand() {
        TypeChecker checker =
                new TypeChecker();

        Expression expression =
                new BinaryExpression(
                        new BooleanLiteral(true),
                        BinaryOperator.LESS_THAN,
                        new IntegerLiteral(2)
                );

        assertThrows(
                TypeCheckException.class,
                () -> checker.determineType(expression)
        );
        }

@Test
        void equalityProducesSingletonBooleanType() {
        TypeChecker checker =
                new TypeChecker();

        Expression expression =
                new BinaryExpression(
                        new IntegerLiteral(1),
                        BinaryOperator.EQUAL,
                        new IntegerLiteral(2)
                );

        assertEquals(
                CType.singletonOf(
                        PrimitiveType.BOOLEAN
                ),
                checker.determineType(expression)
        );
        }

@Test
        void equalityAllowsOptionAndSingletonOfSameMemberType() {
        TypeEnvironment environment =
                new TypeEnvironment();

        environment.define(
                "optionalValue",
                CType.optionOf(
                        PrimitiveType.INTEGER
                )
        );

        TypeChecker checker =
                new TypeChecker(environment);

        Expression expression =
                new BinaryExpression(
                        new VariableExpression("optionalValue"),
                        BinaryOperator.EQUAL,
                        new IntegerLiteral(2)
                );

        assertEquals(
                CType.singletonOf(
                        PrimitiveType.BOOLEAN
                ),
                checker.determineType(expression)
        );
        }

@Test
        void equalityRejectsDifferentCollectionKinds() {
        TypeChecker checker =
                new TypeChecker();

        Expression expression =
                new BinaryExpression(
                        new CoercionExpression(
                                new IntegerLiteral(1),
                                CollectionKind.SET
                        ),
                        BinaryOperator.EQUAL,
                        new CoercionExpression(
                                new IntegerLiteral(1),
                                CollectionKind.BAG
                        )
                );

        assertThrows(
                TypeCheckException.class,
                () -> checker.determineType(expression)
        );
        }

@Test
        void equalityRejectsIntegerAndBoolean() {
        TypeChecker checker =
                new TypeChecker();

        Expression expression =
                new BinaryExpression(
                        new IntegerLiteral(1),
                        BinaryOperator.EQUAL,
                        new BooleanLiteral(true)
                );

        assertThrows(
                TypeCheckException.class,
                () -> checker.determineType(expression)
        );
        }

@Test
        void determinesAndType() {
        TypeChecker checker =
                new TypeChecker();

        Expression expression =
                new BinaryExpression(
                        new BooleanLiteral(true),
                        BinaryOperator.AND,
                        new BooleanLiteral(false)
                );

        assertEquals(
                CType.singletonOf(
                        PrimitiveType.BOOLEAN
                ),
                checker.determineType(expression)
        );
        }

@Test
        void logicalOperationWithOptionalBooleanProducesOption() {
        TypeEnvironment environment =
                new TypeEnvironment();

        environment.define(
                "optionalFlag",
                CType.optionOf(
                        PrimitiveType.BOOLEAN
                )
        );

        TypeChecker checker =
                new TypeChecker(environment);

        Expression expression =
                new BinaryExpression(
                        new VariableExpression("optionalFlag"),
                        BinaryOperator.OR,
                        new BooleanLiteral(true)
                );

        assertEquals(
                CType.optionOf(
                        PrimitiveType.BOOLEAN
                ),
                checker.determineType(expression)
        );
        }

@Test
        void logicalOperationRejectsIntegerOperand() {
        TypeChecker checker =
                new TypeChecker();

        Expression expression =
                new BinaryExpression(
                        new IntegerLiteral(1),
                        BinaryOperator.AND,
                        new BooleanLiteral(true)
                );

        assertThrows(
                TypeCheckException.class,
                () -> checker.determineType(expression)
        );
        }

@Test
        void determinesNotType() {
        TypeChecker checker =
                new TypeChecker();

        Expression expression =
                new UnaryExpression(
                        UnaryOperator.NOT,
                        new BooleanLiteral(true)
                );

        assertEquals(
                CType.singletonOf(
                        PrimitiveType.BOOLEAN
                ),
                checker.determineType(expression)
        );
        }

@Test
        void determinesUnaryMinusType() {
        TypeChecker checker =
                new TypeChecker();

        Expression expression =
                new UnaryExpression(
                        UnaryOperator.NEGATE,
                        new IntegerLiteral(5)
                );

        assertEquals(
                CType.singletonOf(
                        PrimitiveType.INTEGER
                ),
                checker.determineType(expression)
        );
        }

@Test
        void notRejectsInteger() {
        TypeChecker checker =
                new TypeChecker();

        assertThrows(
                TypeCheckException.class,
                () -> checker.determineType(
                        new UnaryExpression(
                                UnaryOperator.NOT,
                                new IntegerLiteral(1)
                        )
                )
        );
        }

@Test
        void unaryMinusRejectsBoolean() {
        TypeChecker checker =
                new TypeChecker();

        assertThrows(
                TypeCheckException.class,
                () -> checker.determineType(
                        new UnaryExpression(
                                UnaryOperator.NEGATE,
                                new BooleanLiteral(true)
                        )
                )
        );
        }

@Test
        void determinesAllInstancesType() {
        TypeChecker checker =
                new TypeChecker();

        assertEquals(
                CType.setOf(
                        new ClassType("Person")
                ),
                checker.determineType(
                        new AllInstancesExpression("Person")
                )
        );
        }

@Test
        void determinesPropertyAccessTypeAfterAllInstances() {
        ModelTypeContext model =
                new ModelTypeContext();

        model.defineProperty(
                "Person",
                "age",
                CType.singletonOf(
                        PrimitiveType.INTEGER
                )
        );

        TypeChecker checker =
                new TypeChecker(
                        new TypeEnvironment(),
                        model
                );

        Expression expression =
                new PropertyAccessExpression(
                        new AllInstancesExpression("Person"),
                        "age"
                );

        assertEquals(
                new CType(
                        PrimitiveType.INTEGER,
                        0,
                        null,
                        false,
                        false
                ),
                checker.determineType(expression)
        );
        }

@Test
        void determinesOptionalPropertyAccessType() {
        ModelTypeContext model =
                new ModelTypeContext();

        model.defineProperty(
                "Person",
                "manager",
                CType.optionOf(
                        new ClassType("Person")
                )
        );

        TypeEnvironment environment =
                new TypeEnvironment();

        environment.define(
                "person",
                CType.singletonOf(
                        new ClassType("Person")
                )
        );

        TypeChecker checker =
                new TypeChecker(
                        environment,
                        model
                );

        assertEquals(
                CType.optionOf(
                        new ClassType("Person")
                ),
                checker.determineType(
                        new PropertyAccessExpression(
                                new VariableExpression("person"),
                                "manager"
                        )
                )
        );
        }

@Test
        void rejectsUnknownProperty() {
        TypeEnvironment environment =
                new TypeEnvironment();

        environment.define(
                "person",
                CType.singletonOf(
                        new ClassType("Person")
                )
        );

        TypeChecker checker =
                new TypeChecker(
                        environment,
                        new ModelTypeContext()
                );

        assertThrows(
                TypeCheckException.class,
                () -> checker.determineType(
                        new PropertyAccessExpression(
                                new VariableExpression("person"),
                                "age"
                        )
                )
        );
        }
}