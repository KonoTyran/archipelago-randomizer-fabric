package dev.koifysh.mixin;

import dev.koifysh.randomizer.ArchipelagoRandomizer;
import dev.koifysh.randomizer.data.APMCData;
import net.minecraft.server.dedicated.Settings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.Properties;


@Mixin(Settings.class)
public abstract class MixinPropertyManager {

    @Inject(method = "loadFromFile(Ljava/nio/file/Path;)Ljava/util/Properties;", at = @At("RETURN"))
    private static void onLoadFromFile(Path pPath, CallbackInfoReturnable<Properties> cir) {
        ArchipelagoRandomizer.INSTANCE.getLogger().info("Injecting Archipelago Properties");
        Properties properties = cir.getReturnValue();
        APMCData data = ArchipelagoRandomizer.INSTANCE.getApmcData();
        if (data.getState() == APMCData.State.MISSING) {
            ArchipelagoRandomizer.INSTANCE.loadAPMC("./" + properties.getProperty("level-name") + "/");
            data = ArchipelagoRandomizer.INSTANCE.getApmcData();
        }
        if (data.getState() != APMCData.State.VALID) {
            properties.setProperty("level-seed", "");
            properties.setProperty("level-name", "world");
            properties.setProperty("generator-settings", "{\"biome\":\"minecraft:the_void\",\"layers\": [ { \"height\": 1, \"block\": \"minecraft:bedrock\"},{ \"height\": 126, \"block\": \"minecraft:dirt\"}, {\"height\":1, \"block\": \"minecraft:grass_block\"}]}");
            properties.setProperty("level-type", "minecraft:flat");
            return;
        }
        String worldName = "Archipelago-" + data.getSeedName() + "-P" + data.getPlayerID();


        properties.setProperty("level-seed", "" + data.getWorldSeed());
        properties.setProperty("spawn-protection", "0");
        properties.setProperty("level-name", worldName);
        properties.setProperty("level-type", "minecraft:normal");
        properties.setProperty("generator-settings", "{}");
        properties.setProperty("force-gamemode", "false");
        properties.setProperty("gamemode", "survival");

        if (data.getRace()) {
            ArchipelagoRandomizer.INSTANCE.getLogger().info("Archipelago race flag found enforcing race settings.");
            properties.setProperty("view-distance", "10");
            properties.setProperty("force-gamemode", "true");
        }


    }
}
