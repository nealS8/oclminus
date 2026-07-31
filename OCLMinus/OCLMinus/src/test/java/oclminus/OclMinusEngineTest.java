package oclminus;

import oclminus.runtime.Environment;
import oclminus.runtime.OclBoolean;
import oclminus.runtime.OclInteger;
import oclminus.runtime.OclObject;
import oclminus.runtime.OclRelation;
import oclminus.runtime.OclValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

class OclMinusEngineTest {

    private final OclMinusEngine engine =
            new OclMinusEngine();

    @Test
    void evaluatesIntegerLiteral() {
        OclValue result = engine.evaluate("42");

        OclRelation relation =
                assertInstanceOf(OclRelation.class, result);

        assertEquals(1, relation.elements().size());

        OclInteger integer =
                assertInstanceOf(
                        OclInteger.class,
                        relation.elements().get(0)
                );

        assertEquals(42, integer.value());
    }

    @Test
    void evaluatesTrueLiteral() {
        OclValue result = engine.evaluate("true");

        OclRelation relation =
                assertInstanceOf(OclRelation.class, result);

        assertEquals(1, relation.elements().size());

        OclBoolean bool =
                assertInstanceOf(
                        OclBoolean.class,
                        relation.elements().get(0)
                );

        assertEquals(true, bool.value());
    }

    @Test
    void evaluatesFalseLiteral() {
        OclValue result = engine.evaluate("false");

        OclRelation relation =
                assertInstanceOf(OclRelation.class, result);

        assertEquals(1, relation.elements().size());

        OclBoolean bool =
                assertInstanceOf(
                        OclBoolean.class,
                        relation.elements().get(0)
                );

        assertEquals(false, bool.value());
    }

    @Test
    void evaluatesAddition() {
        OclValue result = engine.evaluate("2 + 3");

        OclRelation relation =
                assertInstanceOf(OclRelation.class, result);

        assertEquals(1, relation.elements().size());

        OclInteger integer =
                assertInstanceOf(
                        OclInteger.class,
                        relation.elements().get(0)
                );

        assertEquals(5, integer.value());
    }

    @Test
    void evaluatesMultiplication() {
        OclValue result = engine.evaluate("2 * 3");

        OclRelation relation =
                assertInstanceOf(OclRelation.class, result);

        assertEquals(1, relation.elements().size());

        OclInteger integer =
                assertInstanceOf(
                        OclInteger.class,
                        relation.elements().get(0)
                );

        assertEquals(6, integer.value());
    }

    @Test
    void respectsOperatorPrecedence() {
        OclValue result = engine.evaluate("2 + 3 * 4");

        OclRelation relation =
                assertInstanceOf(OclRelation.class, result);

        assertEquals(1, relation.elements().size());

        OclInteger integer =
                assertInstanceOf(
                        OclInteger.class,
                        relation.elements().get(0)
                );

        assertEquals(14, integer.value());
    }

    @Test
    void rejectsBooleanAddition() {
        assertThrows(
                IllegalStateException.class,
                () -> engine.evaluate("true + 3")
        );
    }

    @Test
    void evaluatesIntegerEquality() {
        assertEquals(
                new OclRelation(
                        List.of(new OclBoolean(true))
                ),
                engine.evaluate("2 = 2")
        );
    }

    @Test
    void evaluatesIntegerInequality() {
        assertEquals(
                new OclRelation(
                        List.of(new OclBoolean(false))
                ),
                engine.evaluate("2 = 3")
        );
    }

    @Test
    void evaluatesBooleanEquality() {
        assertEquals(
                new OclRelation(
                        List.of(new OclBoolean(true))
                ),
                engine.evaluate("true = true")
        );
    }

    @Test
    void equalityHasLowerPrecedenceThanAddition() {
        assertEquals(
                new OclRelation(
                        List.of(new OclBoolean(true))
                ),
                engine.evaluate("2 + 3 = 5")
        );
    }

    @Test
        void evaluatesPropertyAccessThroughEngine() {
        OclObject alice =
                new OclObject(
                        "alice",
                        "Person",
                        Map.of(
                                "age",
                                new OclRelation(
                                        List.of(new OclInteger(25))
                                )
                        )
                );

        Environment environment =
                new Environment();

        environment.define(
                "person",
                new OclRelation(List.of(alice))
        );

        OclMinusEngine engine =
                new OclMinusEngine(environment);

        assertEquals(
                new OclRelation(
                        List.of(new OclInteger(25))
                ),
                engine.evaluate("person.age")
        );
        }

@Test
        void evaluatesChainedPropertyAccess() {
        OclObject alice =
                new OclObject(
                        "alice",
                        "Person",
                        Map.of(
                                "age",
                                new OclRelation(
                                        List.of(new OclInteger(25))
                                )
                        )
                );

        OclObject bob =
                new OclObject(
                        "bob",
                        "Person",
                        Map.of(
                                "age",
                                new OclRelation(
                                        List.of(new OclInteger(31))
                                )
                        )
                );

        OclObject company =
                new OclObject(
                        "company1",
                        "Company",
                        Map.of(
                                "employees",
                                new OclRelation(
                                        List.of(alice, bob)
                                )
                        )
                );

        Environment environment =
                new Environment();

        environment.define(
                "company",
                new OclRelation(List.of(company))
        );

        OclMinusEngine engine =
                new OclMinusEngine(environment);

        assertEquals(
                new OclRelation(
                        List.of(
                                new OclInteger(25),
                                new OclInteger(31)
                        )
                ),
                engine.evaluate("company.employees.age")
        );
        }

@Test
        void chainedPropertyAccessStopsAtEmptyRelation() {
        OclObject company =
                new OclObject(
                        "company1",
                        "Company",
                        Map.of(
                                "employees",
                                new OclRelation(List.of())
                        )
                );

        Environment environment =
                new Environment();

        environment.define(
                "company",
                new OclRelation(List.of(company))
        );

        OclMinusEngine engine =
                new OclMinusEngine(environment);

        assertEquals(
                new OclRelation(List.of()),
                engine.evaluate("company.employees.age")
        );
        }
}