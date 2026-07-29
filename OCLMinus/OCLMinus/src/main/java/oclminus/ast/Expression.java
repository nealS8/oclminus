package oclminus.ast;

import java.beans.PropertyVetoException;

public sealed interface Expression
        permits IntegerLiteral, BooleanLiteral, BinaryExpression, VariableExpression, PropertyAccessExpression {
}
