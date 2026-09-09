package oclminus.runtime;

import oclminus.ast.BinaryExpression;
import oclminus.ast.BinaryOperator;
import oclminus.ast.BooleanLiteral;
import oclminus.ast.CoercionExpression;
import oclminus.ast.Expression;
import oclminus.ast.IntegerLiteral;
import oclminus.ast.PropertyAccessExpression;
import oclminus.ast.VariableExpression;
import oclminus.type.CType;
import oclminus.type.TypeChecker;
import oclminus.type.TypeEnvironment;
import oclminus.ast.AllInstancesExpression;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import oclminus.type.MemberType;
import oclminus.ast.UnaryExpression;
import oclminus.ast.UnaryOperator;
import oclminus.ast.NoExpression;
import oclminus.ast.LiftExpression;
import oclminus.ast.LowerExpression;
import oclminus.type.TypeChecker;
import oclminus.ast.IterationExpression;
import oclminus.ast.ConditionalExpression;

public final class Interpreter {

    private final Environment environment;
    private final ObjectStore objectStore;
    private final TypeChecker typeChecker;

    public Interpreter() {
        this(
            new Environment(),
            new ObjectStore(),
            new TypeChecker()
        );
    }

    public Interpreter(Environment environment) {
        this(
            environment,
            new ObjectStore(),
            new TypeChecker()
        );
    }

    public Interpreter(
        Environment environment,
        ObjectStore objectStore
    ) {
        this(
            environment,
            objectStore,
            new TypeChecker()
        );
    }

    public Interpreter(
        Environment environment,
        ObjectStore objectStore,
        TypeChecker typeChecker
    ) {
        this.environment = Objects.requireNonNull(
            environment,
            "Environment darf nicht null sein."
        );

        this.objectStore = Objects.requireNonNull(
            objectStore,
            "ObjectStore darf nicht null sein."
        );

        this.typeChecker = Objects.requireNonNull(
            typeChecker,
            "TypeChecker darf nicht null sein."
        );
    }

    public OclValue evaluate(Expression expression) {

        if (expression == null) {
            throw new IllegalArgumentException("Expression darf nicht null sein.");
        }

        if (expression instanceof IntegerLiteral integerLiteral) {
            return new OclRelation(
                java.util.List.of(new OclInteger(integerLiteral.value()))
            );
        }

        if (expression instanceof PropertyAccessExpression propertyAccessExpression) {
                return evaluatePropertyAccess(propertyAccessExpression);
        }

        if (expression instanceof BooleanLiteral booleanLiteral) {
            return new OclRelation(
                java.util.List.of(
                    new OclBoolean(booleanLiteral.value())
                )
            );
        }

        if (expression instanceof VariableExpression variableExpression) {
            return environment.lookup(variableExpression.name());
        }

        if (expression instanceof AllInstancesExpression allInstancesExpression) {
                return objectStore.allInstances(
                        allInstancesExpression.className()
                );
        }

        if (expression instanceof UnaryExpression unaryExpression) {
            return evaluateUnaryExpression(unaryExpression);
        }

        if (expression instanceof BinaryExpression binaryExpression) {
            return evaluateBinaryExpression(binaryExpression);
        }

        if (expression instanceof NoExpression) {
                return new OclRelation(List.of());
        }

        if (expression instanceof LiftExpression liftExpression) {
                return evaluateLift(liftExpression);
        }

        if (expression instanceof LowerExpression lowerExpression) {
                return evaluateLower(lowerExpression);
        }

        if (expression instanceof CoercionExpression coercionExpression) {
                return evaluateCoercion(coercionExpression);
        }

        if (expression instanceof IterationExpression iterationExpression) {
                 return evaluateIteration(iterationExpression);
        }

        if (expression instanceof ConditionalExpression conditionalExpression) {
            return evaluateConditional(conditionalExpression);
        }

        throw new IllegalStateException(
                "Unbekannter Ausdruck: "
                        + expression.getClass().getSimpleName()
        );
    }

