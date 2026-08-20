package dev.buzzec.opencomputersservo.content;

import com.simibubi.create.content.kinetics.transmission.SplitShaftBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.buzzec.opencomputersservo.OpenComputersServo;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class ExternalServoBlockEntity extends SplitShaftBlockEntity implements ServoEnvironment {
    private static final float MAX_RATIO = 256.0f;

    private final ServoComponent servo = new ServoComponent(this, false);
    private float activeModifier;

    public ExternalServoBlockEntity(BlockPos pos, BlockState state) {
        super(OpenComputersServo.EXTERNAL_SERVO_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public ServoComponent servoComponent() {
        return servo;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void onLoad() {
        super.onLoad();
        servo.connect(this);
    }

    @Override
    public void onChunkUnloaded() {
        servo.disconnect();
        super.onChunkUnloaded();
    }

    @Override
    public void invalidate() {
        servo.disconnect();
        super.invalidate();
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) {
            return;
        }

        servo.control().advance(actualOutputSpeed());
        updateTransmission();
        setChanged();
    }

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (isVirtual()) {
            return 1;
        }
        if (!hasSource()) {
            return 0;
        }

        Direction sourceFace = getSourceFacing();
        Direction input = inputFace();
        Direction output = outputFace();
        if (sourceFace != input) {
            return face == sourceFace ? 1 : 0;
        }
        if (face == input) {
            return 1;
        }
        return face == output ? activeModifier : 0;
    }

    public double actualOutputSpeed() {
        if (!hasValidInput()) {
            return 0;
        }
        float axisSpeed = getSpeed() * activeModifier;
        return convertToDirection(axisSpeed, outputFace());
    }

    public boolean hasValidInput() {
        return hasSource() && getSourceFacing() == inputFace() && Math.abs(getSpeed()) > 1.0e-6f;
    }

    private Direction outputFace() {
        return getBlockState().getValue(ExternalServoBlock.FACING);
    }

    private Direction inputFace() {
        return outputFace().getOpposite();
    }

    private void updateTransmission() {
        float nextModifier = 0;
        if (hasValidInput()) {
            double requested = servo.control().requestedSpeed();
            float requestedAxisSpeed = convertToDirection((float) requested, outputFace());
            nextModifier = requestedAxisSpeed / getSpeed();
            nextModifier = Math.max(-MAX_RATIO, Math.min(MAX_RATIO, nextModifier));
        }

        if (Math.abs(nextModifier - activeModifier) <= 1.0e-4f) {
            return;
        }

        activeModifier = nextModifier;
        detachKinetics();
        removeSource();
        attachKinetics();
        sendData();
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putFloat("ServoModifier", activeModifier);
        servo.write(tag, registries, clientPacket);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        float storedModifier = tag.getFloat("ServoModifier");
        activeModifier = Float.isFinite(storedModifier)
                ? Math.max(-MAX_RATIO, Math.min(MAX_RATIO, storedModifier))
                : 0;
        servo.read(tag, registries, clientPacket);
        super.read(tag, registries, clientPacket);
    }

    private void controlChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            updateTransmission();
        }
        sendData();
    }

    @Callback(doc = "function(mode:string):string -- Select continuous or angle control mode.")
    public Object[] setMode(Context context, Arguments arguments) {
        Object[] result = servo.setMode(arguments);
        controlChanged();
        return result;
    }

    @Callback(doc = "function():string -- Return the current control mode.")
    public Object[] getMode(Context context, Arguments arguments) {
        return servo.getMode();
    }

    @Callback(doc = "function(rpm:number):number -- Select continuous mode and set output RPM.")
    public Object[] setSpeed(Context context, Arguments arguments) {
        Object[] result = servo.setSpeed(arguments);
        controlChanged();
        return result;
    }

    @Callback(doc = "function():number -- Return the measured output RPM.")
    public Object[] getSpeed(Context context, Arguments arguments) {
        return new Object[]{actualOutputSpeed()};
    }

    @Callback(doc = "function():number -- Return the RPM currently requested by the controller.")
    public Object[] getCommandedSpeed(Context context, Arguments arguments) {
        return servo.getCommandedSpeed();
    }

    @Callback(doc = "function(angle:number[, maxRpm:number]):number, number -- Target an angle by the shortest path.")
    public Object[] setTarget(Context context, Arguments arguments) {
        Object[] result = servo.setTarget(arguments);
        controlChanged();
        return result;
    }

    @Callback(doc = "function():number, number -- Return target angle and maximum target RPM.")
    public Object[] getTarget(Context context, Arguments arguments) {
        return servo.getTarget();
    }

    @Callback(doc = "function():number -- Return the current angle in degrees from 0 through 360.")
    public Object[] getAngle(Context context, Arguments arguments) {
        return servo.getAngle();
    }

    @Callback(doc = "function():boolean -- Return whether the angle target has been reached.")
    public Object[] isAtTarget(Context context, Arguments arguments) {
        return servo.isAtTarget();
    }

    @Callback(doc = "function([angle:number]):number -- Recalibrate the current angle without moving.")
    public Object[] resetAngle(Context context, Arguments arguments) {
        Object[] result = servo.resetAngle(arguments);
        controlChanged();
        return result;
    }

    @Callback(doc = "function():boolean -- Stop the servo and select continuous mode.")
    public Object[] stop(Context context, Arguments arguments) {
        Object[] result = servo.stop();
        controlChanged();
        return result;
    }

    @Callback(doc = "function():string -- Return the servo power variant.")
    public Object[] getVariant(Context context, Arguments arguments) {
        return new Object[]{"external_rotation"};
    }

    @Callback(doc = "function():boolean -- Return whether rotation is entering through the rear shaft.")
    public Object[] isPowered(Context context, Arguments arguments) {
        return new Object[]{hasValidInput()};
    }
}
