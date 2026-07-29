package oclminus.ast;

public sealed interface Expression
        permits IntegerLiteral, BooleanLiteral, BinaryExpression, VariableExpression {
}
