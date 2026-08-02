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

public final class TypeChecker {

    private final TypeEnvironment environment;

    public TypeChecker() {
        this(new TypeEnvironment());
    }

    public TypeChecker(TypeEnvironment environment) {
        this.environment = Objects.requireNonNull(
                environment,
                "TypeEnvironment darf nicht null sein."
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
}