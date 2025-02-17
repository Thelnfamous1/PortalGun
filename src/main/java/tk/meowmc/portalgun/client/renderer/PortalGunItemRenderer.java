package tk.meowmc.portalgun.client.renderer;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import tk.meowmc.portalgun.PortalGunMod;
import tk.meowmc.portalgun.items.PortalGunItem;

public class PortalGunItemRenderer extends GeoItemRenderer<PortalGunItem> {

    public static final ResourceLocation MODEL = PortalGunMod.id("geo/item/portalgun.geo.json");
    public static final ResourceLocation TEXTURE = PortalGunMod.id("textures/item/portalgun.png");
    public static final ResourceLocation ANIMATION = PortalGunMod.id("animations/item/portalgun.animation.json");

    public PortalGunItemRenderer() {
        super(new AnimatedGeoModel<>() {
            @Override
            public ResourceLocation getModelResource(PortalGunItem portalGunItem) {
                return MODEL;
            }

            @Override
            public ResourceLocation getTextureResource(PortalGunItem portalGunItem) {
                return TEXTURE;
            }

            @Override
            public ResourceLocation getAnimationResource(PortalGunItem portalGunItem) {
                return ANIMATION;
            }
        });
    }
}
