package fuzs.strawstatues;

import fuzs.puzzlesapi.api.statues.v1.world.inventory.data.ArmorStandPose;
import fuzs.puzzlesapi.api.statues.v1.world.inventory.data.ArmorStandStyleOption;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.context.BuildCreativeModeTabContentsContext;
import fuzs.puzzleslib.api.core.v1.context.EntityAttributesCreateContext;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.event.v1.entity.player.PlayerInteractEvents;
import fuzs.puzzleslib.api.network.v2.MessageDirection;
import fuzs.puzzleslib.api.network.v2.NetworkHandlerV2;
import fuzs.strawstatues.init.ModRegistry;
import fuzs.strawstatues.network.client.C2SStrawStatueModelPartMessage;
import fuzs.strawstatues.network.client.C2SStrawStatueOwnerMessage;
import fuzs.strawstatues.network.client.C2SStrawStatueScaleMessage;
import fuzs.strawstatues.world.entity.decoration.StrawStatue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StrawStatues implements ModConstructor {
    public static final String MOD_ID = "strawstatues";
    public static final String MOD_NAME = "Straw Statues";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final NetworkHandlerV2 NETWORK = NetworkHandlerV2.build(MOD_ID);

    @Override
    public void onConstructMod() {
        ModRegistry.touch();
        registerMessages();
        registerHandlers();
    }

    private static void registerMessages() {
        NETWORK.register(C2SStrawStatueModelPartMessage.class, C2SStrawStatueModelPartMessage::new, MessageDirection.TO_SERVER);
        NETWORK.register(C2SStrawStatueOwnerMessage.class, C2SStrawStatueOwnerMessage::new, MessageDirection.TO_SERVER);
        NETWORK.register(C2SStrawStatueScaleMessage.class, C2SStrawStatueScaleMessage::new, MessageDirection.TO_SERVER);
    }

    private static void registerHandlers() {
        // high priority so we run before the Quark mod
        PlayerInteractEvents.USE_ENTITY_AT.register(EventPhase.BEFORE, StrawStatue::onUseEntityAt);
    }

    @Override
    public void onCommonSetup() {
        ArmorStandStyleOption.register(id("slimarms"), ModRegistry.SLIM_ARMS_STYLE_OPTION);
        ArmorStandStyleOption.register(id("crouching"), ModRegistry.CROUCHING_STYLE_OPTION);
        DispenserBlock.registerBehavior(ModRegistry.STRAW_STATUE_ITEM.get(), new DefaultDispenseItemBehavior() {

            @Override
            public ItemStack execute(BlockSource blockSource, ItemStack stack) {
                Direction direction = blockSource.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos blockpos = blockSource.getPos().relative(direction);
                Level level = blockSource.getLevel();
                ArmorStand armorstand = new StrawStatue(level, blockpos.getX() + 0.5, blockpos.getY(), blockpos.getZ() + 0.5);
                EntityType.updateCustomEntityTag(level, null, armorstand, stack.getTag());
                armorstand.setYRot(direction.toYRot());
                ArmorStandPose.randomValue().applyToEntity(armorstand);
                level.addFreshEntity(armorstand);
                stack.shrink(1);
                return stack;
            }
        });
    }

    @Override
    public void onEntityAttributeCreation(EntityAttributesCreateContext context) {
        context.registerEntityAttributes(ModRegistry.STRAW_STATUE_ENTITY_TYPE.get(), LivingEntity.createLivingAttributes());
    }

    @Override
    public void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsContext context) {
        context.registerBuildListener(CreativeModeTabs.FUNCTIONAL_BLOCKS, (itemDisplayParameters, output) -> {
            output.accept(ModRegistry.STRAW_STATUE_ITEM.get());
        });
        context.registerBuildListener(CreativeModeTabs.REDSTONE_BLOCKS, (itemDisplayParameters, output) -> {
            output.accept(ModRegistry.STRAW_STATUE_ITEM.get());
        });
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
