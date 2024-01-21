package earth.terrarium.chipped;

import com.mojang.logging.LogUtils;
import earth.terrarium.chipped.common.network.NetworkHandler;
import earth.terrarium.chipped.common.registry.*;
import org.slf4j.Logger;

public class Chipped {
    public static final String MOD_ID = "chipped";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        NetworkHandler.init();

        ModBlocks.BLOCKS.init();
        ModItems.ITEMS.init();
        ModMenuTypes.MENUS.init();
        ModRecipeTypes.RECIPE_TYPES.init();
        ModRecipeSerializers.RECIPE_SERIALIZERS.init();
    }
}
