package dev.buzzec.opencomputersservo.control;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServoControlTest {
    @Test
    void continuousCommandsAreClamped() {
        ServoControl control = new ServoControl();

        control.commandContinuous(400);

        assertEquals(ServoMode.CONTINUOUS, control.mode());
        assertEquals(ServoControl.MAX_RPM, control.requestedSpeed());
    }

    @Test
    void angleModeUsesTheShortestPathAcrossZero() {
        ServoControl control = new ServoControl();
        control.resetAngle(350);

        control.commandTarget(10, 16);
        assertEquals(16, control.requestedSpeed());

        control.resetAngle(10);
        control.commandTarget(350, 16);
        assertEquals(-16, control.requestedSpeed());
    }

    @Test
    void finalTargetTickDoesNotOvershoot() {
        ServoControl control = new ServoControl();
        control.commandTarget(1, 256);

        double finalSpeed = control.requestedSpeed();
        assertEquals(1.0 / ServoControl.DEGREES_PER_TICK_PER_RPM, finalSpeed, 1.0e-9);

        control.advance(finalSpeed);
        assertEquals(1, control.angle(), ServoControl.TARGET_TOLERANCE);
        assertEquals(0, control.requestedSpeed());
        assertTrue(control.isAtTarget());
    }

    @Test
    void anglesWrapAndStopReturnsToContinuousMode() {
        ServoControl control = new ServoControl();
        control.resetAngle(-5);
        assertEquals(355, control.angle());

        control.commandTarget(90, 32);
        assertFalse(control.isAtTarget());
        control.stop();

        assertEquals(ServoMode.CONTINUOUS, control.mode());
        assertEquals(0, control.requestedSpeed());
    }
}
