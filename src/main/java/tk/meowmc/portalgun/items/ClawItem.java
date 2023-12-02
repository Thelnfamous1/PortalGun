package tk.meowmc.portalgun.items;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.network.GeckoLibNetwork;
import software.bernie.geckolib3.network.ISyncable;
import software.bernie.geckolib3.util.GeckoLibUtil;
import tk.meowmc.portalgun.client.renderer.ClawItemRenderer;

import java.util.function.Consumer;

public class ClawItem extends Item implements IAnimatable, ISyncable {
    
    public AnimationFactory cache = GeckoLibUtil.createFactory(this);
    
    //private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public ClawItem(Properties settings) {
        super(settings);


        GeckoLibNetwork.registerSyncable(this);
    }
    
    // Utilise our own render hook to define our custom renderer
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final ClawItemRenderer renderer = new ClawItemRenderer();
            
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return this.renderer;
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
            new AnimationController<>(
                this, "clawController", 1, state -> PlayState.CONTINUE
            )
        );
    }
    
    @Override
    public AnimationFactory getFactory() {
        return this.cache;
    }

    @Override
    public void onAnimationSync(int id, int state) {

    }
}