    private OclValue evaluateBinaryExpression(BinaryExpression expression) {
        
        OclValue leftValue = evaluate(expression.left());
        OclValue rightValue = evaluate(expression.right());

        return switch (expression.operator()) {
            case PLUS ->
                    add(leftValue, rightValue);

            case MINUS ->
                    subtract(leftValue, rightValue);

            case MULTIPLY ->
                    multiply(leftValue, rightValue);

            case DIVIDE ->
                    divide(leftValue, rightValue);

            case EQUAL ->
                equal(expression, leftValue, rightValue);

            case NOT_EQUAL -> 
                notEqual(expression, leftValue, rightValue);

            case LESS_THAN ->
                    lessThan(leftValue, rightValue);

            case LESS_THAN_OR_EQUAL ->
                    lessThanOrEqual(leftValue, rightValue);

            case GREATER_THAN ->
                    greaterThan(leftValue, rightValue);

            case GREATER_THAN_OR_EQUAL ->
                    greaterThanOrEqual(leftValue, rightValue);

            case AND ->
                    and(leftValue, rightValue);

            case OR ->
                    or(leftValue, rightValue);

            case XOR ->
                    xor(leftValue, rightValue);

            case IMPLIES ->
                    implies(leftValue, rightValue);

            case MERGE ->
                merge(
                    expression,
                    leftValue,
                    rightValue
                );
        };
    }

    private OclValue add(
        OclValue leftValue,
        OclValue rightValue
) {
    OclInteger leftInteger =
            requireSingleInteger(
                    leftValue,
                    BinaryOperator.PLUS
            );

    OclInteger rightInteger =
            requireSingleInteger(
                    rightValue,
                    BinaryOperator.PLUS
            );

    OclInteger result = new OclInteger(
            leftInteger.value()
                    + rightInteger.value()
    );

    return new OclRelation(
            List.of(result)
    );
}

    private OclValue multiply(
            OclValue leftValue,
            OclValue rightValue
    ) {
        OclInteger leftInteger =
                requireSingleInteger(
                        leftValue,
                        BinaryOperator.MULTIPLY
                );

        OclInteger rightInteger =
                requireSingleInteger(
                        rightValue,
                        BinaryOperator.MULTIPLY
                );

        OclInteger result = new OclInteger(
                leftInteger.value()
                        * rightInteger.value()
        );

        return new OclRelation(
                List.of(result)
        );
    }

    // Bestimmt die CTypes beider Operanden und prüft anschließend deren semantische Gleichheit
    private OclValue equal(BinaryExpression expression, OclValue leftValue, OclValue rightValue) {
        
        if (!(leftValue instanceof OclRelation leftRelation)
            || !(rightValue instanceof OclRelation rightRelation)) {
                throw new IllegalStateException("Gleichheit erwartet Relationen.");
                }

        CType leftType = typeChecker.determineType(expression.left());

        CType rightType = typeChecker.determineType(expression.right());

        boolean result = semanticEquals(leftRelation, leftType, rightRelation, rightType);

        return new OclRelation(List.of(new OclBoolean(result)));
    }

    // Prüft die semantische Ungleichheit der beiden Operanden als Negation der semantischen Gleichheit
    private OclValue notEqual(BinaryExpression expression, OclValue leftValue, OclValue rightValue) {
        
        if (!(leftValue instanceof OclRelation leftRelation)
            || !(rightValue instanceof OclRelation rightRelation)) {
                throw new IllegalStateException("Ungleichheit erwartet Relationen.");
                }

        CType leftType = typeChecker.determineType(expression.left());

        CType rightType = typeChecker.determineType(expression.right());

        boolean result = !semanticEquals(leftRelation, leftType, rightRelation, rightType);

        return new OclRelation(List.of(new OclBoolean(result)));
    }

