package dev.buzzec.opencomputersservo.control;

import java.util.Locale;

public enum ServoMode {
    CONTINUOUS("continuous"),
    ANGLE("angle");

    private final String serializedName;

    ServoMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return serializedName;
    }

    public static ServoMode parse(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "continuous" -> CONTINUOUS;
            case "angle", "target" -> ANGLE;
            default -> throw new IllegalArgumentException("mode must be 'continuous' or 'angle'");
        };
    }
}
