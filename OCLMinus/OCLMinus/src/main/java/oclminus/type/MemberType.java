package oclminus.type;

public sealed interface MemberType
        permits PrimitiveType,
                ClassType,
                CType {
}