    // Vergleicht zwei Relationen rekursiv gemäß ihrer CTypes.
    // Bei geordneten Collections wird die Reihenfolge berücksichtigt,
    // bei ungeordneten Collections wird sie ignoriert.
    private boolean semanticEquals(OclRelation leftRelation, CType leftType, OclRelation rightRelation, CType rightType) {
        
        if (leftRelation.elements().size() != rightRelation.elements().size()) {
            return false;
        }

        // Singleton und Option besitzen keinen CollectionKind.
        // Da sie höchstens ein Element enthalten, wird dieses
        // rekursiv anhand seines Member-Typs verglichen.
        if (!leftType.isCollectionKind()) {
            return orderedEquals(
                leftRelation,
                leftType.memberType(),
                rightRelation,
                rightType.memberType()
            );
        }

        // OrderedSet und Sequence:
        // Elemente müssen an denselben Positionen semantisch gleich sein.
        if (leftType.collectionKind().isOrdered()) {
            return orderedEquals(
                leftRelation,
                leftType.memberType(),
                rightRelation,
                rightType.memberType()
            );
        }

        // Set und Bag:
        // Reihenfolge ist irrelevant.
        return unorderedEquals(leftRelation, leftType.memberType(), rightRelation, rightType.memberType());
    }

    // Vergleicht zwei ungeordnete Relationen semantisch.
    // Die Reihenfolge wird ignoriert, Multiplizitäten bleiben erhalten.
    private boolean unorderedEquals(OclRelation leftRelation, MemberType leftMemberType, OclRelation rightRelation, MemberType rightMemberType) {
        if (leftRelation.elements().size()
            != rightRelation.elements().size()) {
                return false;
            }

        List<OclValue> remaining = new ArrayList<>(rightRelation.elements());

        for (OclValue leftElement : leftRelation.elements()) {

            int matchingIndex = -1;

            for (int i = 0; i < remaining.size(); i++) {

                OclValue rightElement = remaining.get(i);

                if (elementsSemanticallyEqual(leftElement, leftMemberType, rightElement, rightMemberType)) {
                    matchingIndex = i;
                    break;
                }
            }

            if (matchingIndex < 0) {
                return false;
            }

            remaining.remove(matchingIndex);
        }

            return remaining.isEmpty();
    }

    // Vergleicht zwei Relationen elementweise unter Berücksichtigung
    // der Reihenfolge.
    private boolean orderedEquals(
        OclRelation leftRelation,
        MemberType leftMemberType,
        OclRelation rightRelation,
        MemberType rightMemberType
    ) {
        if (leftRelation.elements().size()
            != rightRelation.elements().size()) {
            return false;
        }

        for (int i = 0; i < leftRelation.elements().size(); i++) {

            OclValue leftElement = leftRelation.elements().get(i);

            OclValue rightElement = rightRelation.elements().get(i);

            if (!elementsSemanticallyEqual(leftElement, leftMemberType, rightElement, rightMemberType)) {
                return false;
            }
        }

        return true;
    }

    // Vergleicht zwei einzelne Elemente anhand ihres Member-Typs.
    // Sind die Elemente selbst Relationen, wird rekursiv weiterverglichen
    private boolean elementsSemanticallyEqual(
        OclValue leftElement,
        MemberType leftMemberType,
        OclValue rightElement,
        MemberType rightMemberType
    ) {
        if (leftElement instanceof OclRelation leftRelation
            && rightElement instanceof OclRelation rightRelation
            && leftMemberType instanceof CType leftCType
            && rightMemberType instanceof CType rightCType) {

            return semanticEquals(
                leftRelation,
                leftCType,
                rightRelation,
                rightCType
            );
        }

        return leftElement.equals(rightElement);
    }

    private OclValue evaluateUnaryExpression(
        UnaryExpression expression
    ) {
        OclValue operandValue = evaluate(expression.operand());

        return switch (expression.operator()) {
            case NOT -> not(operandValue);
            case NEGATE -> negate(operandValue);
        };
    }

    private OclValue negate(OclValue operandValue) {
        OclInteger integer =
                requireSingleInteger(
                        operandValue,
                        UnaryOperator.NEGATE
                );

        OclInteger result =
                new OclInteger(-integer.value());

        return new OclRelation(
                List.of(result)
        );
    }

