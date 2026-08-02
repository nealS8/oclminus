package oclminus.type;

import java.util.Objects;

import oclminus.ast.BooleanLiteral;
import oclminus.ast.CoercionExpression;
import oclminus.ast.Expression;
import oclminus.ast.IntegerLiteral;
import oclminus.ast.LiftExpression;
import oclminus.ast.LowerExpression;
import oclminus.ast.NoExpression;
import oclminus.ast.UnaryExpression;
import oclminus.ast.VariableExpression;
import oclminus.ast.BinaryExpression;
import oclminus.ast.BinaryOperator;
import oclminus.ast.AllInstancesExpression;
import oclminus.ast.PropertyAccessExpression;
import oclminus.ast.IterationExpression;

public final class TypeChecker {

    private final TypeEnvironment environment;
    private final ModelTypeContext modelTypeContext;

    public TypeChecker() {
    this(
            new TypeEnvironment(),
            new ModelTypeContext()
    );
}

public TypeChecker(
        TypeEnvironment environment
) {
    this(
            environment,
            new ModelTypeContext()
    );
}

public TypeChecker(
        TypeEnvironment environment,
        ModelTypeContext modelTypeContext
) {
    this.environment = Objects.requireNonNull(
            environment,
            "TypeEnvironment darf nicht null sein."
    );

    this.modelTypeContext = Objects.requireNonNull(
            modelTypeContext,
            "ModelTypeContext darf nicht null sein."
    );
}

    public TypedExpression check(Expression expression) {
        Objects.requireNonNull(
                expression,
                "Expression darf nicht null sein."
        );

        CType type = determineType(expression);

        return new TypedExpression(
                expression,
                type
        );
    }

    public CType determineType(Expression expression) {
        Objects.requireNonNull(
                expression,
                "Expression darf nicht null sein."
        );

        if (expression instanceof IntegerLiteral) {
            return CType.singletonOf(
                    PrimitiveType.INTEGER
            );
        }

        if (expression instanceof BooleanLiteral) {
            return CType.singletonOf(
                    PrimitiveType.BOOLEAN
            );
        }

        if (expression
                instanceof VariableExpression variableExpression) {
            return environment.lookup(
                    variableExpression.name()
            );
        }

        if (expression
                instanceof NoExpression noExpression) {
            return CType.optionOf(
                    parseMemberType(
                            noExpression.typeName()
                    )
            );
        }

        if (expression
                instanceof LiftExpression liftExpression) {
            CType operandType =
                    determineType(
                            liftExpression.operand()
                    );

            return CType.singletonOf(operandType);
        }

        if (expression
                instanceof LowerExpression lowerExpression) {
            return determineLowerType(
                    lowerExpression
            );
        }

        if (expression
                instanceof CoercionExpression coercionExpression) {
            return determineCoercionType(
                    coercionExpression
            );
        }

        if (expression instanceof BinaryExpression binaryExpression) {
            return determineBinaryType(binaryExpression);
        }

        if (expression instanceof UnaryExpression unaryExpression) {
            return determineUnaryType(unaryExpression);
        }

        if (expression
            instanceof AllInstancesExpression allInstancesExpression) {
                return determineAllInstancesType(
                        allInstancesExpression
                );
        }

        if (expression
                instanceof PropertyAccessExpression propertyAccessExpression) {
                        return determinePropertyAccessType(
                                propertyAccessExpression
                        );
        }

        if (expression
                instanceof IterationExpression iterationExpression) {
                        return determineIterationType(
                                iterationExpression
                );
        }

        throw new TypeCheckException(
                "Für den Ausdruckstyp '"
                        + expression.getClass().getSimpleName()
                        + "' ist noch keine Typregel implementiert."
        );
    }

    private CType determineLowerType(
            LowerExpression expression
    ) {
        CType operandType =
                determineType(expression.operand());

        if (!(operandType.memberType()
                instanceof CType innerType)) {
            throw new TypeCheckException(
                    "Lower erwartet einen CType "
                            + "als Membertyp."
            );
        }

        if (!operandType.isSingleton()) {
            throw new TypeCheckException(
                    "Lower erwartet einen Singleton-Typ."
            );
        }

        return innerType;
    }

