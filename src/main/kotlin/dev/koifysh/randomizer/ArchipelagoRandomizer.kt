package dev.koifysh.randomizer

import com.google.common.collect.ImmutableSet
import com.google.gson.Gson
import dev.koifysh.randomizer.data.*
import dev.koifysh.randomizer.utils.TitleQueue
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

object ArchipelagoRandomizer : ModInitializer {

	const val MOD_ID = "archipelago-randomizer"

	val logger: Logger = LoggerFactory.getLogger(MOD_ID)
	
	lateinit var apmcData: APMCData private set
	lateinit var locationManager: APLocations private set
	lateinit var itemManager: APItems private set
	lateinit var goalManager: APGoals private set
	lateinit var apClient: APClient private set
	lateinit var server: MinecraftServer private set
	lateinit var archipelagoWorldData: ArchipelagoWorldData private set

	val validVersions: ImmutableSet<Int> = ImmutableSet.of(
		9, // mc 1.19
		10 // mc 1.21
	)

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		logger.info("Minecraft Archipelago 1.21 v0.1.3 Randomizer initializing.")

		// load apmc file
		val gson = Gson()
		try {
			val path = Paths.get("./APData/")
			if (!Files.exists(path)) {
				Files.createDirectories(path)
				logger.info("APData folder missing, creating.")
			}

			val files = checkNotNull(File(path.toUri()).listFiles { _: File?, name: String -> name.endsWith(".apmc") })
			Arrays.sort(files, Comparator.comparingLong { obj: File -> obj.lastModified() })
			val b64 = Files.readAllLines(files[0].toPath())[0]
			val json = String(Base64.getDecoder().decode(b64))
			apmcData = gson.fromJson(json, APMCData::class.java)
			if (!validVersions.contains(apmcData.clientVersion)) {
				apmcData.state = APMCData.State.INVALID_VERSION
			}

		} catch (e: IOException) {
			apmcData.state = APMCData.State.MISSING
		} catch (e: NullPointerException) {
			apmcData.state = APMCData.State.MISSING
		} catch (e: ArrayIndexOutOfBoundsException) {
			apmcData.state = APMCData.State.MISSING
		} catch (e: AssertionError) {
			apmcData.state = APMCData.State.MISSING
		}

		if (apmcData.state == APMCData.State.MISSING) {
			logger.error("no .apmc file found. please place .apmc file in './APData/' folder. ")
		}

		locationManager = APLocations(apmcData)

		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted)
		ServerTickEvents.END_SERVER_TICK.register(TitleQueue::onServerTick)
	}

	private fun onServerStarted(minecraftServer: MinecraftServer?) {
		server = minecraftServer!!
	    apClient = APClient(this, minecraftServer)
	}
}