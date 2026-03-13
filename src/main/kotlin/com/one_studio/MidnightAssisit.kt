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
        
        MidnightAssisitConfig.load()

		CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
			dispatcher.register(
				CommandManager.literal("midnight")
					.then(CommandManager.literal("reload")
						.executes { context ->
							MidnightAssisitConfig.load()
							context.source.sendFeedback({ Text.literal("§7Midnight Assisit: §aConfig reloaded!") }, false)
							1
						}
					)
					.then(CommandManager.literal("toggle")
						.executes { context ->
							MidnightAssisitConfig.data.globalEnabled = !MidnightAssisitConfig.data.globalEnabled
							MidnightAssisitConfig.save()
							val status = if (MidnightAssisitConfig.data.globalEnabled) "§aON" else "§cOFF"
							context.source.sendFeedback({ Text.literal("§7Midnight Assisit: $status") }, false)
							1
						}
					)
					.executes { context ->
						context.source.sendFeedback({ Text.literal("§7Midnight Assisit: Use §f/midnight reload §7or §f/midnight toggle") }, false)
						1
					}
			)
		}
	}
}
