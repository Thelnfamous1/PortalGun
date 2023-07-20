package tk.meowmc.portalgun.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import qouteall.imm_ptl.core.block_manipulation.BlockManipulationClient;
import qouteall.q_misc_util.api.McRemoteProcedureCall;
import tk.meowmc.portalgun.PortalGunMod;

@Mixin(value = BlockManipulationClient.class, remap = false)
public class MixinImmPtlBlockManipulationClient {

}
