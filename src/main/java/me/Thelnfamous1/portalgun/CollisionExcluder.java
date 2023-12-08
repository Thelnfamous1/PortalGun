package me.Thelnfamous1.portalgun;

import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * @author iPortalTeam
 */
public interface CollisionExcluder {

    VoxelShape getThisSideCollisionExclusion();
}
