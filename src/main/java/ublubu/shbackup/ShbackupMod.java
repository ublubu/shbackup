package ublubu.shbackup;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShbackupMod implements ModInitializer {
	public static final String MOD_ID = "shbackup";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Starting Shbackup by ublubu");

		var configHolder = AutoConfig.register(Config.class, JanksonConfigSerializer::new);
		var config = configHolder.get();

		var backupper = new Backupper(config);
		ServerTickEvents.END_SERVER_TICK.register(backupper::tick);
		ServerLifecycleEvents.SERVER_STOPPED.register(backupper::shutdown);
	}
}