    private OclValue not(OclValue operandValue) {
        OclBoolean booleanValue =
                requireSingleBoolean(
                        operandValue,
                        UnaryOperator.NOT
                );

        OclBoolean result =
                new OclBoolean(!booleanValue.value());

        return new OclRelation(
                List.of(result)
        );
    }

    private OclValue subtract(
        OclValue leftValue,
        OclValue rightValue
    ) {
        OclInteger leftInteger =
                requireSingleInteger(
                        leftValue,
                        BinaryOperator.MINUS
                );

        OclInteger rightInteger =
                requireSingleInteger(
                        rightValue,
                        BinaryOperator.MINUS
                );

        OclInteger result = new OclInteger(
                leftInteger.value()
                        - rightInteger.value()
        );

        return new OclRelation(
                List.of(result)
        );
    }

    private OclValue divide(
        OclValue leftValue,
        OclValue rightValue
    ) {
        OclInteger leftInteger =
                requireSingleInteger(
                        leftValue,
                        BinaryOperator.DIVIDE
                );

        OclInteger rightInteger =
                requireSingleInteger(
                        rightValue,
                        BinaryOperator.DIVIDE
                );

        if (rightInteger.value() == 0) {
            throw new ArithmeticException(
                    "Division durch null ist nicht erlaubt."
            );
        }

        OclInteger result = new OclInteger(
                leftInteger.value()
                        / rightInteger.value()
        );

        return new OclRelation(
                List.of(result)
        );
    }

    private OclValue lessThan(
        OclValue leftValue,
        OclValue rightValue
    ) {
        OclInteger leftInteger =
                requireSingleInteger(
                        leftValue,
                        BinaryOperator.LESS_THAN
                );

        OclInteger rightInteger =
                requireSingleInteger(
                        rightValue,
                        BinaryOperator.LESS_THAN
                );

        return new OclRelation(
                List.of(
                        new OclBoolean(
                                leftInteger.value()
                                        < rightInteger.value()
                        )
                )
        );
    }

    private OclValue lessThanOrEqual(
        OclValue leftValue,
        OclValue rightValue
    ) {
        OclInteger leftInteger =
                requireSingleInteger(
                        leftValue,
                        BinaryOperator.LESS_THAN_OR_EQUAL
                );

        OclInteger rightInteger =
                requireSingleInteger(
                        rightValue,
                        BinaryOperator.LESS_THAN_OR_EQUAL
                );

        return new OclRelation(
                List.of(
                        new OclBoolean(
                                leftInteger.value()
                                        <= rightInteger.value()
                        )
                )
        );
    }

    private OclValue greaterThan(
        OclValue leftValue,
        OclValue rightValue
    ) {
        OclInteger leftInteger =
                requireSingleInteger(
                        leftValue,
                        BinaryOperator.GREATER_THAN
                );

        OclInteger rightInteger =
                requireSingleInteger(
                        rightValue,
                        BinaryOperator.GREATER_THAN
                );

        return new OclRelation(
                List.of(
                        new OclBoolean(
                                leftInteger.value()
                                        > rightInteger.value()
                        )
                )
        );
    }

    private OclValue greaterThanOrEqual(
        OclValue leftValue,
        OclValue rightValue
    ) {
        OclInteger leftInteger =
                requireSingleInteger(
                        leftValue,
                        BinaryOperator.GREATER_THAN_OR_EQUAL
                );

        OclInteger rightInteger =
                requireSingleInteger(
                        rightValue,
                        BinaryOperator.GREATER_THAN_OR_EQUAL
                );

        return new OclRelation(
                List.of(
                        new OclBoolean(
                                leftInteger.value()
                                        >= rightInteger.value()
                        )
                )
        );
    }

