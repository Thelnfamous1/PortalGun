package tk.meowmc.portalgun;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import qouteall.q_misc_util.api.McRemoteProcedureCall;
import qouteall.q_misc_util.my_util.IntBox;
import tk.meowmc.portalgun.config.PortalGunConfig;
import tk.meowmc.portalgun.entities.CustomPortal;
import tk.meowmc.portalgun.items.ClawItem;
import tk.meowmc.portalgun.items.PortalGunItem;
import tk.meowmc.portalgun.misc.BlockList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PortalGunMod implements ModInitializer {
    public static final String MODID = "portalgun";
    public static final String KEY = MODID + ":portalgun_portals";
    public static final String MOD_NAME = "PortalGun Mod";
    
    public static final double portalOffset = 0.001;
    public static final double portalOverlayOffset = 0.001;
    
    
    public static final PortalGunItem PORTAL_GUN = new PortalGunItem(new FabricItemSettings().fireResistant().stacksTo(1).rarity(Rarity.EPIC));
    public static final Item PORTAL_GUN_BODY = new Item(new FabricItemSettings().fireResistant().stacksTo(1).rarity(Rarity.RARE));
    public static final ClawItem PORTAL_GUN_CLAW = new ClawItem(new FabricItemSettings().fireResistant().stacksTo(1).rarity(Rarity.RARE));
    
    public static final ResourceLocation PORTAL1_SHOOT = new ResourceLocation("portalgun:portal1_shoot");
    public static final ResourceLocation PORTAL2_SHOOT = new ResourceLocation("portalgun:portal2_shoot");
    public static final ResourceLocation PORTAL_OPEN = new ResourceLocation("portalgun:portal_open");
    public static final ResourceLocation PORTAL_CLOSE = new ResourceLocation("portalgun:portal_close");
    
    public static final SoundEvent PORTAL1_SHOOT_EVENT = SoundEvent.createVariableRangeEvent(PORTAL1_SHOOT);
    public static final SoundEvent PORTAL2_SHOOT_EVENT = SoundEvent.createVariableRangeEvent(PORTAL2_SHOOT);
    public static final SoundEvent PORTAL_OPEN_EVENT = SoundEvent.createVariableRangeEvent(PORTAL_OPEN);
    public static final SoundEvent PORTAL_CLOSE_EVENT = SoundEvent.createVariableRangeEvent(PORTAL_CLOSE);
    
    public static final Logger LOGGER = LogManager.getLogger();
    
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
    
    public static boolean isAreaClear(Level world, IntBox airBox1) {
        return airBox1.fastStream().allMatch(p -> world.getBlockState(p).isAir());
    }
    
    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.ITEM, id("portal_gun"), PORTAL_GUN);
        Registry.register(BuiltInRegistries.ITEM, id("portalgun_body"), PORTAL_GUN_BODY);
        Registry.register(BuiltInRegistries.ITEM, id("portalgun_claw"), PORTAL_GUN_CLAW);
        
        Registry.register(BuiltInRegistries.ENTITY_TYPE, id("custom_portal"), CustomPortal.entityType);
        
        Registry.register(BuiltInRegistries.SOUND_EVENT, PORTAL1_SHOOT, PORTAL1_SHOOT_EVENT);
        Registry.register(BuiltInRegistries.SOUND_EVENT, PORTAL2_SHOOT, PORTAL2_SHOOT_EVENT);
        Registry.register(BuiltInRegistries.SOUND_EVENT, PORTAL_OPEN, PORTAL_OPEN_EVENT);
        Registry.register(BuiltInRegistries.SOUND_EVENT, PORTAL_CLOSE, PORTAL_CLOSE_EVENT);
        
        PortalGunConfig.register();
        
        // add into creative inventory
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(PORTAL_GUN);
    
            ItemStack stack = new ItemStack(PORTAL_GUN);
            PortalGunItem.ItemInfo itemInfo = new PortalGunItem.ItemInfo(
                new BlockList(List.of("minecraft:quartz_block"))
            );
            stack.setTag(itemInfo.toTag());
            entries.accept(stack);
        });
        
        ClientPreAttackCallback.EVENT.register(new ClientPreAttackCallback() {
            @Override
            public boolean onClientPlayerPreAttack(Minecraft client, LocalPlayer player, int clickCount) {
                ItemStack mainHandItem = player.getMainHandItem();
                
                if (mainHandItem.getItem() == PortalGunMod.PORTAL_GUN) {
                    
                    ItemCooldowns cooldowns = player.getCooldowns();
                    float cooldownPercent = cooldowns.getCooldownPercent(PortalGunMod.PORTAL_GUN, 0);
                    
                    if (cooldownPercent < 0.001) {
                        McRemoteProcedureCall.tellServerToInvoke(
                            "tk.meowmc.portalgun.misc.RemoteCallables.onClientLeftClickPortalGun"
                        );
                    }
                    
                    return true;
                }
                
                return false;
            }
        });
    }
}
