package dev.buzzec.opencomputersservo.content;

import li.cil.oc.api.machine.Callback;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServoCallbackSurfaceTest {
    private static final Set<String> EXPECTED_CALLBACKS = Set.of(
            "setMode",
            "getMode",
            "setSpeed",
            "getSpeed",
            "getCommandedSpeed",
            "setTarget",
            "getTarget",
            "getAngle",
            "isAtTarget",
            "resetAngle",
            "stop",
            "getVariant",
            "isPowered");

    @Test
    void bothVariantsExposeTheSameComputerApi() {
        assertEquals(EXPECTED_CALLBACKS, callbacksOn(ComputerServoBlockEntity.class));
        assertEquals(EXPECTED_CALLBACKS, callbacksOn(ExternalServoBlockEntity.class));
    }

    private static Set<String> callbacksOn(Class<?> type) {
        return Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Callback.class))
                .map(Method::getName)
                .collect(Collectors.toSet());
    }
}
