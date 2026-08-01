package oclminus.type;

import java.util.Objects;

public record ClassType(
        String className
) implements MemberType {

    public ClassType {
        Objects.requireNonNull(
                className,
                "Klassenname darf nicht null sein."
        );

        if (className.isBlank()) {
            throw new IllegalArgumentException(
                    "Klassenname darf nicht leer sein."
            );
        }
    }
}
