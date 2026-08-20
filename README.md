# OpenComputers Servo

OpenComputers Servo adds two computer-controlled Create kinetic blocks for
Minecraft 1.21.1 on NeoForge:

- **Computer-Powered Servo Motor** consumes OpenComputers network energy and
  supplies a deliberately small amount of Create stress capacity.
- **Externally-Powered Servo Motor** accepts Create rotation through its rear
  shaft and produces regulated rotation from the front shaft.

Both blocks appear as `component.servo` and support continuous rotation and
shortest-path angle targeting. Speed is limited to 256 RPM and angles are
reported in degrees, normalized to the range `[0, 360)`.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.233 or newer in the 21.1 line
- Create 6.0.10
- OpenComputers 1.9.4 or newer
- Java 21

## Placement

The motor's front face is its controlled output. The external variant's
opposite face is its required rotation input. OpenComputers cables may connect
to any side.

The computer-powered motor provides 4 base stress units and consumes
`0.05 + 0.02 × |RPM|` OpenComputers energy each tick while turning. The
external motor applies 1 base stress unit to its input network.

## Lua API

```lua
local component = require("component")
local servo = component.servo

-- Continuous mode at 32 RPM.
servo.setSpeed(32)

-- Move to 90 degrees by the shortest path, at no more than 16 RPM.
servo.setTarget(90, 16)
while not servo.isAtTarget() do
  os.sleep(0.05)
end

print(servo.getAngle(), servo.getSpeed())
servo.stop()
```

Available methods:

- `setMode("continuous" | "angle")`
- `getMode()`
- `setSpeed(rpm)`
- `getSpeed()`
- `getCommandedSpeed()`
- `setTarget(angle[, maxRpm])`
- `getTarget()`
- `getAngle()`
- `isAtTarget()`
- `resetAngle([angle])`
- `stop()`
- `getVariant()`
- `isPowered()`

`setSpeed` selects continuous mode. `setTarget` selects angle mode. The angle
target controller slows on its final tick to avoid overshooting.

## Building

Run `./gradlew build`. The jar is written to `build/libs`.

## License

MIT