    private OclValue evaluatePropertyAccess(PropertyAccessExpression expression) {
        OclValue target = evaluate(expression.target());

        if (!(target instanceof OclRelation relation)) {
            throw new IllegalStateException("Property Access erwartet eine Relation.");
        }

        return accessProperty(relation, expression.propertyName());
    }

    private OclRelation accessProperty(OclRelation relation, String propertyName) {
        List<OclValue> result = new ArrayList<>();

        for (OclValue value : relation.elements()) {

            if (!(value instanceof OclObject object)) {
                throw new IllegalStateException("Property Access kann nur auf Objekten erfolgen.");
            }

            OclRelation property = object.property(propertyName);

            result.addAll(property.elements());
        }

        return new OclRelation(result);
    }

    private OclBoolean requireSingleBoolean(
        OclValue value,
        BinaryOperator operator
    ) {
        OclValue element =
                requireSingleElement(value, operator);

        if (!(element instanceof OclBoolean booleanValue)) {
            throw new IllegalStateException(
                    "Der Operator '"
                            + operator
                            + "' erwartet einen Boolean."
            );
        }

        return booleanValue;
    }

    private OclValue and(
        OclValue leftValue,
        OclValue rightValue
    ) {
        OclBoolean leftBoolean =
                requireSingleBoolean(
                        leftValue,
                        BinaryOperator.AND
                );

        OclBoolean rightBoolean =
                requireSingleBoolean(
                        rightValue,
                        BinaryOperator.AND
                );

        return new OclRelation(
                List.of(
                        new OclBoolean(
                                leftBoolean.value()
                                        && rightBoolean.value()
                        )
                )
        );
    }

    private OclValue or(
        OclValue leftValue,
        OclValue rightValue
    ) {
        OclBoolean leftBoolean =
                requireSingleBoolean(
                        leftValue,
                        BinaryOperator.OR
                );

        OclBoolean rightBoolean =
                requireSingleBoolean(
                        rightValue,
                        BinaryOperator.OR
                );

        return new OclRelation(
                List.of(
                        new OclBoolean(
                                leftBoolean.value()
                                        || rightBoolean.value()
                        )
                )
        );
    }

    private OclValue xor(
        OclValue leftValue,
        OclValue rightValue
    ) {
        OclBoolean leftBoolean =
                requireSingleBoolean(
                        leftValue,
                        BinaryOperator.XOR
                );

        OclBoolean rightBoolean =
                requireSingleBoolean(
                        rightValue,
                        BinaryOperator.XOR
                );

        return new OclRelation(
                List.of(
                        new OclBoolean(
                                leftBoolean.value()
                                        ^ rightBoolean.value()
                        )
                )
        );
    }

    private OclValue implies(
        OclValue leftValue,
        OclValue rightValue
    ) {
        OclBoolean leftBoolean =
                requireSingleBoolean(
                        leftValue,
                        BinaryOperator.IMPLIES
                );

        OclBoolean rightBoolean =
                requireSingleBoolean(
                        rightValue,
                        BinaryOperator.IMPLIES
                );

        boolean result =
                !leftBoolean.value()
                        || rightBoolean.value();

        return new OclRelation(
                List.of(new OclBoolean(result))
        );
    }

    private OclBoolean requireSingleBoolean(
        OclValue value,
        UnaryOperator operator
    ) {
        OclValue element =
                requireSingleElement(value, operator);

        if (!(element instanceof OclBoolean booleanValue)) {
            throw new IllegalStateException(
                    "Der Operator '"
                            + operator
                            + "' erwartet einen Boolean."
            );
        }

        return booleanValue;
    }

    private OclInteger requireSingleInteger(
        OclValue value,
        UnaryOperator operator
    ) {
        OclValue element =
                requireSingleElement(value, operator);

        if (!(element instanceof OclInteger integer)) {
            throw new IllegalStateException(
                    "Der Operator '"
                            + operator
                            + "' erwartet einen Integer."
            );
        }

        return integer;
    }

