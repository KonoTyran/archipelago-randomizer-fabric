package dev.koifysh.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.koifysh.randomizer.ArchipelagoRandomizer;
import dev.koifysh.randomizer.data.APMCData;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.server.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

@Mixin(Main.class)
public class MixinModInit {


    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;startTimerHackThread()V", shift = At.Shift.AFTER))
    private static void afterModInit(String[] strings, CallbackInfo ci, @Local OptionSet optionSet, @Local(ordinal = 10) OptionSpec<String> worldOption) {
        ArchipelagoRandomizer.INSTANCE.loadAPMC("APData/");
        if (ArchipelagoRandomizer.INSTANCE.getApmcData().getState() == APMCData.State.VALID) {
            var data = ArchipelagoRandomizer.INSTANCE.getApmcData();
            var apmcFile = Paths.get(String.valueOf(data.getFileName())).toFile();
            String worldName = "Archipelago-" + data.getSeedName() + "-P" + data.getPlayerID();
            File newFileName = Paths.get(worldName, "save.apmc").toFile();
            try {
                newFileName.getParentFile().mkdirs();
                apmcFile.renameTo(newFileName);
                ArchipelagoRandomizer.INSTANCE.getLogger().info("Moved APMC file to " + newFileName.getAbsolutePath());
            } catch (Exception e) {
                ArchipelagoRandomizer.INSTANCE.getLogger().error("Failed to move APMC file");
            }
            return;
        }

        var world = Optional.ofNullable(optionSet.valueOf(worldOption));
        world.ifPresent(ArchipelagoRandomizer.INSTANCE::loadAPMC);
    }

    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/Bootstrap;bootStrap()V"))
    private static void beforeModInit(String[] strings, CallbackInfo ci) {
        ArchipelagoRandomizer.INSTANCE.getLogger().info("pre-init setup");
    }
}
