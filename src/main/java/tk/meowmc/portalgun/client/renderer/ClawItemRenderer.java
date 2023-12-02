package tk.meowmc.portalgun.client.renderer;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import tk.meowmc.portalgun.PortalGunMod;
import tk.meowmc.portalgun.items.ClawItem;

public class ClawItemRenderer extends GeoItemRenderer<ClawItem> {

    public static final ResourceLocation MODEL = new ResourceLocation(PortalGunMod.MODID, "geo/item/portalgun_claw.geo.json");
    public static final ResourceLocation TEXTURE = new ResourceLocation(PortalGunMod.MODID, "textures/item/portalgun.png");
    public static final ResourceLocation ANIMATION = new ResourceLocation(PortalGunMod.MODID, "animations/item/portalgun_claw.animation.json");

    public ClawItemRenderer() {
        super(
                new AnimatedGeoModel<>() {
                    @Override
                    public ResourceLocation getModelResource(ClawItem clawItem) {
                        return MODEL;
                    }

                    @Override
                    public ResourceLocation getTextureResource(ClawItem clawItem) {
                        return TEXTURE;
                    }

                    @Override
                    public ResourceLocation getAnimationResource(ClawItem clawItem) {
                        return ANIMATION;
                    }
                }
        );
    }
}
