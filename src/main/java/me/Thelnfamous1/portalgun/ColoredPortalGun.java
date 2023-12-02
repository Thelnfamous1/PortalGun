package me.Thelnfamous1.portalgun;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import tk.meowmc.portalgun.PortalGunRecord;
import tk.meowmc.portalgun.entities.CustomPortal;

public interface ColoredPortalGun {

    String CUSTOM_PORTAL_COLORS_TAG = "CustomPortalColors";

    static void colorPortalFromGun(CustomPortal portal, ItemStack portalGun, PortalGunRecord.PortalGunSide side){
        if(portalGun.getItem() instanceof ColoredPortalGun cpg && cpg.hasCustomColorForSide(portalGun, side)){
            portal.setCustomPortalColor(cpg.getColorForSide(portalGun, side));
        }
    }

    default boolean hasCustomColorForSide(ItemStack portalGun, PortalGunRecord.PortalGunSide side) {
        CompoundTag customColor = portalGun.getTagElement(CUSTOM_PORTAL_COLORS_TAG);
        return customColor != null && customColor.contains(side.name(), 99);
    }

    default int getColorForSide(ItemStack portalGun, PortalGunRecord.PortalGunSide side) {
        CompoundTag display = portalGun.getTagElement(CUSTOM_PORTAL_COLORS_TAG);
        return display != null && display.contains(side.name(), Tag.TAG_ANY_NUMERIC) ? display.getInt(side.name()) : side.getColorInt();
    }

    default void setColorForSide(ItemStack portalGun, int pColor, PortalGunRecord.PortalGunSide side) {
        portalGun.getOrCreateTagElement(CUSTOM_PORTAL_COLORS_TAG).putInt(side.name(), pColor);
    }

    default void clearColorForSide(ItemStack portalGun, PortalGunRecord.PortalGunSide side) {
        CompoundTag display = portalGun.getTagElement(CUSTOM_PORTAL_COLORS_TAG);
        if (display != null && display.contains(side.name())) {
            display.remove(side.name());
        }
    }
}
