package oclminus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import oclminus.runtime.OclObject;
import oclminus.runtime.ObjectStore;
import oclminus.runtime.OclRelation;
import org.junit.jupiter.api.Test;

final class ObjectStoreTest {

    @Test
    void returnsAllInstancesOfRequestedClass() {
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

        assertEquals(
                new OclRelation(
                        List.of(alice, bob)
                ),
                objectStore.allInstances("Person")
        );
    }

    @Test
    void returnsEmptyRelationWhenClassHasNoInstances() {
        ObjectStore objectStore =
                new ObjectStore();

        assertEquals(
                new OclRelation(List.of()),
                objectStore.allInstances("Person")
        );
    }
}