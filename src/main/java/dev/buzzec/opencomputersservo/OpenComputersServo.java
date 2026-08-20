package dev.buzzec.opencomputersservo;

import com.simibubi.create.api.stress.BlockStressValues;
import dev.buzzec.opencomputersservo.content.ComputerServoBlock;
import dev.buzzec.opencomputersservo.content.ComputerServoBlockEntity;
import dev.buzzec.opencomputersservo.content.ExternalServoBlock;
import dev.buzzec.opencomputersservo.content.ExternalServoBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(OpenComputersServo.MOD_ID)
public final class OpenComputersServo {
    public static final String MOD_ID = "opencomputersservo";
    public static final Logger LOGGER = LoggerFactory.getLogger("OpenComputers Servo");

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final DeferredBlock<ComputerServoBlock> COMPUTER_SERVO = BLOCKS.register(
            "computer_servo",
            () -> new ComputerServoBlock(servoProperties(MapColor.COLOR_GRAY)));
    public static final DeferredBlock<ExternalServoBlock> EXTERNAL_SERVO = BLOCKS.register(
            "external_servo",
            () -> new ExternalServoBlock(servoProperties(MapColor.COLOR_ORANGE)));

    public static final DeferredItem<BlockItem> COMPUTER_SERVO_ITEM = ITEMS.register(
            "computer_servo",
            () -> new BlockItem(COMPUTER_SERVO.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> EXTERNAL_SERVO_ITEM = ITEMS.register(
            "external_servo",
            () -> new BlockItem(EXTERNAL_SERVO.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ComputerServoBlockEntity>>
            COMPUTER_SERVO_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "computer_servo",
            () -> BlockEntityType.Builder.of(ComputerServoBlockEntity::new, COMPUTER_SERVO.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExternalServoBlockEntity>>
            EXTERNAL_SERVO_BLOCK_ENTITY = BLOCK_ENTITIES.register(
            "external_servo",
            () -> BlockEntityType.Builder.of(ExternalServoBlockEntity::new, EXTERNAL_SERVO.get()).build(null));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.opencomputersservo"))
                    .icon(() -> new ItemStack(COMPUTER_SERVO_ITEM.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(COMPUTER_SERVO_ITEM.get());
                        output.accept(EXTERNAL_SERVO_ITEM.get());
                    })
                    .build());

    public OpenComputersServo(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        CREATIVE_TABS.register(modBus);
        modBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BlockStressValues.CAPACITIES.register(COMPUTER_SERVO.get(), () -> 4.0);
            BlockStressValues.IMPACTS.register(EXTERNAL_SERVO.get(), () -> 1.0);
        });
    }

    private static Block.Properties servoProperties(MapColor mapColor) {
        return Block.Properties.of()
                .mapColor(mapColor)
                .strength(3.0f, 6.0f)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion();
    }
}
