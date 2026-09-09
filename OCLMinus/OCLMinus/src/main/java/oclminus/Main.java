package oclminus;

import java.util.List;
import java.util.Map;

import oclminus.runtime.Environment;
import oclminus.runtime.ObjectStore;
import oclminus.runtime.OclInteger;
import oclminus.runtime.OclObject;
import oclminus.runtime.OclRelation;
import oclminus.runtime.OclValue;
import oclminus.type.ClassType;
import oclminus.type.CType;
import oclminus.type.ModelTypeContext;
import oclminus.type.PrimitiveType;
import oclminus.type.TypeEnvironment;
import oclminus.type.ValueTypeChecker;

public class Main {

    public static void main(String[] args) {

        // =====================================================
        // 1. Modellobjekte
        // =====================================================

        OclObject person4 = new OclObject(
                "p4",
                "Person",
                Map.of(
                        "friends",
                        new OclRelation(List.of())
                )
        );

        OclObject person3 = new OclObject(
                "p3",
                "Person",
                Map.of(
                        "friends",
                        new OclRelation(List.of())
                )
        );

        OclObject person2 = new OclObject(
                "p2",
                "Person",
                Map.of(
                        "friends",
                        new OclRelation(List.of(person4))
                )
        );

        OclObject person1 = new OclObject(
                "p1",
                "Person",
                Map.of(
                        "friends",
                        new OclRelation(
                                List.of(person2, person3)
                        )
                )
        );

        // Student1 ist eine Unterklasse von Person
        // und hat Person2 als Freund.
        OclObject student1 = new OclObject(
                "s1",
                "Student",
                Map.of(
                        "friends",
                        new OclRelation(
                                List.of(person2)
                        )
                )
        );


        // =====================================================
        // 2. Environments und Modellkontext
        // =====================================================

        Environment environment = new Environment();
        TypeEnvironment typeEnvironment = new TypeEnvironment();
        ModelTypeContext modelTypeContext = new ModelTypeContext();
        modelTypeContext.defineSuperClass("Student", "Person");

        modelTypeContext.defineProperty(
                "Person",
                "friends",
                CType.setOf(
                        new ClassType("Person")
                )
        );


        // =====================================================
        // 3. Person-Beispiel
        // =====================================================

        environment.define(
                "Person1",
                new OclRelation(List.of(person1))
        );

        typeEnvironment.define(
                "Person1",
                CType.singletonOf(
                        new ClassType("Person")
                )
        );

        modelTypeContext.defineProperty(
                "Person",
                "friends",
                CType.setOf(
                        new ClassType("Person")
                )
        );


        // =====================================================
        // 4. Sequence-Beispiel
        // =====================================================

        environment.define(
                "sequence1",
                new OclRelation(
                        List.of(
                                new OclInteger(1),
                                new OclInteger(2)
                        )
                )
        );

        environment.define(
                "sequence2",
                new OclRelation(
                        List.of(
                                new OclInteger(2),
                                new OclInteger(1)
                        )
                )
        );

        typeEnvironment.define(
                "sequence1",
                CType.sequenceOf(PrimitiveType.INTEGER)
        );

        typeEnvironment.define(
                "sequence2",
                CType.sequenceOf(PrimitiveType.INTEGER)
        );


        // =====================================================
        // 5. Verschachteltes Set-Beispiel
        // =====================================================

        OclRelation innerSet1 = new OclRelation(
                List.of(
                        new OclInteger(1),
                        new OclInteger(2)
                )
        );

        OclRelation innerSet2 = new OclRelation(
                List.of(
                        new OclInteger(2),
                        new OclInteger(1)
                )
        );

        environment.define(
                "outerSet1",
                new OclRelation(List.of(innerSet1))
        );

        environment.define(
                "outerSet2",
                new OclRelation(List.of(innerSet2))
        );

        CType outerSetType = CType.setOf(
                CType.setOf(PrimitiveType.INTEGER)
        );

        typeEnvironment.define(
                "outerSet1",
                outerSetType
        );

        typeEnvironment.define(
                "outerSet2",
                outerSetType
        );


        // =====================================================
        // 6. Set(Set(Integer))-Beispiel
        // =====================================================

        OclRelation nestedSet1 = new OclRelation(
                List.of(
                        new OclRelation(
                                List.of(
                                        new OclInteger(1),
                                        new OclInteger(2)
                                )
                        ),
                        new OclRelation(
                                List.of(
                                        new OclInteger(3),
                                        new OclInteger(4)
                                )
                        )
                )
        );

        OclRelation nestedSet2 = new OclRelation(
                List.of(
                        new OclRelation(
                                List.of(
                                        new OclInteger(4),
                                        new OclInteger(3)
                                )
                        ),
                        new OclRelation(
                                List.of(
                                        new OclInteger(2),
                                        new OclInteger(1)
                                )
                        )
                )
        );

        environment.define(
                "nestedSet1",
                nestedSet1
        );

        environment.define(
                "nestedSet2",
                nestedSet2
        );

        CType nestedSetType = CType.setOf(
                CType.setOf(PrimitiveType.INTEGER)
        );

        typeEnvironment.define(
                "nestedSet1",
                nestedSetType
        );

        typeEnvironment.define(
                "nestedSet2",
                nestedSetType
        );


        // =====================================================
        // 7. Sequence(Set(Integer))-Beispiel
        // =====================================================

        OclRelation nestedSequence1 = new OclRelation(
                List.of(
                        new OclRelation(
                                List.of(
                                        new OclInteger(1),
                                        new OclInteger(2)
                                )
                        ),
                        new OclRelation(
                                List.of(
                                        new OclInteger(3),
                                        new OclInteger(4)
                                )
                        )
                )
        );

        OclRelation nestedSequence2 = new OclRelation(
                List.of(
                        new OclRelation(
                                List.of(
                                        new OclInteger(4),
                                        new OclInteger(3)
                                )
                        ),
                        new OclRelation(
                                List.of(
                                        new OclInteger(2),
                                        new OclInteger(1)
                                )
                        )
                )
        );

        environment.define(
                "nestedSequence1",
                nestedSequence1
        );

        environment.define(
                "nestedSequence2",
                nestedSequence2
        );

        CType nestedSequenceType = CType.sequenceOf(
                CType.setOf(PrimitiveType.INTEGER)
        );

        typeEnvironment.define(
                "nestedSequence1",
                nestedSequenceType
        );

        typeEnvironment.define(
                "nestedSequence2",
                nestedSequenceType
        );


        // =====================================================
        // 8. Tief verschachteltes Set-Beispiel: [[[0]], []]
        // =====================================================

        OclRelation deepValue1 = new OclRelation(
                List.of(
                        new OclRelation(
                                List.of(
                                        new OclRelation(
                                                List.of(
                                                        new OclInteger(0)
                                                )
                                        )
                                )
                        ),
                        new OclRelation(List.of())
                )
        );

        OclRelation deepValue2 = new OclRelation(
                List.of(
                        new OclRelation(List.of()),
                        new OclRelation(
                                List.of(
                                        new OclRelation(
                                                List.of(
                                                        new OclInteger(0)
                                                )
                                        )
                                )
                        )
                )
        );

        environment.define(
                "deepValue1",
                deepValue1
        );

        environment.define(
                "deepValue2",
                deepValue2
        );

        CType deepType = CType.setOf(
                CType.setOf(
                        CType.setOf(
                                PrimitiveType.INTEGER
                        )
                )
        );

        typeEnvironment.define(
                "deepValue1",
                deepType
        );

        typeEnvironment.define(
                "deepValue2",
                deepType
        );

        // =====================================================
        // Vererbungsbeispiel
        // =====================================================

        environment.define(
                "Student1",
                        new OclRelation(
                        List.of(student1)
                )
        );

        typeEnvironment.define(
                "Student1",
                CType.singletonOf(
                        new ClassType("Student")
                )
        );

        // =====================================================
        // 9. Mixed-CType-Beispiel für Merge
        // =====================================================

        // e1 = [0] als Bag(Integer) -> nicht unique
        environment.define(
                "e1",
                new OclRelation(
                        List.of(
                                new OclInteger(0)
                        )
                )
        );

        typeEnvironment.define(
                "e1",
                CType.bagOf(
                        PrimitiveType.INTEGER
                )
        );


        // e2 = [1] als Set(Integer) -> unique
        environment.define(
                "e2",
                new OclRelation(
                        List.of(
                                new OclInteger(1)
                        )
                )
        );

        typeEnvironment.define(
                "e2",
                CType.setOf(
                        PrimitiveType.INTEGER
                )
        );


        // e3 = [1] als Set(Integer) -> unique
        environment.define(
                "e3",
                new OclRelation(
                        List.of(
                                new OclInteger(1)
                        )
                )
        );

        typeEnvironment.define(
                "e3",
                CType.setOf(
                        PrimitiveType.INTEGER
                )
        );

        // =====================================================
        // 9. ObjectStore
        // =====================================================

        ObjectStore objectStore = new ObjectStore(modelTypeContext);

        objectStore.add(person1);
        objectStore.add(person2);
        objectStore.add(person3);
        objectStore.add(person4);

        objectStore.add(student1);


        // =====================================================
        // 10. Engine
        // =====================================================

        OclMinusEngine engine = new OclMinusEngine(
                environment,
                objectStore,
                typeEnvironment,
                modelTypeContext
        );


        // =====================================================
        // 11. Auswertungen
        // =====================================================

        System.out.println(
                "Person1.friends = "
                        + engine.evaluate("Person1.friends")
        );

        System.out.println(
                "Person1.friends.friends = "
                        + engine.evaluate("Person1.friends.friends")
        );

        System.out.println(
                "Iterator-Ergebnis = "
                        + engine.evaluate(
                                "Person1.friends "
                                + "▷ [p | acc ◁ no Person as Bag | "
                                + "acc ⊔ p.friends]"
                        )
        );


        // Any mit Treffer
        OclValue anyResult = engine.evaluate(
                "no int as Set ⊔ 1 ⊔ 4 ⊔ 5 "
                + "▷ [x | acc ◁ no int | "
                + "acc = no int and x > 3 ? x : acc]"
        );

        System.out.println(
                "any(x > 3) = " + anyResult
        );


        // Any ohne Treffer
        OclValue anyResult2 = engine.evaluate(
                "no int as Set ⊔ 1 ⊔ 4 ⊔ 5 "
                + "▷ [x | acc ◁ no int | "
                + "acc = no int and x > 10 ? x : acc]"
        );

        System.out.println(
                "any(x > 10) = " + anyResult2
        );


        // Semantische Gleichheit
        System.out.println(
                "sequence1 = sequence2: "
                        + engine.evaluate(
                                "sequence1 = sequence2"
                        )
        );

        System.out.println(
                "Verschachtelter Set-Merge = "
                        + engine.evaluate(
                                "outerSet1 ⊔ outerSet2"
                        )
        );

        System.out.println(
                "nestedSet1 = nestedSet2: "
                        + engine.evaluate(
                                "nestedSet1 = nestedSet2"
                        )
        );

        System.out.println(
                "nestedSequence1 = nestedSequence2: "
                        + engine.evaluate(
                                "nestedSequence1 = nestedSequence2"
                        )
        );

        System.out.println(
                "deepValue1 = deepValue2: "
                        + engine.evaluate(
                                "deepValue1 = deepValue2"
                        )
        );

        System.out.println(
                "Person.allInstances = "
                        + objectStore.allInstances(
                                "Person"
                        )
        );

        System.out.println(
        "Student1.friends = "
                + engine.evaluate("Student1.friends")
        );

        // =====================================================
        // Mixed-CType: fehlende Assoziativität von Merge
        // =====================================================

        OclValue mergeLeftAssociated = engine.evaluate(
                "(e1 ⊔ e2) ⊔ e3"
        );

        OclValue mergeRightAssociated = engine.evaluate(
                "e1 ⊔ (e2 ⊔ e3)"
        );

        System.out.println(
                "(e1 ⊔ e2) ⊔ e3 = "
                        + mergeLeftAssociated
        );

        System.out.println(
                "e1 ⊔ (e2 ⊔ e3) = "
                        + mergeRightAssociated
        );

        // =====================================================
        // Navigation-safe Conditional
        // =====================================================

        OclValue emptyConditionResult = engine.evaluate(
                "no bool ? 1 : 2"
        );

        System.out.println(
                "no bool ? 1 : 2 = "
                        + emptyConditionResult
        );

        OclRelation illTypedValue = new OclRelation(
            List.of(new OclInteger(1), new OclRelation(List.of())));
        
        ValueTypeChecker valueTypeChecker = new ValueTypeChecker(modelTypeContext);

        boolean result = valueTypeChecker.conformsTo(illTypedValue, CType.setOf(PrimitiveType.INTEGER));

        System.out.println("[1, []] as Set(Integer) = " + result);

        OclRelation wellTypedNestedValue = new OclRelation(List.of(new OclRelation(
            List.of(new OclRelation(List.of(new OclInteger(1))))), new OclRelation(List.of())));

        CType nestedIntegerType = CType.setOf(CType.setOf(CType.setOf(PrimitiveType.INTEGER)));

        boolean nestedResult = valueTypeChecker.conformsTo(wellTypedNestedValue, nestedIntegerType);

        System.out.println("[[[1]], []] well-typed = " + nestedResult);

        boolean environmentWellTyped = valueTypeChecker.isWellTyped(environment, typeEnvironment);

        System.out.println("Environment well-typed = " + environmentWellTyped);

        OclRelation emptyValue = new OclRelation(List.of());

        boolean emptyAsSingleton = valueTypeChecker.conformsTo(emptyValue, CType.singletonOf(PrimitiveType.INTEGER));

        System.out.println("[] as Singleton(Integer) = " + emptyAsSingleton);

        boolean emptyAsOption = valueTypeChecker.conformsTo(emptyValue, CType.optionOf(PrimitiveType.INTEGER));

        System.out.println("[] as Option(Integer) = " + emptyAsOption);

        OclRelation duplicateValue = new OclRelation(List.of(new OclInteger(1), new OclInteger(1)));

        System.out.println("[1,1] as Set(Integer) = " + valueTypeChecker.conformsTo(
            duplicateValue, CType.setOf(PrimitiveType.INTEGER)));

        System.out.println("[1,1] as Bag(Integer) = " + valueTypeChecker.conformsTo(
            duplicateValue, CType.bagOf(PrimitiveType.INTEGER)));

        OclRelation orderedTestValue = new OclRelation(List.of(new OclInteger(1), new OclInteger(2)));

        System.out.println("[1,2] as Sequence(Integer) = " + valueTypeChecker.conformsTo(
            orderedTestValue, CType.sequenceOf(PrimitiveType.INTEGER)));

        System.out.println("[1,2] as Bag(Integer) = " + valueTypeChecker.conformsTo(
            orderedTestValue, CType.bagOf(PrimitiveType.INTEGER)));

        System.out.println("[1,2] as OrderedSet(Integer) = " + valueTypeChecker.conformsTo(
            orderedTestValue, CType.orderedSetOf(PrimitiveType.INTEGER)));

        System.out.println("[1,2] as Set(Integer) = " + valueTypeChecker.conformsTo(
            orderedTestValue, CType.setOf(PrimitiveType.INTEGER)));

        OclRelation duplicateOrderedTestValue = new OclRelation(List.of(new OclInteger(1), new OclInteger(1)));

        System.out.println("[1,1] as Sequence(Integer) = " + valueTypeChecker.conformsTo(
            duplicateOrderedTestValue, CType.sequenceOf(PrimitiveType.INTEGER)) );

        System.out.println("[1,1] as Bag(Integer) = " + valueTypeChecker.conformsTo(
            duplicateOrderedTestValue, CType.bagOf(PrimitiveType.INTEGER)));

        System.out.println("[1,1] as OrderedSet(Integer) = " + valueTypeChecker.conformsTo(
            duplicateOrderedTestValue, CType.orderedSetOf(PrimitiveType.INTEGER)));

        System.out.println("[1,1] as Set(Integer) = " + valueTypeChecker.conformsTo(
            duplicateOrderedTestValue, CType.setOf(PrimitiveType.INTEGER)));
    }
}