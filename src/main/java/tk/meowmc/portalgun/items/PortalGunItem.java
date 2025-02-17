package tk.meowmc.portalgun.items;

import me.Thelnfamous1.portalgun.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import qouteall.imm_ptl.core.McHelper;
import qouteall.imm_ptl.core.compat.GravityChangerInterface;
import qouteall.q_misc_util.Helper;
import qouteall.q_misc_util.my_util.IntBox;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.network.GeckoLibNetwork;
import software.bernie.geckolib3.network.ISyncable;
import software.bernie.geckolib3.util.GeckoLibUtil;
import tk.meowmc.portalgun.PortalGunMod;
import tk.meowmc.portalgun.PortalGunRecord;
import tk.meowmc.portalgun.client.renderer.PortalGunItemRenderer;
import tk.meowmc.portalgun.entities.CustomPortal;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class PortalGunItem extends Item implements IAnimatable, ISyncable, ColoredPortalGun {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final int COOLDOWN_TICKS = 4;
    public static final String CONTROLLER_NAME = "portalGunController";

    public final AnimationFactory cache = GeckoLibUtil.createFactory(this);

    private static final AnimationBuilder SHOOT_ANIM = new AnimationBuilder().addAnimation("portal_shoot", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    
    //private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
    public static final int ANIM_OPEN = 0;
    
    public PortalGunItem(Properties settings) {
        super(settings);

        GeckoLibNetwork.registerSyncable(this);
    }
    
    
    // Utilise our own render hook to define our custom renderer
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final PortalGunItemRenderer renderer = new PortalGunItemRenderer();
            
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return this.renderer;
            }

            @Override
            public HumanoidModel.@org.jetbrains.annotations.Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                if (!entityLiving.swinging && itemStack.getItem() instanceof PortalGunItem) {
                    return HumanoidModel.ArmPose.CROSSBOW_HOLD;
                }
                return null;
            }
        });
    }

    /*
    @Override
    public Supplier<Object> getRenderProvider() {
        return this.renderProvider;
    }
     */
    
    // Register our animation controllers
    @Override
    public void registerControllers(AnimationData controllers) {
        controllers.addAnimationController(
            new AnimationController<>(this, CONTROLLER_NAME, 1, state -> PlayState.CONTINUE)
                //.triggerableAnim("shoot_anim", SHOOT_ANIM)
        );
    }
    
    @Override
    public AnimationFactory getFactory() {
        return this.cache;
    }

    @Override
    public void onAnimationSync(int id, int state) {
        if (state == ANIM_OPEN) {
            final AnimationController<?> controller = GeckoLibUtil.getControllerForID(this.cache, id, CONTROLLER_NAME);
            if (controller.getAnimationState() == AnimationState.Stopped) {
                controller.markNeedsReload();
                controller.setAnimation(SHOOT_ANIM);
            }
        }
    }
    
    @Override
    public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player miner) {
        return false;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (world.isClientSide()) {
            return InteractionResultHolder.fail(itemStack);
        }
        
        boolean success = interact(
            (ServerPlayer) player, hand,
            PortalGunRecord.PortalGunSide.orange
        );
        
        if (success) {
            player.awardStat(Stats.ITEM_USED.get(this));
            
            // always fail, to cancel hand swing
            return InteractionResultHolder.fail(itemStack);
        }
        else {
            return InteractionResultHolder.fail(itemStack);
        }
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);
        
        tooltip.add(Component.translatable("item.portalgun.portal_gun_desc").withStyle(ChatFormatting.GOLD));
        this.addCustomPortalColorsTooltip(stack, world, tooltip, context);
    }
    
    public InteractionResult onAttack(
        Player player, Level world, InteractionHand hand
    ) {
        if (world.isClientSide()) {
            return InteractionResult.PASS;
        }
        
        boolean success = interact(
            (ServerPlayer) player, hand,
            PortalGunRecord.PortalGunSide.blue
        );
        
        if (success) {
            player.awardStat(Stats.ITEM_USED.get(this));
            
            return InteractionResult.SUCCESS;
        }
        else {
            return InteractionResult.FAIL;
        }
    }
    
    // return whether successful
    public boolean interact(
        ServerPlayer player,
        InteractionHand hand,
        PortalGunRecord.PortalGunSide side
    ) {
        ItemStack itemStack = player.getItemInHand(hand);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        
        PortalGunMod.PortalAwareRaytraceResult raytraceResult = PortalGunMod.portalAwareRayTrace(player, 100);
        
        if (raytraceResult == null) {
            return false;
        }
        
        BlockHitResult blockHit = raytraceResult.hitResult();
        ServerLevel world = ((ServerLevel) raytraceResult.world());
        Direction wallFacing = blockHit.getDirection();
    
        if (!checkAction(player, world)) {
            return false;
        }
        
        Validate.isTrue(blockHit.getType() == HitResult.Type.BLOCK);
        
        player.level.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            side == PortalGunRecord.PortalGunSide.blue ?
                PortalGunMod.PORTAL1_SHOOT_EVENT.get() : PortalGunMod.PORTAL2_SHOOT_EVENT.get(),
            SoundSource.PLAYERS,
            1.0F, 1.0F
        );
        
        PortalGunRecord.PortalGunKind kind = PortalGunRecord.PortalGunKind._2x1;
        
        PortalPlacement placement = findPortalPlacement(player, kind, raytraceResult);
        
        if (placement == null) {
            return false;
        }
        
        Direction rightDir = placement.rotation.transformedX;
        Direction upDir = placement.rotation.transformedY;

        /*
        triggerAnim(
            player,
            GeoItem.getOrAssignId(player.getItemInHand(hand), ((ServerLevel) player.level)),
            "portalGunController", "shoot_anim"
        );
         */
        final int id = GeckoLibUtil.guaranteeIDForStack(itemStack, player.getLevel());
        final PacketDistributor.PacketTarget target = PacketDistributor.TRACKING_ENTITY_AND_SELF
                .with(() -> player);
        GeckoLibNetwork.syncAnimation(target, this, id, ANIM_OPEN);

        PortalGunRecord record = PortalGunRecord.get();
        
        PortalGunRecord.PortalDescriptor descriptor =
            new PortalGunRecord.PortalDescriptor(player.getUUID(), kind, side);
        
        PortalGunRecord.PortalDescriptor otherSideDescriptor = descriptor.getTheOtherSide();
        
        PortalGunRecord.PortalInfo thisSideInfo = record.data.get(descriptor);
        PortalGunRecord.PortalInfo otherSideInfo = record.data.get(otherSideDescriptor);
        
        Vec3 wallFacingVec = Vec3.atLowerCornerOf(wallFacing.getNormal());
        Vec3 newPortalOrigin = Helper
            .getBoxSurface(placement.areaBox.toRealNumberBox(), wallFacing.getOpposite())
            .getCenter()
            .add(wallFacingVec.scale(PortalGunMod.portalOffset));
        
        CustomPortal portal = null;
        boolean isExistingPortal = false;
        
        if (thisSideInfo != null) {
            Entity entity = world.getEntity(thisSideInfo.portalId());
            if (entity instanceof CustomPortal customPortal) {
                portal = customPortal;
                isExistingPortal = true;
            }
        }
        
        if (portal == null) {
            portal = PortalGunMod.CUSTOM_PORTAL.get().create(world);
            Validate.notNull(portal);
        }

        ColoredPortalGun.colorPortal(portal, itemStack, side);
        
        portal.setOriginPos(newPortalOrigin);
        portal.setOrientationAndSize(
            Vec3.atLowerCornerOf(rightDir.getNormal()),
            Vec3.atLowerCornerOf(upDir.getNormal()),
            kind.getWidth() * 0.9,
            kind.getHeight() * 0.9
        );
        portal.descriptor = descriptor;
        portal.wallBox = placement.wallBox;
        portal.airBox = placement.areaBox;
        portal.thisSideUpdateCounter = thisSideInfo == null ? 0 : thisSideInfo.updateCounter();
        portal.otherSideUpdateCounter = otherSideInfo == null ? 0 : otherSideInfo.updateCounter();
        PortalManipulationHelper.makePortalRound(portal, 20);
        PortalHelper.disableDefaultAnimation(portal);
        
        if (otherSideInfo == null) {
            // it's unpaired, invisible and not teleportable
            portal.setDestinationDimension(world.dimension());
            portal.setDestination(newPortalOrigin.add(0, 10, 0));
            portal.setIsVisible(false);
            portal.teleportable = false;
        }
        else {
            // it's linked
            portal.setDestinationDimension(otherSideInfo.portalDim());
            portal.setDestination(otherSideInfo.portalPos());
            PortalHelper.setOtherSideOrientation(portal, otherSideInfo.portalOrientation());
            portal.setIsVisible(true);
            portal.teleportable = true;
            player.level.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                PortalGunMod.PORTAL_OPEN_EVENT.get(),
                SoundSource.PLAYERS,
                1.0F, 1.0F
            );
        }
        
        portal.thisSideUpdateCounter += 1;
        thisSideInfo = new PortalGunRecord.PortalInfo(
            portal.getUUID(),
            world.dimension(),
            newPortalOrigin,
            portal.getOrientationRotation(),
            portal.thisSideUpdateCounter
        );
        record.data.put(descriptor, thisSideInfo);
        record.setDirty();
        
        if (!isExistingPortal) {
            McHelper.spawnServerEntity(portal);
        }
        else {
            portal.reloadAndSyncToClient();
        }
        
        return true;
    }
    
    private static boolean checkAction(ServerPlayer player, ServerLevel world) {
        // NOTE ImmPtl does not initialize ender dragon information immediately when
        // opening end portal. This make `hasPreviouslyKilledDragon` return true if no player entered the end before.
        // Is this a vanilla bug?
        // This should be fixed in later ImmPtl versions.
        if (world.dimension() == Level.END) {
            EndDragonFight endDragonFight = world.dragonFight();
            if (endDragonFight != null) {
                if (!endDragonFight.hasPreviouslyKilledDragon()) {
                    player.displayClientMessage(
                        Component.literal("Using portal gun in end before killing any ender dragon is not allowed"),
                        true
                    );
                    return false;
                }
            }
        }
        return true;
    }

    private static record PortalPlacement(
        AARotation rotation,
        IntBox areaBox,
        IntBox wallBox
    ) {
    }
    
    @Nullable
    private static PortalPlacement findPortalPlacement(
        ServerPlayer player,
        PortalGunRecord.PortalGunKind kind,
        PortalGunMod.PortalAwareRaytraceResult raytraceResult
    ) {
        BlockHitResult blockHit = raytraceResult.hitResult();
        ServerLevel world = ((ServerLevel) raytraceResult.world());
        BlockPos interactingAirPos = blockHit.getBlockPos().relative(blockHit.getDirection());
        Direction wallFacing = blockHit.getDirection();
        
        Direction playerGravity = GravityChangerInterface.invoker.getGravityDirection(player);
        Direction transformedGravity = raytraceResult.portalsPassingThrough().stream().reduce(
            playerGravity,
            (gravity, portal) -> PortalHelper.getTeleportedGravityDirection(portal, gravity),
            (g1, g2) -> {throw new RuntimeException();}
        );
        Vec3 viewVector = player.getViewVector(1);
        Vec3 transformedViewVector = raytraceResult.portalsPassingThrough().stream().reduce(
            viewVector,
            (v, portal) -> portal.transformLocalVec(v),
            (v1, v2) -> {throw new RuntimeException();}
        );
        
        Vec3 viewVectorLocal = GravityChangerInterface.invoker
            .transformWorldToPlayer(transformedGravity, transformedViewVector);
        
        Direction wallFacingLocal = GCIInvokerHelper.transformDirWorldToPlayer(GravityChangerInterface.invoker,
            transformedGravity, wallFacing);
        
        Direction[] upDirCandidates = Helper.getAnotherFourDirections(wallFacingLocal.getAxis());
        
        Arrays.sort(upDirCandidates, Comparator.comparingDouble((Direction dir) -> {
            if (dir == Direction.UP) {
                // the local up direction has the highest priority
                return 1;
            }
            // horizontal dot product
            return dir.getNormal().getX() * viewVectorLocal.x + dir.getNormal().getZ() * viewVectorLocal.z;
        }).reversed());
        
        BlockPos portalAreaSize = new BlockPos(kind.getWidth(), kind.getHeight(), 1);
        
        for (Direction upDir : upDirCandidates) {
            AARotation rot = AARotation.getAARotationFromYZ(upDir, wallFacing);
            BlockPos transformedSize = rot.transform(portalAreaSize);
            IntBox portalArea = IntBoxHelper.getBoxByPosAndSignedSize(interactingAirPos, transformedSize);
            IntBox wallArea = portalArea.getMoved(wallFacing.getOpposite().getNormal());
            
            if (PortalGunMod.isAreaClear(world, portalArea) &&
                PortalGunMod.isWallValid(world, wallArea) &&
                !portalExistsInArea(world, wallArea, wallFacing)
            ) {
                return new PortalPlacement(rot, portalArea, wallArea);
            }
        }
        
        return null;
    }
    
    private static boolean portalExistsInArea(Level world, IntBox wallArea, Direction wallFacing) {
        List<CustomPortal> portals = McHelper.findEntitiesByBox(
            CustomPortal.class,
            world,
            wallArea.toRealNumberBox().inflate(0.1),
                IPGlobalHelper.maxNormalPortalRadius,
            p -> PortalHelper.getApproximateFacingDirection(p) == wallFacing
                && IntBox.getIntersect(p.wallBox, wallArea) != null
        );
        return !portals.isEmpty();
    }
}
