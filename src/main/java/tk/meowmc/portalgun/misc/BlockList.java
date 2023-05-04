package tk.meowmc.portalgun.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import qouteall.q_misc_util.my_util.LimitedLogger;
import tk.meowmc.portalgun.PortalGunMod;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record BlockList(
    List<String> list
) {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final LimitedLogger LIMITED_LOGGER = new LimitedLogger(100);
    
    public static BlockList fromTag(ListTag tag) {
        return new BlockList(PortalGunMod.listTagDeserialize(tag, StringTag::getAsString, StringTag.class));
    }
    
    public BiPredicate<Level, BlockPos> getWallPredicate() {
        if (list().isEmpty()) {
            // default predicate: only solid blocks
            return (w, p) -> w.getBlockState(p).isSolidRender(w, p);
        }
        
        Set<Block> allowedBlocks = asStream().collect(Collectors.toSet());
        
        return (w, p) -> allowedBlocks.contains(w.getBlockState(p).getBlock());
    }
    
    public ListTag toTag() {
        return PortalGunMod.listTagSerialize(list, StringTag::valueOf);
    }
    
    public Stream<Block> asStream() {
        return list.stream().flatMap(s -> parseBlockStr(s).stream());
    }
    
    public static Collection<Block> parseBlockStr(String str) {
        if (str.startsWith("#")) {
            TagKey<Block> tagKey = TagKey.create(
                Registries.BLOCK,
                new ResourceLocation(str.substring(1))
            );
            Optional<HolderSet.Named<Block>> named = BuiltInRegistries.BLOCK.getTag(tagKey);
            if (named.isEmpty()) {
                LIMITED_LOGGER.invoke(() -> {
                    LOGGER.error("Unknown block tag: {}", str);
                });
                return Collections.emptyList();
            }
            else {
                HolderSet.Named<Block> holderSet = named.get();
                return holderSet.stream().map(Holder::value).toList();
            }
        }
        else {
            Optional<Block> optional = BuiltInRegistries.BLOCK.getOptional(new ResourceLocation(str));
            if (optional.isPresent()) {
                return Collections.singletonList(optional.get());
            }
            else {
                LIMITED_LOGGER.invoke(() -> {
                    LOGGER.error("Unknown block: {}", str);
                });
                return Collections.emptyList();
            }
        }
    }
}
