package oclminus.type;

import java.util.Objects;

import oclminus.ast.BooleanLiteral;
import oclminus.ast.CoercionExpression;
import oclminus.ast.Expression;
import oclminus.ast.IntegerLiteral;
import oclminus.ast.LiftExpression;
import oclminus.ast.LowerExpression;
import oclminus.ast.NoExpression;
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
        if (expression.operator() == BinaryOperator.MERGE) {
            return determineMergeType(expression);
        }

        throw new TypeCheckException(
                "Für den binären Operator '"
                        + expression.operator()
                        + "' ist noch keine Typregel implementiert."
        );
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
}