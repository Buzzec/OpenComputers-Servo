package dev.buzzec.opencomputersservo.client;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.simibubi.create.content.kinetics.transmission.SplitShaftRenderer;
import com.simibubi.create.content.kinetics.transmission.SplitShaftVisual;
import dev.buzzec.opencomputersservo.OpenComputersServo;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = OpenComputersServo.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                OpenComputersServo.COMPUTER_SERVO_BLOCK_ENTITY.get(), ComputerServoRenderer::new);
        event.registerBlockEntityRenderer(
                OpenComputersServo.EXTERNAL_SERVO_BLOCK_ENTITY.get(), context -> new SplitShaftRenderer(context));
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            SimpleBlockEntityVisualizer.builder(OpenComputersServo.COMPUTER_SERVO_BLOCK_ENTITY.get())
                    .factory(OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF))
                    .skipVanillaRender(blockEntity -> true)
                    .apply();
            SimpleBlockEntityVisualizer.builder(OpenComputersServo.EXTERNAL_SERVO_BLOCK_ENTITY.get())
                    .factory(SplitShaftVisual::new)
                    .skipVanillaRender(blockEntity -> true)
                    .apply();
        });
    }
}
