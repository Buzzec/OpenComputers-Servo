package dev.buzzec.opencomputersservo.content;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.buzzec.opencomputersservo.OpenComputersServo;
import dev.buzzec.opencomputersservo.control.ServoControl;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class ComputerServoBlockEntity extends GeneratingKineticBlockEntity implements ServoEnvironment {
    public static final double IDLE_ENERGY_PER_TICK = 0.05;
    public static final double ENERGY_PER_RPM_PER_TICK = 0.02;

    private final ServoComponent servo = new ServoComponent(this, true);
    private float activeSpeed;
    private boolean powered = true;

    public ComputerServoBlockEntity(BlockPos pos, BlockState state) {
        super(OpenComputersServo.COMPUTER_SERVO_BLOCK_ENTITY.get(), pos, state);
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
        double requested = servo.control().requestedSpeed();
        boolean idle = Math.abs(requested) < 1.0e-6;
        boolean hasPower = idle || servo.consume(IDLE_ENERGY_PER_TICK
                + ENERGY_PER_RPM_PER_TICK * Math.abs(requested));
        float nextSpeed = hasPower ? (float) requested : 0;
        powered = hasPower;

        if (Math.abs(nextSpeed - activeSpeed) > 1.0e-4f) {
            activeSpeed = nextSpeed;
            updateGeneratedRotation();
        }
        setChanged();
    }

    @Override
    public float getGeneratedSpeed() {
        if (!getBlockState().is(OpenComputersServo.COMPUTER_SERVO.get())) {
            return 0;
        }
        return convertToDirection(activeSpeed, getBlockState().getValue(ComputerServoBlock.FACING));
    }

    public double actualOutputSpeed() {
        return convertToDirection(getSpeed(), getBlockState().getValue(ComputerServoBlock.FACING));
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putFloat("ServoActiveSpeed", activeSpeed);
        tag.putBoolean("ServoPowered", powered);
        servo.write(tag, registries, clientPacket);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        float storedSpeed = tag.getFloat("ServoActiveSpeed");
        activeSpeed = (float) ServoControl.clampSpeed(Float.isFinite(storedSpeed) ? storedSpeed : 0);
        powered = !tag.contains("ServoPowered") || tag.getBoolean("ServoPowered");
        servo.read(tag, registries, clientPacket);
        super.read(tag, registries, clientPacket);
    }

    private void controlChanged() {
        setChanged();
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
        return new Object[]{"computer_powered"};
    }

    @Callback(doc = "function():boolean -- Return whether the current motion request has OC energy.")
    public Object[] isPowered(Context context, Arguments arguments) {
        return new Object[]{powered};
    }
}