    private OclInteger requireSingleInteger(
        OclValue value,
        BinaryOperator operator
    ) {
        OclValue element =
                requireSingleElement(value, operator);

        if (!(element instanceof OclInteger integer)) {
            throw new IllegalStateException(
                    "Der Operator '"
                            + operator
                            + "' erwartet einen Integer."
            );
        }

        return integer;
    }

    private OclValue requireSingleElement(
        OclValue value,
        UnaryOperator operator
    ) {
        if (!(value instanceof OclRelation relation)) {
            throw new IllegalStateException(
                    "Der Operator '"
                            + operator
                            + "' erwartet eine Relation."
            );
        }

        if (relation.elements().size() != 1) {
            throw new IllegalStateException(
                    "Der Operator '"
                            + operator
                            + "' erwartet genau einen Wert."
            );
        }

        return relation.elements().get(0);
    }

    private OclValue requireSingleElement(
        OclValue value,
        BinaryOperator operator
    ) {
        if (!(value instanceof OclRelation relation)) {
            throw new IllegalStateException(
                    "Der Operator '"
                            + operator
                            + "' erwartet eine Relation."
            );
        }

        if (relation.elements().size() != 1) {
            throw new IllegalStateException(
                    "Der Operator '"
                            + operator
                            + "' erwartet genau einen Wert."
            );
        }

        return relation.elements().get(0);
    }

    private OclRelation evaluateLower(LowerExpression expression) {
        OclValue value = evaluate(expression.operand());

        if (!(value instanceof OclRelation outerRelation)) {
                throw new IllegalStateException(
                        "Lower erwartet eine Relation."
                );
        }

        if (outerRelation.elements().size() != 1) {
                throw new IllegalStateException(
                        "Lower erwartet eine Singleton-Relation."
                );
        }

        OclValue innerValue =
                outerRelation.elements().get(0);

        if (!(innerValue instanceof OclRelation innerRelation)) {
                throw new IllegalStateException(
                        "Lower erwartet eine Relation, "
                                + "die eine Relation enthält."
                );
        }

        return innerRelation;
        }

        private OclRelation evaluateLift(LiftExpression expression) {
                OclValue value = evaluate(expression.operand());

                return new OclRelation(
                        List.of(value)
                );
        }
    
    // Bei unique Collections werden semantisch gleiche Elemente nicht erneut hinzugefügt
    private OclRelation merge(BinaryExpression expression, OclValue leftValue, OclValue rightValue) {
        
        if (!(leftValue instanceof OclRelation leftRelation)) {
            throw new IllegalStateException("Merge erwartet links eine Relation.");
        }

        if (!(rightValue instanceof OclRelation rightRelation)) {throw new IllegalStateException("Merge erwartet rechts eine Relation.");
        }

        CType leftType = typeChecker.determineType(expression.left());

        if (!leftType.isCollectionKind()) {
            throw new IllegalStateException("Der linke Operand von Merge besitzt keinen Collection-Kind.");
        }

        List<OclValue> result = new ArrayList<>(leftRelation.elements());

        if (!leftType.collectionKind().isUnique()) {

            result.addAll(rightRelation.elements());

            return new OclRelation(result);
        }

        for (OclValue rightElement : rightRelation.elements()) {

            if (!containsSemantically(result, rightElement, leftType.memberType())) {
                result.add(rightElement);
            }
        }

        return new OclRelation(result);
        }

    // Prüft, ob bereits ein semantisch gleiches Element
    // in der Liste enthalten ist.
    private boolean containsSemantically(List<OclValue> values, OclValue candidate, MemberType memberType) {
        for (OclValue value : values) {

            if (elementsSemanticallyEqual(value,memberType, candidate, memberType)) {
                return true;
            }
        }

        return false;
    }  

