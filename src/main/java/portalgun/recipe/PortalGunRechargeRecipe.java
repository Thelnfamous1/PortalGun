package portalgun.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import portalgun.PortalGunMod;
import portalgun.config.PortalGunConfig;
import portalgun.items.PortalGunItem;
import portalgun.misc.BlockList;

import java.util.Collection;

public class PortalGunRechargeRecipe extends CustomRecipe {
    public static final RecipeSerializer<PortalGunRechargeRecipe> SERIALIZER =
        new SimpleCraftingRecipeSerializer<>(PortalGunRechargeRecipe::new);
    
    public static void init() {
        Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            new ResourceLocation("portalgun:portal_gun_recharge"),
            SERIALIZER
        );
    }
    
    public PortalGunRechargeRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }
    
    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return container.countItem(PortalGunMod.PORTAL_GUN) == 1 &&
            container.countItem(Items.NETHER_STAR) == 1;
    }
    
    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack portalGun =
            container.getItems().stream().filter(i -> i.getItem() == PortalGunMod.PORTAL_GUN)
                .findFirst().orElse(null);
        
        if (portalGun == null) {
            return ItemStack.EMPTY;
        }
        
        PortalGunItem.ItemInfo itemInfo = PortalGunItem.ItemInfo.fromTag(portalGun.getOrCreateTag());
        itemInfo.remainingEnergy = itemInfo.maxEnergy;
        return itemInfo.toStack();
    }
    
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
    
    // MC's recipe book is not designed for dynamic recipes
    // In ServerPlaceRecipe#addItemToSlot, it uses the item id to recreate the default item stack
    // In Inventory#findSlotMatchingUnusedItem, it cannot pick items with custom nbt
    // So the recharging recipe cannot work with recipe book
    
//    /**
//     * Make it to be in the recipe book.
//     * {@link net.minecraft.stats.ServerRecipeBook#addRecipes(Collection, ServerPlayer)}
//     */
//    @Override
//    public boolean isSpecial() {
//        return false;
//    }
//
//    @Override
//    public NonNullList<Ingredient> getIngredients() {
//        NonNullList<Ingredient> ingredients = NonNullList.withSize(
//            2, Ingredient.EMPTY
//        );
//
//        ingredients.set(0, Ingredient.of(
//            new PortalGunItem.ItemInfo(
//                BlockList.createDefault(),
//                0,
//                PortalGunConfig.get().maxEnergy
//            ).toStack()
//        ));
//        ingredients.set(1, Ingredient.of(
//            new ItemStack(Items.NETHER_STAR)
//        ));
//
//        return ingredients;
//    }
//
//    @Override
//    public ItemStack getResultItem(RegistryAccess registryAccess) {
//        return new PortalGunItem.ItemInfo(
//            BlockList.createDefault(),
//            PortalGunConfig.get().maxEnergy,
//            PortalGunConfig.get().maxEnergy
//        ).toStack();
//    }
}
