package com.one_studio

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager
import net.minecraft.text.Text
import org.slf4j.LoggerFactory

object MidnightAssisit : ModInitializer {
    private val logger = LoggerFactory.getLogger("midnight-assisit")

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		logger.info("Hello Fabric world!")

		CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
			dispatcher.register(
				CommandManager.literal("midnight")
					.executes { context ->
						context.source.sendFeedback({ Text.literal("Hello from Midnight Assisit!") }, false)
						1
					}
			)
		}
	}
}
