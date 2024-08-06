package earth.terrarium.chipped.datagen;

import earth.terrarium.chipped.Chipped;
import earth.terrarium.chipped.datagen.provider.client.ModBlockStateProvider;
import earth.terrarium.chipped.datagen.provider.client.ModItemModelProvider;
import earth.terrarium.chipped.datagen.provider.client.ModLangProvider;
import earth.terrarium.chipped.datagen.provider.server.ModBlockTagProvider;
import earth.terrarium.chipped.datagen.provider.server.ModItemTagProvider;
import earth.terrarium.chipped.datagen.provider.server.ModLootTableProvider;
import earth.terrarium.chipped.datagen.provider.server.ModRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = Chipped.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ChippedDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        // Client
        generator.addProvider(new ModBlockStateProvider(generator, existingFileHelper));
        generator.addProvider(new ModItemModelProvider(generator, existingFileHelper));
        generator.addProvider(new ModLangProvider(generator));

        // Server
        generator.addProvider(new ModLootTableProvider(generator));
        generator.addProvider(new ModRecipeProvider(generator));
        generator.addProvider(new ModBlockTagProvider(generator, existingFileHelper));
        generator.addProvider(new ModItemTagProvider(generator, existingFileHelper));

    }
}
