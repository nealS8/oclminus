package oclminus;

import oclminus.ast.AllInstancesExpression;
import oclminus.ast.BinaryExpression;
import oclminus.ast.BinaryOperator;
import oclminus.ast.BooleanLiteral;
import oclminus.ast.Expression;
import oclminus.ast.IntegerLiteral;
import oclminus.ast.LiftExpression;
import oclminus.ast.LowerExpression;
import oclminus.ast.NoExpression;
import oclminus.ast.VariableExpression;
import oclminus.runtime.Environment;
import oclminus.runtime.Interpreter;
import oclminus.runtime.ObjectStore;
import oclminus.runtime.OclBoolean;
import oclminus.runtime.OclInteger;
import oclminus.runtime.OclObject;
import oclminus.runtime.OclRelation;
import oclminus.runtime.OclValue;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;

import oclminus.ast.UnaryExpression;
import oclminus.ast.UnaryOperator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InterpreterTest {

    private final Interpreter interpreter =
            new Interpreter();

    @Test
    void evaluatesIntegerLiteral() {
        assertEquals(
                new OclRelation(
                        List.of(new OclInteger(42))
                ),
                interpreter.evaluate(
                        new IntegerLiteral(42)
                )
        );
    }

    @Test
    void evaluatesTrueLiteral() {
        assertEquals(
                new OclRelation(
                        List.of(new OclBoolean(true))
                ),
                interpreter.evaluate(
                        new BooleanLiteral(true)
                )
        );
    }

    @Test
    void evaluatesFalseLiteral() {
        assertEquals(
                new OclRelation(
                        List.of(new OclBoolean(false))
                ),
                interpreter.evaluate(
                        new BooleanLiteral(false)
                )
        );
    }

    @Test
    void evaluatesAddition() {
        Expression expression =
                new BinaryExpression(
                        new IntegerLiteral(2),
                        BinaryOperator.PLUS,
                        new IntegerLiteral(3)
                );

        OclValue result =
                interpreter.evaluate(expression);

        assertEquals(
                new OclRelation(
                        List.of(new OclInteger(5))
                ),
                result
        );
    }

    @Test
    void evaluatesMultiplication() {
        Expression expression =
                new BinaryExpression(
                        new IntegerLiteral(2),
                        BinaryOperator.MULTIPLY,
                        new IntegerLiteral(3)
                );

        OclValue result =
                interpreter.evaluate(expression);

        assertEquals(
                new OclRelation(
                        List.of(new OclInteger(6))
                ),
                result
        );
    }

    @Test
    void evaluatesNestedExpression() {
        Expression expression =
                new BinaryExpression(
                        new IntegerLiteral(2),
                        BinaryOperator.PLUS,
                        new BinaryExpression(
                                new IntegerLiteral(3),
                                BinaryOperator.MULTIPLY,
                                new IntegerLiteral(4)
                        )
                );

        OclValue result =
                interpreter.evaluate(expression);

        assertEquals(
                new OclRelation(
                        List.of(new OclInteger(14))
                ),
                result
        );
    }

    @Test
    void rejectsBooleanAddition() {
        Expression expression =
                new BinaryExpression(
                        new BooleanLiteral(true),
                        BinaryOperator.PLUS,
                        new IntegerLiteral(3)
                );

        assertThrows(
                IllegalStateException.class,
                () -> interpreter.evaluate(expression)
        );
    }

    @Test
    void rejectsNullExpression() {
        assertThrows(
                IllegalArgumentException.class,
                () -> interpreter.evaluate(null)
        );
    }

    @Test
    void evaluatesVariableExpression() {
        Environment environment =
                new Environment();

        environment.define(
                "x",
                new OclRelation(
                        List.of(new OclInteger(42))
                )
        );

        Interpreter interpreter =
                new Interpreter(environment);

        OclValue result =
                interpreter.evaluate(
                        new VariableExpression("x")
                );

        assertEquals(
                new OclRelation(
                        List.of(new OclInteger(42))
                ),
                result
        );
    }

    @Test
    void evaluatesExpressionContainingVariable() {
        Environment environment =
                new Environment();

        environment.define(
                "x",
                new OclRelation(
                        List.of(new OclInteger(5))
                )
        );

        Interpreter interpreter =
                new Interpreter(environment);

        Expression expression =
                new BinaryExpression(
                        new VariableExpression("x"),
                        BinaryOperator.PLUS,
                        new IntegerLiteral(3)
                );

        assertEquals(
                new OclRelation(
                        List.of(new OclInteger(8))
                ),
                interpreter.evaluate(expression)
        );
    }

    @Test
        void evaluatesEqualIntegers() {
        Expression expression =
                new BinaryExpression(
                        new IntegerLiteral(2),
                        BinaryOperator.EQUAL,
                        new IntegerLiteral(2)
                );

        assertEquals(
                new OclRelation(
                        List.of(new OclBoolean(true))
                ),
                interpreter.evaluate(expression)
        );
        }

@Test
        void evaluatesDifferentIntegers() {
        Expression expression =
                new BinaryExpression(
                        new IntegerLiteral(2),
                        BinaryOperator.EQUAL,
                        new IntegerLiteral(3)
                );

        assertEquals(
                new OclRelation(
                        List.of(new OclBoolean(false))
                ),
                interpreter.evaluate(expression)
        );
        }

@Test
void evaluatesEqualBooleans() {
    Expression expression =
            new BinaryExpression(
                    new BooleanLiteral(true),
                    BinaryOperator.EQUAL,
                    new BooleanLiteral(true)
            );

    assertEquals(
            new OclRelation(
                    List.of(new OclBoolean(true))
            ),
            interpreter.evaluate(expression)
    );
}

@Test
void evaluatesDifferentTypesAsNotEqual() {
    Expression expression =
            new BinaryExpression(
                    new IntegerLiteral(1),
                    BinaryOperator.EQUAL,
                    new BooleanLiteral(true)
            );

    assertEquals(
            new OclRelation(
                    List.of(new OclBoolean(false))
            ),
            interpreter.evaluate(expression)
    );
}

@Test
void evaluatesSubtraction() {
    Expression expression =
            new BinaryExpression(
                    new IntegerLiteral(7),
                    BinaryOperator.MINUS,
                    new IntegerLiteral(3)
            );

    OclValue result =
            interpreter.evaluate(expression);

    assertEquals(
            new OclRelation(
                    List.of(new OclInteger(4))
            ),
            result
    );
}

@Test
void evaluatesDivision() {
    Expression expression =
            new BinaryExpression(
                    new IntegerLiteral(8),
                    BinaryOperator.DIVIDE,
                    new IntegerLiteral(2)
            );

    OclValue result =
            interpreter.evaluate(expression);

    assertEquals(
            new OclRelation(
                    List.of(new OclInteger(4))
            ),
            result
    );
}

@Test
void rejectsDivisionByZero() {
    Expression expression =
            new BinaryExpression(
                    new IntegerLiteral(8),
                    BinaryOperator.DIVIDE,
                    new IntegerLiteral(0)
            );

    assertThrows(
            ArithmeticException.class,
            () -> interpreter.evaluate(expression)
    );
}

@Test
void evaluatesNotEqualIntegers() {
    Expression expression =
            new BinaryExpression(
                    new IntegerLiteral(2),
                    BinaryOperator.NOT_EQUAL,
                    new IntegerLiteral(3)
            );

    assertEquals(
            new OclRelation(
                    List.of(new OclBoolean(true))
            ),
            interpreter.evaluate(expression)
    );
}

@Test
void evaluatesLessThan() {
    Expression expression =
            new BinaryExpression(
                    new IntegerLiteral(2),
                    BinaryOperator.LESS_THAN,
                    new IntegerLiteral(3)
            );

    assertEquals(
            new OclRelation(
                    List.of(new OclBoolean(true))
            ),
            interpreter.evaluate(expression)
    );
}

@Test
void evaluatesLessThanOrEqual() {
    Expression expression =
            new BinaryExpression(
                    new IntegerLiteral(3),
                    BinaryOperator.LESS_THAN_OR_EQUAL,
                    new IntegerLiteral(3)
            );

    assertEquals(
            new OclRelation(
                    List.of(new OclBoolean(true))
            ),
            interpreter.evaluate(expression)
    );
}

@Test
void evaluatesGreaterThan() {
    Expression expression =
            new BinaryExpression(
                    new IntegerLiteral(5),
                    BinaryOperator.GREATER_THAN,
                    new IntegerLiteral(2)
            );

    assertEquals(
            new OclRelation(
                    List.of(new OclBoolean(true))
            ),
            interpreter.evaluate(expression)
    );
}

@Test
void evaluatesGreaterThanOrEqual() {
    Expression expression =
            new BinaryExpression(
                    new IntegerLiteral(5),
                    BinaryOperator.GREATER_THAN_OR_EQUAL,
                    new IntegerLiteral(5)
            );

    assertEquals(
            new OclRelation(
                    List.of(new OclBoolean(true))
            ),
            interpreter.evaluate(expression)
    );
}

@Test
void evaluatesAnd() {
    Expression expression =
            new BinaryExpression(
                    new BooleanLiteral(true),
                    BinaryOperator.AND,
                    new BooleanLiteral(false)
            );

    assertEquals(
            new OclRelation(
                    List.of(new OclBoolean(false))
            ),
            interpreter.evaluate(expression)
    );
}

@Test
void evaluatesOr() {
    Expression expression =
            new BinaryExpression(
                    new BooleanLiteral(true),
                    BinaryOperator.OR,
                    new BooleanLiteral(false)
            );

    assertEquals(
            new OclRelation(
                    List.of(new OclBoolean(true))
            ),
            interpreter.evaluate(expression)
    );
}

@Test
void evaluatesXor() {
    Expression expression =
            new BinaryExpression(
                    new BooleanLiteral(true),
                    BinaryOperator.XOR,
                    new BooleanLiteral(false)
            );

    assertEquals(
            new OclRelation(
                    List.of(new OclBoolean(true))
            ),
            interpreter.evaluate(expression)
    );
}

@Test
void evaluatesEqualBooleansWithXorAsFalse() {
    Expression expression =
            new BinaryExpression(
                    new BooleanLiteral(true),
                    BinaryOperator.XOR,
                    new BooleanLiteral(true)
            );

    assertEquals(
            new OclRelation(
                    List.of(new OclBoolean(false))
            ),
            interpreter.evaluate(expression)
    );
}

@Test
void evaluatesTrueImpliesFalseAsFalse() {
    Expression expression =
            new BinaryExpression(
                    new BooleanLiteral(true),
                    BinaryOperator.IMPLIES,
                    new BooleanLiteral(false)
            );

    assertEquals(
            new OclRelation(
                    List.of(new OclBoolean(false))
            ),
            interpreter.evaluate(expression)
    );
}

@Test
void evaluatesFalseImpliesFalseAsTrue() {
    Expression expression =
            new BinaryExpression(
                    new BooleanLiteral(false),
                    BinaryOperator.IMPLIES,
                    new BooleanLiteral(false)
            );

    assertEquals(
            new OclRelation(
                    List.of(new OclBoolean(true))
            ),
            interpreter.evaluate(expression)
    );
}

@Test
void evaluatesUnaryNegation() {
    Expression expression =
            new UnaryExpression(
                    UnaryOperator.NEGATE,
                    new IntegerLiteral(5)
            );

    assertEquals(
            new OclRelation(
                    List.of(new OclInteger(-5))
            ),
            interpreter.evaluate(expression)
    );
}

@Test
void evaluatesNot() {
    Expression expression =
            new UnaryExpression(
                    UnaryOperator.NOT,
                    new BooleanLiteral(false)
            );

    assertEquals(
            new OclRelation(
                    List.of(new OclBoolean(true))
            ),
            interpreter.evaluate(expression)
    );
}

@Test
void evaluatesNegatedAddition() {
    Expression expression =
            new UnaryExpression(
                    UnaryOperator.NEGATE,
                    new BinaryExpression(
                            new IntegerLiteral(2),
                            BinaryOperator.PLUS,
                            new IntegerLiteral(3)
                    )
            );

    assertEquals(
            new OclRelation(
                    List.of(new OclInteger(-5))
            ),
            interpreter.evaluate(expression)
    );
}

@Test
void rejectsNotOnInteger() {
    Expression expression =
            new UnaryExpression(
                    UnaryOperator.NOT,
                    new IntegerLiteral(1)
            );

    assertThrows(
            IllegalStateException.class,
            () -> interpreter.evaluate(expression)
    );
}

@Test
void rejectsComparisonOfBooleans() {
    Expression expression =
            new BinaryExpression(
                    new BooleanLiteral(true),
                    BinaryOperator.LESS_THAN,
                    new BooleanLiteral(false)
            );

    assertThrows(
            IllegalStateException.class,
            () -> interpreter.evaluate(expression)
    );
}

@Test
        void evaluatesAllInstancesExpression() {
        OclObject alice =
                new OclObject(
                        "alice",
                        "Person",
                        Map.of()
                );

        OclObject bob =
                new OclObject(
                        "bob",
                        "Person",
                        Map.of()
                );

        OclObject company =
                new OclObject(
                        "company1",
                        "Company",
                        Map.of()
                );

        ObjectStore objectStore =
                new ObjectStore();

        objectStore.add(alice);
        objectStore.add(bob);
        objectStore.add(company);

        Interpreter interpreter =
                new Interpreter(
                        new Environment(),
                        objectStore
                );

        OclValue result = interpreter.evaluate(
                new AllInstancesExpression("Person")
        );

        assertEquals(
                new OclRelation(
                        List.of(alice, bob)
                ),
                result
        );
        }

@Test
        void allInstancesReturnsEmptyRelationForUnknownClass() {
        Interpreter interpreter =
                new Interpreter(
                        new Environment(),
                        new ObjectStore()
                );

        assertEquals(
                new OclRelation(List.of()),
                interpreter.evaluate(
                        new AllInstancesExpression("Person")
                )
        );
        }

@Test
        void evaluatesNoExpressionAsEmptyRelation() {
        Interpreter interpreter = new Interpreter();

        OclValue result = interpreter.evaluate(
                new NoExpression("Person")
        );

        assertEquals(
                new OclRelation(List.of()),
                result
        );
        }

@Test
        void evaluatesNoIntegerAsEmptyRelation() {
        Interpreter interpreter = new Interpreter();

        assertEquals(
                new OclRelation(List.of()),
                interpreter.evaluate(
                        new NoExpression("Integer")
                )
        );
        }

@Test
        void evaluatesNoExpressionThroughEngine() {
        OclMinusEngine engine = new OclMinusEngine();

        assertEquals(
                new OclRelation(List.of()),
                engine.evaluate("no Person")
        );
        }

@Test
        void evaluatesLiftExpression() {
        Environment environment =
                new Environment();

        OclRelation original =
                new OclRelation(
                        List.of(
                                new OclInteger(1),
                                new OclInteger(2)
                        )
                );

        environment.define("value", original);

        Interpreter interpreter =
                new Interpreter(environment);

        assertEquals(
                new OclRelation(
                        List.of(original)
                ),
                interpreter.evaluate(
                        new LiftExpression(
                                new VariableExpression("value")
                        )
                )
        );
        }

@Test
        void evaluatesLowerExpression() {
        OclRelation inner =
                new OclRelation(
                        List.of(
                                new OclInteger(1),
                                new OclInteger(2)
                        )
                );

        Environment environment =
                new Environment();

        environment.define(
                "value",
                new OclRelation(
                        List.of(inner)
                )
        );

        Interpreter interpreter =
                new Interpreter(environment);

        assertEquals(
                inner,
                interpreter.evaluate(
                        new LowerExpression(
                                new VariableExpression("value")
                        )
                )
        );
        }

@Test
        void liftThenLowerReturnsOriginalRelation() {
        OclRelation original =
                new OclRelation(
                        List.of(
                                new OclInteger(1),
                                new OclInteger(2)
                        )
                );

        Environment environment =
                new Environment();

        environment.define("value", original);

        Interpreter interpreter =
                new Interpreter(environment);

        Expression expression =
                new LowerExpression(
                        new LiftExpression(
                                new VariableExpression("value")
                        )
                );

        assertEquals(
                original,
                interpreter.evaluate(expression)
        );
        }

@Test
        void lowerRejectsNonSingletonRelation() {
        Environment environment =
                new Environment();

        environment.define(
                "value",
                new OclRelation(
                        List.of(
                                new OclInteger(1),
                                new OclInteger(2)
                        )
                )
        );

        Interpreter interpreter =
                new Interpreter(environment);

        assertThrows(
                IllegalStateException.class,
                () -> interpreter.evaluate(
                        new LowerExpression(
                                new VariableExpression("value")
                        )
                )
        );
        }

@Test
        void evaluatesLiftAndLowerThroughEngine() {
        OclRelation original =
                new OclRelation(
                        List.of(
                                new OclInteger(1),
                                new OclInteger(2)
                        )
                );

        Environment environment =
                new Environment();

        environment.define("value", original);

        OclMinusEngine engine =
                new OclMinusEngine(environment);

        assertEquals(
                original,
                engine.evaluate("value↑↓")
        );
        }
}