package dev.buzzec.opencomputersservo.content;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import dev.buzzec.opencomputersservo.OpenComputersServo;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

public final class ComputerServoBlock extends AbstractServoBlock implements IBE<ComputerServoBlockEntity> {
    public ComputerServoBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredFacing(context);
        if ((context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) || preferred == null) {
            return super.getStateForPlacement(context);
        }
        return defaultBlockState().setValue(DirectionalKineticBlock.FACING, preferred);
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING);
    }

    @Override
    public Class<ComputerServoBlockEntity> getBlockEntityClass() {
        return ComputerServoBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ComputerServoBlockEntity> getBlockEntityType() {
        return OpenComputersServo.COMPUTER_SERVO_BLOCK_ENTITY.get();
    }
}
