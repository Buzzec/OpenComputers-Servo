package dev.buzzec.opencomputersservo.content;

import com.simibubi.create.foundation.block.IBE;
import dev.buzzec.opencomputersservo.OpenComputersServo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class ExternalServoBlock extends AbstractServoBlock implements IBE<ExternalServoBlockEntity> {
    public ExternalServoBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(FACING).getAxis();
    }

    @Override
    public Class<ExternalServoBlockEntity> getBlockEntityClass() {
        return ExternalServoBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ExternalServoBlockEntity> getBlockEntityType() {
        return OpenComputersServo.EXTERNAL_SERVO_BLOCK_ENTITY.get();
    }
}
