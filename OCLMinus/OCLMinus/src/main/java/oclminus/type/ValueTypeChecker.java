package oclminus.type;

import java.util.List;
import java.util.Objects;

import oclminus.runtime.Environment;
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
     * Prüft, ob ein konkreter OCL-Minus-Wert zu einem CType passt.
     *
     * Entspricht konzeptionell der Relation:
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

        // OCL-Minus-Werte werden relational dargestellt.
        if (!(value instanceof OclRelation relation)) {
            return false;
        }

        int size = relation.elements().size();

        /*
         * 1. Multiplizität
         *
         * Die Anzahl der Elemente muss innerhalb
         * der Grenzen des CTypes liegen:
         *
         * lowerBound <= size <= upperBound
         */
        if (!hasValidMultiplicity(size, type)) {
            return false;
        }

        /*
         * 2. Ordnung
         *
         * Werte sind als Listen repräsentiert und können
         * deshalb sowohl ordered als auch unordered CTypes
         * erfüllen.
         *
         * ordered == null ist jedoch nur für Werte mit
         * höchstens einem Element zulässig.
         */
        if (!hasValidOrdering(size, type)) {
            return false;
        }

        /*
         * 3. MemberType
         *
         * Jedes Element muss zum MemberType des CTypes
         * passen.
         *
         * Bei [] ist die Schleife leer. Dadurch kann die
         * leere Relation jeden passenden MemberType erfüllen.
         */
        if (!allElementsConform(
                relation,
                type.memberType()
        )) {
            return false;
        }

        /*
         * 4. Uniqueness
         *
         * Falls der CType unique ist, dürfen keine zwei
         * Elemente semantisch gleich sein.
         */
        if (!hasValidUniqueness(
                relation,
                type
        )) {
            return false;
        }

        return true;
    }

    /**
     * Prüft, ob ein kompletter Variablenspeicher zu
     * seinem TypeEnvironment passt.
     *
     * Entspricht konzeptionell:
     *
     * M0 ; Γ ⊢ σ
     */
    public boolean isWellTyped(
            Environment environment,
            TypeEnvironment typeEnvironment
    ) {
        Objects.requireNonNull(
                environment,
                "Environment darf nicht null sein."
        );

        Objects.requireNonNull(
                typeEnvironment,
                "TypeEnvironment darf nicht null sein."
        );

        /*
         * Beide Umgebungen müssen dieselben
         * Variablennamen enthalten.
         */
        if (!environment.variableNames().equals(
                typeEnvironment.variableNames()
        )) {
            return false;
        }

        /*
         * Für jede Variable muss der konkrete Runtime-Wert
         * zum statisch gespeicherten CType passen.
         */
        for (String variableName
                : environment.variableNames()) {

            OclValue value =
                    environment.lookup(variableName);

            CType type =
                    typeEnvironment.lookup(variableName);

            if (!conformsTo(value, type)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Prüft die Kardinalitätsgrenzen des CTypes.
     */
    private boolean hasValidMultiplicity(
            int size,
            CType type
    ) {
        if (size < type.lowerBound()) {
            return false;
        }

        if (type.upperBound() != null
                && size > type.upperBound()) {
            return false;
        }

        return true;
    }

    /**
     * Prüft die für die Werttypisierung zulässige
     * Ordered-Eigenschaft.
     */
    private boolean hasValidOrdering(
            int size,
            CType type
    ) {
        return size <= 1
                || type.ordered() != null;
    }

    /**
     * Prüft, ob jedes Element der Relation
     * zum erwarteten MemberType passt.
     */
    private boolean allElementsConform(
            OclRelation relation,
            MemberType memberType
    ) {
        for (OclValue element
                : relation.elements()) {

            if (!elementConformsTo(
                    element,
                    memberType
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
         * Das Element muss selbst wieder eine Relation sein,
         * die zum inneren CType passt.
         */
        if (expectedType instanceof CType nestedType) {
            return conformsTo(
                    element,
                    nestedType
            );
        }

        /*
         * Primitive Typen.
         */
        if (expectedType == PrimitiveType.INTEGER) {
            return element instanceof OclInteger;
        }

        if (expectedType == PrimitiveType.BOOLEAN) {
            return element instanceof OclBoolean;
        }

        /*
         * Klassen inklusive Vererbung.
         *
         * Ein Student kann beispielsweise zu Person passen,
         * falls Student eine Unterklasse von Person ist.
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

    /**
     * Prüft die Unique-Eigenschaft eines CTypes.
     */
    private boolean hasValidUniqueness(
            OclRelation relation,
            CType type
    ) {
        if (!Boolean.TRUE.equals(type.unique())) {
            return true;
        }

        return elementsAreUnique(
                relation,
                type.memberType()
        );
    }

    /**
     * Prüft, ob alle Elemente semantisch verschieden sind.
     */
    private boolean elementsAreUnique(
            OclRelation relation,
            MemberType memberType
    ) {
        List<OclValue> elements =
                relation.elements();

        for (int i = 0;
                i < elements.size();
                i++) {

            for (int j = i + 1;
                    j < elements.size();
                    j++) {

                if (semanticallyEqual(
                        elements.get(i),
                        elements.get(j),
                        memberType
                )) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Vergleicht zwei Elemente abhängig von ihrem
     * semantischen MemberType.
     */
    private boolean semanticallyEqual(
            OclValue left,
            OclValue right,
            MemberType memberType
    ) {

        /*
         * Bei verschachtelten Collections muss deren
         * Collection-Semantik berücksichtigt werden.
         */
        if (memberType instanceof CType nestedType) {

            if (!(left instanceof OclRelation leftRelation)
                    || !(right instanceof OclRelation rightRelation)) {
                return false;
            }

            return relationsSemanticallyEqual(
                    leftRelation,
                    rightRelation,
                    nestedType
            );
        }

        /*
         * Primitive Werte und Objekte.
         */
        return left.equals(right);
    }

    /**
     * Vergleicht zwei Relationen entsprechend der
     * Ordered-Eigenschaft ihres CTypes.
     */
    private boolean relationsSemanticallyEqual(
            OclRelation left,
            OclRelation right,
            CType type
    ) {

        if (left.elements().size()
                != right.elements().size()) {
            return false;
        }

        /*
         * Ordered:
         * Die Position der Elemente ist relevant.
         */
        if (Boolean.TRUE.equals(type.ordered())) {

            for (int i = 0;
                    i < left.elements().size();
                    i++) {

                if (!semanticallyEqual(
                        left.elements().get(i),
                        right.elements().get(i),
                        type.memberType()
                )) {
                    return false;
                }
            }

            return true;
        }

        /*
         * Unordered:
         * Die Reihenfolge der Elemente ist irrelevant.
         */
        boolean[] matched =
                new boolean[right.elements().size()];

        for (OclValue leftElement
                : left.elements()) {

            boolean found = false;

            for (int i = 0;
                    i < right.elements().size();
                    i++) {

                if (!matched[i]
                        && semanticallyEqual(
                                leftElement,
                                right.elements().get(i),
                                type.memberType()
                        )) {

                    matched[i] = true;
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }
}