package dev.buzzec.opencomputersservo.content;

import dev.buzzec.opencomputersservo.OpenComputersServo;
import dev.buzzec.opencomputersservo.control.ServoControl;
import dev.buzzec.opencomputersservo.control.ServoMode;
import li.cil.oc.api.Network;
import li.cil.oc.api.UnrecoverablePersistanceException;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.network.Connector;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ServoComponent {
    private static final String CONTROL_TAG = "ServoControl";
    private static final String NODE_TAG = "ServoNode";
    private static final double CONNECTOR_BUFFER = 200.0;

    private final ServoControl control = new ServoControl();
    private final Node node;

    public ServoComponent(Environment owner, boolean withPowerConnector) {
        var builder = Network.newNode(owner, Visibility.Network)
                .withComponent("servo", Visibility.Network);
        node = withPowerConnector
                ? builder.withConnector(CONNECTOR_BUFFER).create()
                : builder.create();
    }

    public ServoControl control() {
        return control;
    }

    public Node node() {
        return node;
    }

    public boolean consume(double amount) {
        return node instanceof Connector connector
                && connector.tryChangeBuffer(-Math.max(0, amount));
    }

    public void connect(BlockEntity blockEntity) {
        if (blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide) {
            Network.joinOrCreateNetwork(blockEntity);
        }
    }

    public void disconnect() {
        node.remove();
    }

    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        CompoundTag controlTag = new CompoundTag();
        controlTag.putString("Mode", control.mode().serializedName());
        controlTag.putDouble("ContinuousSpeed", control.continuousSpeed());
        controlTag.putDouble("TargetAngle", control.targetAngle());
        controlTag.putDouble("MaxTargetSpeed", control.maxTargetSpeed());
        controlTag.putDouble("Angle", control.angle());
        tag.put(CONTROL_TAG, controlTag);

        if (!clientPacket) {
            CompoundTag nodeTag = new CompoundTag();
            node.saveData(nodeTag, registries);
            tag.put(NODE_TAG, nodeTag);
        }
    }

    public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        if (tag.contains(CONTROL_TAG)) {
            CompoundTag controlTag = tag.getCompound(CONTROL_TAG);
            ServoMode mode;
            try {
                mode = ServoMode.parse(controlTag.getString("Mode"));
            } catch (IllegalArgumentException ignored) {
                mode = ServoMode.CONTINUOUS;
            }
            control.restore(
                    mode,
                    finiteOrZero(controlTag.getDouble("ContinuousSpeed")),
                    finiteOrZero(controlTag.getDouble("TargetAngle")),
                    finiteOrDefault(controlTag.getDouble("MaxTargetSpeed"), ServoControl.DEFAULT_TARGET_RPM),
                    finiteOrZero(controlTag.getDouble("Angle")));
        }

        if (!clientPacket && tag.contains(NODE_TAG)) {
            try {
                node.loadData(tag.getCompound(NODE_TAG), registries);
            } catch (UnrecoverablePersistanceException exception) {
                OpenComputersServo.LOGGER.warn("Could not restore an OpenComputers servo node", exception);
            }
        }
    }

    public Object[] setMode(Arguments arguments) {
        control.setMode(ServoMode.parse(arguments.checkString(0)));
        return getMode();
    }

    public Object[] getMode() {
        return result(control.mode().serializedName());
    }

    public Object[] setSpeed(Arguments arguments) {
        control.commandContinuous(arguments.checkDouble(0));
        return result(control.continuousSpeed());
    }

    public Object[] getCommandedSpeed() {
        return result(control.requestedSpeed());
    }

    public Object[] setTarget(Arguments arguments) {
        double target = arguments.checkDouble(0);
        double maxSpeed = arguments.optDouble(1, control.maxTargetSpeed());
        control.commandTarget(target, maxSpeed);
        return result(control.targetAngle(), control.maxTargetSpeed());
    }

    public Object[] getTarget() {
        return result(control.targetAngle(), control.maxTargetSpeed());
    }

    public Object[] getAngle() {
        return result(control.angle());
    }

    public Object[] isAtTarget() {
        return result(control.isAtTarget());
    }

    public Object[] resetAngle(Arguments arguments) {
        control.resetAngle(arguments.optDouble(0, 0));
        return result(control.angle());
    }

    public Object[] stop() {
        control.stop();
        return result(true);
    }

    private static Object[] result(Object... values) {
        return values;
    }

    private static double finiteOrZero(double value) {
        return Double.isFinite(value) ? value : 0;
    }

    private static double finiteOrDefault(double value, double fallback) {
        return Double.isFinite(value) ? value : fallback;
    }
}
