package dev.buzzec.opencomputersservo.client;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.buzzec.opencomputersservo.content.ComputerServoBlockEntity;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public final class ComputerServoRenderer extends KineticBlockEntityRenderer<ComputerServoBlockEntity> {
    public ComputerServoRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(ComputerServoBlockEntity blockEntity, BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state);
    }
}