    private CType determineCoercionType(
            CoercionExpression expression
    ) {
        CType operandType =
                determineType(expression.operand());

        return CType.collectionOf(
                operandType.memberType(),
                expression.collectionKind()
        );
    }

    private MemberType parseMemberType(
            String typeName
    ) {
        return switch (typeName) {
            case "int", "Integer" ->
                    PrimitiveType.INTEGER;

            case "bool", "Boolean" ->
                    PrimitiveType.BOOLEAN;

            case "any", "Any" ->
                    PrimitiveType.ANY;

            default ->
                    new ClassType(typeName);
        };
    }

    private CType determineBinaryType(
        BinaryExpression expression
) {
    return switch (expression.operator()) {
        case MERGE ->
                determineMergeType(expression);

        case PLUS,
             MINUS,
             MULTIPLY,
             DIVIDE ->
                determineIntegerBinaryType(expression);

        case LESS_THAN,
             LESS_THAN_OR_EQUAL,
             GREATER_THAN,
             GREATER_THAN_OR_EQUAL ->
                determineIntegerComparisonType(expression);

        case EQUAL,
             NOT_EQUAL ->
                determineEqualityType(expression);

        case AND,
             OR,
             XOR,
             IMPLIES ->
                determineBooleanBinaryType(expression);
    };
}

    private CType determineMergeType(
        BinaryExpression expression
    ) {
        CType leftType =
                determineType(expression.left());

        CType rightType =
                determineType(expression.right());

        if (!leftType.isCollectionKind()) {
            throw new TypeCheckException(
                    "Der linke Operand von Merge muss "
                            + "ein Collection-Kind besitzen. "
                            + "Verwende beispielsweise 'as Set', "
                            + "'as Bag', 'as OSet' oder 'as Seq'."
            );
        }

        if (!memberTypesCompatible(
                leftType.memberType(),
                rightType.memberType()
        )) {
            throw new TypeCheckException(
                    "Die Membertypen von Merge sind nicht kompatibel: "
                            + leftType.memberType()
                            + " und "
                            + rightType.memberType()
                            + "."
            );
        }

        int lowerBound;

        if (leftType.collectionKind().isUnique()) {
            lowerBound = Math.max(
                    leftType.lowerBound(),
                    rightType.lowerBound()
            );
        } else {
            lowerBound = Math.addExact(
                    leftType.lowerBound(),
                    rightType.lowerBound()
            );
        }

        Integer upperBound = addUpperBounds(
                leftType.upperBound(),
                rightType.upperBound()
        );

        return new CType(
                leftType.memberType(),
                lowerBound,
                upperBound,
                leftType.unique(),
                leftType.ordered()
        );
    }

    private boolean memberTypesCompatible(
        MemberType left,
        MemberType right
    ) {
        if (left.equals(right)) {
            return true;
        }

        return left == PrimitiveType.ANY
                || right == PrimitiveType.ANY;
    }

