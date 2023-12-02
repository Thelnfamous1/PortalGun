package tk.meowmc.portalgun;

import com.mojang.datafixers.util.Pair;
import me.Thelnfamous1.portalgun.DyeColorArgument;
import me.Thelnfamous1.portalgun.PortalGunCommands;
import me.Thelnfamous1.portalgun.PortalManipulationHelper;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.q_misc_util.my_util.IntBox;
import tk.meowmc.portalgun.client.PortalgunClient;
import tk.meowmc.portalgun.config.PortalGunConfig;
import tk.meowmc.portalgun.entities.CustomPortal;
import tk.meowmc.portalgun.items.ClawItem;
import tk.meowmc.portalgun.items.PortalGunItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod(PortalGunMod.MODID)
public class PortalGunMod /*implements ModInitializer*/ {
    public static final String MODID = "portalgun";
    public static final String KEY = MODID + ":portalgun_portals";
    public static final String MOD_NAME = "PortalGun Mod";
    
    public static final double portalOffset = 0.001;
    public static final double portalOverlayOffset = 0.001;

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    
    public static final RegistryObject<PortalGunItem> PORTAL_GUN = ITEMS.register("portal_gun", () -> new PortalGunItem(new Item.Properties().fireResistant().stacksTo(1).rarity(Rarity.EPIC).tab(CreativeModeTab.TAB_TOOLS)));
    public static final RegistryObject<Item> PORTAL_GUN_BODY = ITEMS.register("portalgun_body", () -> new Item(new Item.Properties().fireResistant().stacksTo(1).rarity(Rarity.RARE).tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<ClawItem> PORTAL_GUN_CLAW = ITEMS.register("portalgun_claw", () -> new ClawItem(new Item.Properties().fireResistant().stacksTo(1).rarity(Rarity.RARE).tab(CreativeModeTab.TAB_MISC)));


    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static RegistryObject<EntityType<CustomPortal>> CUSTOM_PORTAL = ENTITIES.register("custom_portal", () -> EntityType.Builder.of(CustomPortal::new, MobCategory.MISC)
            .sized(0F, 0F)
            .build(id("custom_portal").toString()));

    public static final ResourceLocation PORTAL1_SHOOT = new ResourceLocation("portalgun:portal1_shoot");
    public static final ResourceLocation PORTAL2_SHOOT = new ResourceLocation("portalgun:portal2_shoot");
    public static final ResourceLocation PORTAL_OPEN = new ResourceLocation("portalgun:portal_open");
    public static final ResourceLocation PORTAL_CLOSE = new ResourceLocation("portalgun:portal_close");

    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final RegistryObject<SoundEvent> PORTAL1_SHOOT_EVENT = SOUNDS.register(PORTAL1_SHOOT.getPath(), () -> new SoundEvent(PORTAL1_SHOOT));
    public static final RegistryObject<SoundEvent> PORTAL2_SHOOT_EVENT = SOUNDS.register(PORTAL2_SHOOT.getPath(), () -> new SoundEvent(PORTAL2_SHOOT));
    public static final RegistryObject<SoundEvent> PORTAL_OPEN_EVENT = SOUNDS.register(PORTAL_OPEN.getPath(), () -> new SoundEvent(PORTAL_OPEN));
    public static final RegistryObject<SoundEvent> PORTAL_CLOSE_EVENT = SOUNDS.register(PORTAL_CLOSE.getPath(), () -> new SoundEvent(PORTAL_CLOSE));

    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registry.COMMAND_ARGUMENT_TYPE_REGISTRY, MODID);
    private static final RegistryObject<SingletonArgumentInfo<DyeColorArgument>> DYE_COLOR_COMMAND_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES.register("dye_color", () ->
            ArgumentTypeInfos.registerByClass(DyeColorArgument.class, SingletonArgumentInfo.contextFree(DyeColorArgument::color)));
    public static final Logger LOGGER = LogManager.getLogger();

    public PortalGunMod(){
        this.onInitialize();
        if(FMLEnvironment.dist == Dist.CLIENT){
            new PortalgunClient().onInitializeClient();
        }
    }
    
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
    
    public static boolean isBlockSolid(Level world, BlockPos p) {
//        return true;
//        return !world.getBlockState(p).isAir();
        return world.getBlockState(p).isSolidRender(world, p);
    }
    
    public static boolean isAreaClear(Level world, IntBox airBox1) {
        return airBox1.fastStream().allMatch(p -> world.getBlockState(p).isAir());
    }
    
    public static boolean isWallValid(Level world, IntBox wallBox1) {
        return wallBox1.fastStream().allMatch(p -> isBlockSolid(world, p));
    }
    
    public static record PortalAwareRaytraceResult(
        Level world,
        BlockHitResult hitResult,
        List<Portal> portalsPassingThrough
    ) {}
    
    // TODO move this into ImmPtl
    @Nullable
    public static PortalAwareRaytraceResult portalAwareRayTrace(
        Entity entity, double maxDistance
    ) {
        return portalAwareRayTrace(
            entity.level,
            entity.getEyePosition(),
            entity.getViewVector(1),
            maxDistance,
            entity
        );
    }
    
    @Nullable
    public static PortalAwareRaytraceResult portalAwareRayTrace(
        Level world,
        Vec3 startingPoint,
        Vec3 direction,
        double maxDistance,
        Entity entity
    ) {
        return portalAwareRayTrace(world, startingPoint, direction, maxDistance, entity, List.of());
    }
    
    @Nullable
    public static PortalAwareRaytraceResult portalAwareRayTrace(
        Level world,
        Vec3 startingPoint,
        Vec3 direction,
        double maxDistance,
        Entity entity,
        @NotNull List<Portal> portalsPassingThrough
    ) {
        if (portalsPassingThrough.size() > 5) {
            return null;
        }
        
        Vec3 endingPoint = startingPoint.add(direction.scale(maxDistance));
        Optional<Pair<Portal, Vec3>> portalHit = PortalManipulationHelper.raytracePortals(
            world, startingPoint, endingPoint, true
        );
        
        ClipContext context = new ClipContext(
            startingPoint,
            endingPoint,
            ClipContext.Block.OUTLINE,
            ClipContext.Fluid.NONE,
            entity
        );
        BlockHitResult blockHitResult = world.clip(context);
        
        boolean portalHitFound = portalHit.isPresent();
        boolean blockHitFound = blockHitResult.getType() == HitResult.Type.BLOCK;
        
        boolean shouldContinueRaytraceInsidePortal = false;
        if (portalHitFound && blockHitFound) {
            double portalDistance = portalHit.get().getSecond().distanceTo(startingPoint);
            double blockDistance = blockHitResult.getLocation().distanceTo(startingPoint);
            if (portalDistance < blockDistance) {
                // continue raytrace from within the portal
                shouldContinueRaytraceInsidePortal = true;
            }
            else {
                return new PortalAwareRaytraceResult(
                    world, blockHitResult, portalsPassingThrough
                );
            }
        }
        else if (!portalHitFound && blockHitFound) {
            return new PortalAwareRaytraceResult(
                world, blockHitResult, portalsPassingThrough
            );
        }
        else if (portalHitFound && !blockHitFound) {
            // continue raytrace from within the portal
            shouldContinueRaytraceInsidePortal = true;
        }
        
        if (shouldContinueRaytraceInsidePortal) {
            double portalDistance = portalHit.get().getSecond().distanceTo(startingPoint);
            Portal portal = portalHit.get().getFirst();
            Vec3 newStartingPoint = portal.transformPoint(portalHit.get().getSecond())
                .add(portal.getContentDirection().scale(0.001));
            Vec3 newDirection = portal.transformLocalVecNonScale(direction);
            double restDistance = maxDistance - portalDistance;
            if (restDistance < 0) {
                return null;
            }
            return portalAwareRayTrace(
                portal.getDestinationWorld(),
                newStartingPoint,
                newDirection,
                restDistance,
                entity,
                Stream.concat(
                    portalsPassingThrough.stream(), Stream.of(portal)
                ).collect(Collectors.toList())
            );
        }
        else {
            return null;
        }
    }
    
    //@Override
    public void onInitialize() {
        /*
        Registry.register(BuiltInRegistries.ITEM, id("portal_gun"), PORTAL_GUN);
        Registry.register(BuiltInRegistries.ITEM, id("portalgun_body"), PORTAL_GUN_BODY);
        Registry.register(BuiltInRegistries.ITEM, id("portalgun_claw"), PORTAL_GUN_CLAW);
        
        Registry.register(BuiltInRegistries.ENTITY_TYPE, id("custom_portal"), CustomPortal.entityType);
        
        Registry.register(BuiltInRegistries.SOUND_EVENT, PORTAL1_SHOOT, PORTAL1_SHOOT_EVENT);
        Registry.register(BuiltInRegistries.SOUND_EVENT, PORTAL2_SHOOT, PORTAL2_SHOOT_EVENT);
        Registry.register(BuiltInRegistries.SOUND_EVENT, PORTAL_OPEN, PORTAL_OPEN_EVENT);
        Registry.register(BuiltInRegistries.SOUND_EVENT, PORTAL_CLOSE, PORTAL_CLOSE_EVENT);
         */
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        SOUNDS.register(modEventBus);
        COMMAND_ARGUMENT_TYPES.register(modEventBus);
        
        PortalGunConfig.register();
        
        // disable block breaking hand swinging
        /*
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() == PORTAL_GUN.get()) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
         */
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, (PlayerInteractEvent.LeftClickBlock event) -> {
            ItemStack stack = event.getEntity().getItemInHand(event.getHand());
            if (stack.getItem() == PORTAL_GUN.get()) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
            }
        });
        
        // add into creative inventory
        /*
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(PORTAL_GUN);
        });
         */
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            PortalGunCommands.register(event.getDispatcher());
        });
    }
    
    
}
