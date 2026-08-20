package dev.buzzec.opencomputersservo.control;

public final class ServoControl {
    public static final double MAX_RPM = 256.0;
    public static final double DEFAULT_TARGET_RPM = 16.0;
    public static final double DEGREES_PER_TICK_PER_RPM = 0.3;
    public static final double TARGET_TOLERANCE = 0.01;

    private ServoMode mode = ServoMode.CONTINUOUS;
    private double continuousSpeed;
    private double targetAngle;
    private double maxTargetSpeed = DEFAULT_TARGET_RPM;
    private double angle;

    public synchronized ServoMode mode() {
        return mode;
    }

    public synchronized void setMode(ServoMode mode) {
        this.mode = mode;
    }

    public synchronized void setContinuousSpeed(double rpm) {
        continuousSpeed = clampSpeed(rpm);
    }

    public synchronized void commandContinuous(double rpm) {
        continuousSpeed = clampSpeed(rpm);
        mode = ServoMode.CONTINUOUS;
    }

    public synchronized double continuousSpeed() {
        return continuousSpeed;
    }

    public synchronized void commandTarget(double target, double maxRpm) {
        targetAngle = normalizeAngle(target);
        maxTargetSpeed = clampMagnitude(maxRpm);
        mode = ServoMode.ANGLE;
    }

    public synchronized double targetAngle() {
        return targetAngle;
    }

    public synchronized double maxTargetSpeed() {
        return maxTargetSpeed;
    }

    public synchronized double angle() {
        return angle;
    }

    public synchronized void resetAngle(double newAngle) {
        angle = normalizeAngle(newAngle);
    }

    public synchronized void advance(double actualRpm) {
        if (!Double.isFinite(actualRpm)) {
            return;
        }
        angle = normalizeAngle(angle + actualRpm * DEGREES_PER_TICK_PER_RPM);
        if (mode == ServoMode.ANGLE && Math.abs(shortestDelta(angle, targetAngle)) <= TARGET_TOLERANCE) {
            angle = targetAngle;
        }
    }

    public synchronized double requestedSpeed() {
        if (mode == ServoMode.CONTINUOUS) {
            return continuousSpeed;
        }

        double delta = shortestDelta(angle, targetAngle);
        if (Math.abs(delta) <= TARGET_TOLERANCE || maxTargetSpeed == 0) {
            return 0;
        }

        double noOvershootSpeed = Math.abs(delta) / DEGREES_PER_TICK_PER_RPM;
        return Math.copySign(Math.min(maxTargetSpeed, noOvershootSpeed), delta);
    }

    public synchronized boolean isAtTarget() {
        return mode == ServoMode.ANGLE
                && Math.abs(shortestDelta(angle, targetAngle)) <= TARGET_TOLERANCE;
    }

    public synchronized void stop() {
        continuousSpeed = 0;
        mode = ServoMode.CONTINUOUS;
    }

    public synchronized void restore(ServoMode mode, double continuousSpeed, double targetAngle,
                                     double maxTargetSpeed, double angle) {
        this.mode = mode;
        this.continuousSpeed = clampSpeed(continuousSpeed);
        this.targetAngle = normalizeAngle(targetAngle);
        this.maxTargetSpeed = clampMagnitude(maxTargetSpeed);
        this.angle = normalizeAngle(angle);
    }

    public static double normalizeAngle(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("angle must be finite");
        }
        double normalized = value % 360.0;
        if (normalized < 0) {
            normalized += 360.0;
        }
        return normalized == 360.0 ? 0.0 : normalized;
    }

    public static double shortestDelta(double from, double to) {
        double delta = normalizeAngle(to) - normalizeAngle(from);
        delta = (delta + 540.0) % 360.0 - 180.0;
        return delta == -180.0 ? 180.0 : delta;
    }

    public static double clampSpeed(double rpm) {
        requireFinite(rpm, "speed");
        return Math.max(-MAX_RPM, Math.min(MAX_RPM, rpm));
    }

    public static double clampMagnitude(double rpm) {
        requireFinite(rpm, "speed");
        return Math.min(MAX_RPM, Math.abs(rpm));
    }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}
