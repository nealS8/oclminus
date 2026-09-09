package oclminus.type;

import java.util.Objects;

import oclminus.runtime.OclBoolean;
import oclminus.runtime.OclInteger;
import oclminus.runtime.OclObject;
import oclminus.runtime.OclRelation;
import oclminus.runtime.OclValue;

public final class ValueTypeChecker {

    private final ModelTypeContext modelTypeContext;

    public ValueTypeChecker(
            ModelTypeContext modelTypeContext
    ) {
        this.modelTypeContext = Objects.requireNonNull(
                modelTypeContext,
                "ModelTypeContext darf nicht null sein."
        );
    }

    /**
     * Prüft, ob ein konkreter OCL-Wert zu einem CType passt.
     *
     * Entspricht konzeptionell:
     *
     * M0 ⊢ v : χ
     */
    public boolean conformsTo(
            OclValue value,
            CType type
    ) {
        Objects.requireNonNull(
                value,
                "Wert darf nicht null sein."
        );

        Objects.requireNonNull(
                type,
                "CType darf nicht null sein."
        );

        // Ein OCL-Minus-Wert ist relational.
        if (!(value instanceof OclRelation relation)) {
            return false;
        }

        /*
         * Sonderfall []:
         *
         * Eine leere Relation enthält kein Element,
         * das dem erwarteten MemberType widersprechen könnte.
         *
         * Deshalb passt [] bezüglich des MemberTypes
         * zu jedem CType.
         */
        if (relation.elements().isEmpty()) {
            return true;
        }

        /*
         * Jedes Element der Relation muss zum
         * MemberType des CTypes passen.
         */
        for (OclValue element : relation.elements()) {

            if (!elementConformsTo(
                    element,
                    type.memberType()
            )) {
                return false;
            }
        }

        return true;
    }

    /**
     * Prüft ein einzelnes Element gegen einen MemberType.
     */
    private boolean elementConformsTo(
            OclValue element,
            MemberType expectedType
    ) {

        /*
         * Verschachtelter CType:
         *
         * Beispiel:
         *
         * Set(Set(Integer))
         *
         * Das Element der äußeren Relation muss also
         * selbst wieder eine Relation sein, die zum
         * inneren CType passt.
         */
        if (expectedType instanceof CType nestedType) {

            return conformsTo(
                    element,
                    nestedType
            );
        }

        /*
         * Primitive MemberTypes
         */
        if (expectedType == PrimitiveType.INTEGER) {
            return element instanceof OclInteger;
        }

        if (expectedType == PrimitiveType.BOOLEAN) {
            return element instanceof OclBoolean;
        }

        /*
         * Klassen:
         *
         * Auch Unterklassen sind erlaubt.
         *
         * Student kann also beispielsweise
         * zu einem erwarteten Person-Typ passen.
         */
        if (expectedType instanceof ClassType classType) {

            if (!(element instanceof OclObject object)) {
                return false;
            }

            return modelTypeContext.isSameOrSubclass(
                    object.className(),
                    classType.className()
            );
        }

        return false;
    }
}