    private Integer addUpperBounds(
        Integer left,
        Integer right
    ) {
        if (left == null || right == null) {
            return null;
        }

        return Math.addExact(left, right);
    }

private CType determineIntegerBinaryType(
        BinaryExpression expression
        ) {
        CType leftType =
                determineType(expression.left());

        CType rightType =
                determineType(expression.right());

        requireIntegerScalarType(
                leftType,
                "linken"
        );

        requireIntegerScalarType(
                rightType,
                "rechten"
        );

        int lowerBound =
                leftType.lowerBound()
                        * rightType.lowerBound();

        Integer upperBound =
                multiplyUpperBounds(
                        leftType.upperBound(),
                        rightType.upperBound()
                );

        return new CType(
                PrimitiveType.INTEGER,
                lowerBound,
                upperBound,
                null,
                null
        );
        }

private void requireIntegerScalarType(
        CType type,
        String operandDescription
        ) {
        if (type.memberType() != PrimitiveType.INTEGER) {
                throw new TypeCheckException(
                        "Der "
                                + operandDescription
                                + " Operand muss den Membertyp int besitzen."
                );
        }

        if (!type.isSingleton()
                && !type.isOption()) {
                throw new TypeCheckException(
                        "Der "
                                + operandDescription
                                + " Operand muss ein Singleton- "
                                + "oder Option-Typ sein."
                );
        }
        }

private Integer multiplyUpperBounds(
        Integer left,
        Integer right
        ) {
        if (left == null || right == null) {
                return null;
        }

        return Math.multiplyExact(
                left,
                right
        );
        }

private CType determineIntegerComparisonType(
        BinaryExpression expression
        ) {
        CType leftType =
                determineType(expression.left());

        CType rightType =
                determineType(expression.right());

        requireIntegerScalarType(
                leftType,
                "linken"
        );

        requireIntegerScalarType(
                rightType,
                "rechten"
        );

        int lowerBound =
                leftType.lowerBound()
                        * rightType.lowerBound();

        Integer upperBound =
                multiplyUpperBounds(
                        leftType.upperBound(),
                        rightType.upperBound()
                );

        return new CType(
                PrimitiveType.BOOLEAN,
                lowerBound,
                upperBound,
                null,
                null
        );
        }

private CType determineEqualityType(
        BinaryExpression expression
        ) {
        CType leftType =
                determineType(expression.left());

        CType rightType =
                determineType(expression.right());

        if (!typesComparable(
                leftType,
                rightType
        )) {
                throw new TypeCheckException(
                        "Die Operandentypen von "
                                + expression.operator()
                                + " sind nicht vergleichbar: "
                                + leftType
                                + " und "
                                + rightType
                                + "."
                );
        }

        return CType.singletonOf(
                PrimitiveType.BOOLEAN
        );
        }

private boolean typesComparable(
        CType left,
        CType right
        ) {
        if (!memberTypesCompatible(
                left.memberType(),
                right.memberType()
        )) {
                return false;
        }

        if (left.isCollectionKind()
                && right.isCollectionKind()) {
                return left.collectionKind()
                        == right.collectionKind();
        }

        if (left.isCollectionKind()
                != right.isCollectionKind()) {
                return false;
        }

        return true;
        }

private CType determineBooleanBinaryType(
        BinaryExpression expression
        ) {
        CType leftType =
                determineType(expression.left());

        CType rightType =
                determineType(expression.right());

        requireBooleanScalarType(
                leftType,
                "linken"
        );

        requireBooleanScalarType(
                rightType,
                "rechten"
        );

        int lowerBound =
                leftType.lowerBound()
                        * rightType.lowerBound();

        Integer upperBound =
                multiplyUpperBounds(
                        leftType.upperBound(),
                        rightType.upperBound()
                );

        return new CType(
                PrimitiveType.BOOLEAN,
                lowerBound,
                upperBound,
                null,
                null
        );
        }

private void requireBooleanScalarType(
        CType type,
        String operandDescription
        ) {
        if (type.memberType() != PrimitiveType.BOOLEAN) {
                throw new TypeCheckException(
                        "Der "
                                + operandDescription
                                + " Operand muss den Membertyp bool besitzen."
                );
        }

        if (!type.isSingleton()
                && !type.isOption()) {
                throw new TypeCheckException(
                        "Der "
                                + operandDescription
                                + " Operand muss ein Singleton- "
                                + "oder Option-Typ sein."
                );
        }
        }

private CType determineUnaryType(
        UnaryExpression expression
        ) {
        CType operandType =
                determineType(expression.operand());

        return switch (expression.operator()) {
                case NOT -> {
                requireBooleanScalarType(
                        operandType,
                        "unäre"
                );

                yield new CType(
                        PrimitiveType.BOOLEAN,
                        operandType.lowerBound(),
                        operandType.upperBound(),
                        null,
                        null
                );
                }

                case NEGATE -> {
                requireIntegerScalarType(
                        operandType,
                        "unäre"
                );

                yield new CType(
                        PrimitiveType.INTEGER,
                        operandType.lowerBound(),
                        operandType.upperBound(),
                        null,
                        null
                );
                }
        };
        }

private CType determineAllInstancesType(
        AllInstancesExpression expression
        ) {
        return CType.setOf(
                new ClassType(
                        expression.className()
                )
        );
        }

private CType determinePropertyAccessType(
        PropertyAccessExpression expression
        ) {
        CType targetType =
                determineType(expression.target());

        if (!(targetType.memberType()
                instanceof ClassType classType)) {
                throw new TypeCheckException(
                        "Property Access erwartet einen Klassentyp "
                                + "als Membertyp, erhalten wurde: "
                                + targetType.memberType()
                                + "."
                );
        }

        CType propertyType =
                modelTypeContext.lookupProperty(
                        classType.className(),
                        expression.propertyName()
                );

        return combineNavigationTypes(
                targetType,
                propertyType
        );
        }

private CType combineNavigationTypes(
        CType targetType,
        CType propertyType
) {
    int lowerBound;

    try {
        lowerBound = Math.multiplyExact(
                targetType.lowerBound(),
                propertyType.lowerBound()
        );
    } catch (ArithmeticException exception) {
        throw new TypeCheckException(
                "Untere Multiplizitätsgrenze ist zu groß."
        );
    }

    Integer upperBound =
            multiplyUpperBounds(
                    targetType.upperBound(),
                    propertyType.upperBound()
            );

    if (upperBound != null && upperBound <= 1) {
        return new CType(
                propertyType.memberType(),
                lowerBound,
                upperBound,
                null,
                null
        );
    }

    Boolean ordered =
            combineOrderedness(
                    targetType,
                    propertyType
            );

    return new CType(
            propertyType.memberType(),
            lowerBound,
            upperBound,
            false,
            ordered
    );
}

private Boolean combineOrderedness(
        CType targetType,
        CType propertyType
        ) {
        boolean targetOrdered =
                Boolean.TRUE.equals(
                        targetType.ordered()
                );

        boolean propertyOrdered =
                Boolean.TRUE.equals(
                        propertyType.ordered()
                );

        if (targetType.isSingleton()
                && propertyType.isSingleton()) {
                return null;
        }

        if ((targetType.isSingleton()
                || targetType.isOption())
                && (propertyType.isSingleton()
                || propertyType.isOption())) {
                return null;
        }

        return targetOrdered || propertyOrdered;
        }

private CType determineIterationType(
        IterationExpression expression
        ) {
        CType sourceType =
                determineType(expression.source());

        CType initialType =
                determineType(expression.initialValue());

        CType iteratorType =
                CType.singletonOf(
                        sourceType.memberType()
                );

        CType accumulatorType =
                initialType.nullable();

        TypeEnvironment localEnvironment =
                environment.createChild();

        localEnvironment.define(
                expression.iteratorVariable(),
                iteratorType
        );

        localEnvironment.define(
                expression.accumulatorVariable(),
                accumulatorType
        );

        TypeChecker localChecker =
                new TypeChecker(
                        localEnvironment,
                        modelTypeContext
                );

        CType bodyType =
                localChecker.determineType(
                        expression.body()
                );

        if (!isSubtypeCompatible(
                bodyType,
                accumulatorType
        )) {
                throw new TypeCheckException(
                        "Der Typ des Iterationsrumpfs "
                                + bodyType
                                + " ist nicht mit dem Akkumulatortyp "
                                + accumulatorType
                                + " kompatibel."
                );
        }

        return bodyType;
        }

private boolean isSubtypeCompatible(
        CType candidate,
        CType expected
        ) {
        if (!memberTypesCompatible(
                candidate.memberType(),
                expected.memberType()
        )) {
                return false;
        }

        if (candidate.lowerBound()
                < expected.lowerBound()) {
                return false;
        }

        if (!upperBoundFits(
                candidate.upperBound(),
                expected.upperBound()
        )) {
                return false;
        }

        if (candidate.isCollectionKind()
                && expected.isCollectionKind()) {
                return candidate.collectionKind()
                        == expected.collectionKind();
        }

        return candidate.isCollectionKind()
                == expected.isCollectionKind();
        }

private boolean upperBoundFits(
        Integer candidate,
        Integer expected
        ) {
        if (expected == null) {
                return true;
        }

        if (candidate == null) {
                return false;
        }

        return candidate <= expected;
        }
}