    private OclRelation evaluateCoercion(CoercionExpression expression) {

        OclValue value = evaluate(expression.operand());

        if (!(value instanceof OclRelation relation)) {
            throw new IllegalStateException(
                "Collection-Coercion erwartet eine Relation."
                );
        }

        if (!expression.collectionKind().isUnique()) {
            return relation;
        }

        return removeDuplicates(relation);
        }

private OclRelation removeDuplicates(
        OclRelation relation
        ) {
        List<OclValue> uniqueElements =
                new ArrayList<>();

        for (OclValue element : relation.elements()) {
                if (!uniqueElements.contains(element)) {
                uniqueElements.add(element);
                }
        }

        return new OclRelation(uniqueElements);
        }

    private OclValue evaluateIteration(IterationExpression expression) {

        OclValue sourceValue = evaluate(expression.source()); // Ausdruck über dessen Ergebnis iteriert wird

        if (!(sourceValue instanceof OclRelation sourceRelation)) {
                throw new IllegalStateException("Iteration erwartet eine Relation als Source.");
        }

        OclValue accumulatorValue = evaluate(expression.initialValue()); // Startzustand der Iteration

        CType sourceType = typeChecker.determineType(expression.source()); // Statische Typ der Source wird bestimmt

        CType initialType = typeChecker.determineType(expression.initialValue()); // Typ des Initialwerts des Akkumulators wird bestimmt

        CType iteratorType = CType.singletonOf(sourceType.memberType()); // Typ des Akkumulators aus Typ des Initialwerts abgeleitet

        CType accumulatorType = initialType.nullable(); // Erweitert den Typ, sodass leerer bzw. optionaler Zustand zulässig ist

        for (OclValue element : sourceRelation.elements()) {

            // Neue lokale Laufzeitumgebung wird erstellt
            Environment localEnvironment =environment.createChild();
            
            // Hier wird die Iteratorvariable im lokalen Environment definiert
            localEnvironment.define(expression.iteratorVariable(), new OclRelation(List.of(element)));
            
            // Hier wird auch die Akkumulatorvariable im lokalen Environment definiert
            localEnvironment.define(expression.accumulatorVariable(), accumulatorValue);
            
            // Lokale Typumgebung wird erzeugt
            TypeEnvironment localTypeEnvironment = typeChecker.environment().createChild();

            localTypeEnvironment.define(expression.iteratorVariable(), iteratorType);

            localTypeEnvironment.define(expression.accumulatorVariable(), accumulatorType);
            
            // Lokaler TypeChecker wird erzeugt, der die lokalen Typen erkennt
            TypeChecker localTypeChecker = new TypeChecker(localTypeEnvironment, typeChecker.modelTypeContext());

            Interpreter localInterpreter = new Interpreter(localEnvironment, objectStore, localTypeChecker);

            accumulatorValue = localInterpreter.evaluate(expression.body());
        }

        return accumulatorValue;
    }

    private OclRelation evaluateConditional(ConditionalExpression expression) {
        
        OclValue conditionValue = evaluate(expression.condition());

        if (!(conditionValue instanceof OclRelation conditionRelation)) {
            throw new IllegalStateException("Die Bedingung muss eine Relation sein.");
        }

        if (conditionRelation.elements().isEmpty()) {
                return new OclRelation(List.of());
        }

        if (conditionRelation.elements().size() != 1) {
            throw new IllegalStateException("Die Bedingung muss eine Singleton- oder leere Relation sein.");
        }

        OclValue element = conditionRelation.elements().get(0);

        if (!(element instanceof OclBoolean booleanValue)) {
                throw new IllegalStateException(
                        "Die Bedingung muss einen Boolean enthalten."
                );
        }

        if (booleanValue.value()) {
                OclValue thenValue =
                        evaluate(expression.thenBranch());

                return requireRelation(
                        thenValue,
                        "Then-Zweig"
                );
        }

        OclValue elseValue =
                evaluate(expression.elseBranch());

        return requireRelation(
                elseValue,
                "Else-Zweig"
        );
        }

private OclRelation requireRelation(
        OclValue value,
        String description
        ) {
        if (!(value instanceof OclRelation relation)) {
                throw new IllegalStateException(
                        description
                                + " muss eine Relation ergeben."
                );
        }

        return relation;
        }
}