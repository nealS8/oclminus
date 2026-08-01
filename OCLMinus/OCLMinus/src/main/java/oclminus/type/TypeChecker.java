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
}