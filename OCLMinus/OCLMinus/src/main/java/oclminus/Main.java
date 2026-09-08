package oclminus;

import java.util.List;
import java.util.Map;

import oclminus.runtime.Environment;
import oclminus.runtime.ObjectStore;
import oclminus.runtime.OclObject;
import oclminus.runtime.OclRelation;
import oclminus.runtime.OclValue;

import oclminus.type.CType;
import oclminus.type.ClassType;
import oclminus.type.ModelTypeContext;
import oclminus.type.TypeEnvironment;

public class Main {

    public static void main(String[] args) {

        // =====================================================
        // 1. Modellobjekte erstellen
        // =====================================================

        // Person4 hat keine Freunde.
        OclObject person4 = new OclObject(
                "p4",
                "Person",
                Map.of(
                        "friends",
                        new OclRelation(List.of())
                )
        );

        // Person3 hat keine Freunde.
        OclObject person3 = new OclObject(
                "p3",
                "Person",
                Map.of(
                        "friends",
                        new OclRelation(List.of())
                )
        );

        // Person2 ist mit Person4 befreundet.
        OclObject person2 = new OclObject(
                "p2",
                "Person",
                Map.of(
                        "friends",
                        new OclRelation(
                                List.of(person4)
                        )
                )
        );

        // Person1 ist mit Person2 und Person3 befreundet.
        OclObject person1 = new OclObject(
                "p1",
                "Person",
                Map.of(
                        "friends",
                        new OclRelation(
                                List.of(
                                        person2,
                                        person3
                                )
                        )
                )
        );


        // =====================================================
        // 2. Runtime-Environment
        // =====================================================

        Environment environment = new Environment();

        // Die Variable "Person1" liefert relational {Person1}.
        environment.define(
                "Person1",
                new OclRelation(
                        List.of(person1)
                )
        );


        // =====================================================
        // 3. ObjectStore
        // =====================================================

        ObjectStore objectStore = new ObjectStore();

        objectStore.add(person1);
        objectStore.add(person2);
        objectStore.add(person3);
        objectStore.add(person4);


        // =====================================================
        // 4. TypeEnvironment
        // =====================================================

        TypeEnvironment typeEnvironment = new TypeEnvironment();

        // Person1 hat genau einen Wert vom Typ Person.
        typeEnvironment.define(
                "Person1",
                CType.singletonOf(
                        new ClassType("Person")
                )
        );


        // =====================================================
        // 5. ModelTypeContext
        // =====================================================

        ModelTypeContext modelTypeContext = new ModelTypeContext();

        // Person.friends ist eine Menge von Personen.
        modelTypeContext.defineProperty(
                "Person",
                "friends",
                CType.setOf(
                        new ClassType("Person")
                )
        );


        // =====================================================
        // 6. Engine
        // =====================================================

        OclMinusEngine engine =
                new OclMinusEngine(
                        environment,
                        objectStore,
                        typeEnvironment,
                        modelTypeContext
                );


        // =====================================================
        // 7. Ganze Engine testen
        // =====================================================

        OclValue result1 = engine.evaluate("Person1.friends");

        System.out.println("Person1.friends = " + result1);

        OclValue result2 = engine.evaluate("Person1.friends.friends");

        System.out.println("Person1.friends.friends = " + result2);

        OclValue iteratorResult = engine.evaluate("Person1.friends ▷ [p | acc ◁ no Person as Bag | acc ⊔ p.friends]");

        System.out.println("Iterator-Ergebnis = " + iteratorResult);

        // =====================================================
        // 8. any mit iterate nachbilden
        // =====================================================

        OclValue anyResult = engine.evaluate(
                "no int as Set ⊔ 1 ⊔ 4 ⊔ 5 "
                + "▷ [x | acc ◁ no int | "
                + "acc = no int and x > 3 ? x : acc]"
        );

        System.out.println(
                "any(x > 3) = " + anyResult
        );

        OclValue anyResult2 = engine.evaluate(
            "no int as Set ⊔ 1 ⊔ 4 ⊔ 5 "
            + "▷ [x | acc ◁ no int | "
            + "acc = no int and x > 10 ? x : acc]"
        );

        System.out.println("any(x > 10) = " + anyResult2);
    }
}