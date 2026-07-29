package oclminus;

import oclminus.runtime.OclBoolean;
import oclminus.runtime.OclInteger;
import oclminus.runtime.OclValue;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        OclMinusEngine engine = new OclMinusEngine();

        evaluateAndPrint(engine, "42");
        evaluateAndPrint(engine, "true");
        evaluateAndPrint(engine, "false");

        evaluateAndPrint(engine, "2 + 3");
        evaluateAndPrint(engine, "2 * 3");
        evaluateAndPrint(engine, "2 + 3 * 4");

        evaluateAndPrint(engine, "true + 3");
    }

    private static void evaluateAndPrint(
            OclMinusEngine engine,
            String source
    ) {
        try {
            System.out.print(source + " = ");

            OclValue result = engine.evaluate(source);

            printResult(result);
        } catch (RuntimeException exception) {
            System.out.println(
                    "Fehler: " + exception.getMessage()
            );
        }
    }

    private static void printResult(OclValue value) {
        if (value instanceof OclInteger integer) {
            System.out.println(integer.value());
            return;
        }

        if (value instanceof OclBoolean bool) {
            System.out.println(bool.value());
            return;
        }

        throw new IllegalStateException(
                "Unbekannter OclValue-Typ: "
                        + value.getClass().getSimpleName()
        );
    }
}