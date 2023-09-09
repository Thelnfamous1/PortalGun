package portalgun.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import qouteall.q_misc_util.api.McRemoteProcedureCall;
import portalgun.PortalGunMod;
import portalgun.client.renderer.CustomPortalEntityRenderer;
import portalgun.client.renderer.models.PortalOverlayModel;
import portalgun.entities.CustomPortal;

import static portalgun.PortalGunMod.id;

@Environment(EnvType.CLIENT)
public class PortalgunClient implements ClientModInitializer {
    public static final ModelLayerLocation OVERLAY_MODEL_LAYER = new ModelLayerLocation(id("portal_overlay"), "main");
    
    @Override
    public void onInitializeClient() {
        KeyMapping clearPortals = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.portalgun.clearportals", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.portalgun"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (clearPortals.consumeClick()) {
                McRemoteProcedureCall.tellServerToInvoke("portalgun.misc.RemoteCallables.onClientClearPortalGun");
            }
        });
        
        EntityModelLayerRegistry.registerModelLayer(OVERLAY_MODEL_LAYER, PortalOverlayModel::getTexturedModelData);
        EntityRendererRegistry.register(CustomPortal.entityType, CustomPortalEntityRenderer::new);
        
        ClientPreAttackCallback.EVENT.register(new ClientPreAttackCallback() {
            @Override
            public boolean onClientPlayerPreAttack(Minecraft client, LocalPlayer player, int clickCount) {
                ItemStack mainHandItem = player.getMainHandItem();
                
                if (mainHandItem.getItem() == PortalGunMod.PORTAL_GUN) {
                    
                    ItemCooldowns cooldowns = player.getCooldowns();
                    float cooldownPercent = cooldowns.getCooldownPercent(PortalGunMod.PORTAL_GUN, 0);
                    
                    if (cooldownPercent < 0.001) {
                        McRemoteProcedureCall.tellServerToInvoke(
                            "portalgun.misc.RemoteCallables.onClientLeftClickPortalGun"
                        );
                    }
                    
                    return true;
                }
                
                return false;
            }
        });
    }
}
