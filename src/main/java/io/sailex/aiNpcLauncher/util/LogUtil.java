package io.sailex.aiNpcLauncher.client.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogUtil {

	private static final Logger LOGGER = LogManager.getLogger(LogUtil.class);
	private static final String PREFIX = "[§5AI-NPC§f] ";

	public static void info(String message, ServerPlayerEntity player) {
		info(message, false, player);
	}

	public static void info(String message, boolean onlyInConsole, ServerPlayerEntity player) {
		String formattedMessage = PREFIX + message;
		if (onlyInConsole) {
			LOGGER.info(formattedMessage);
		} else {
			log(formattedMessage, player);
		}
	}

	public static void error(String message, ServerPlayerEntity player) {
		String formattedMessage = PREFIX + "§c" + message;
		log(formattedMessage, player);
	}

	private static void log(String formattedMessage, ServerPlayerEntity player) {
	  player.sendMessage(Text.of(formattedMessage));
	}
}
