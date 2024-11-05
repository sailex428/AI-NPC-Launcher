package io.sailex.aiNpcLauncher.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class LogUtil {

	private static final String PREFIX = "[§5AI-NPC§f] ";
	private static final MinecraftClient client = MinecraftClient.getInstance();

	public static void info(String message) {
		String formattedMessage = PREFIX + message;
		log(formattedMessage);
	}

	public static void error(String message) {
		String formattedMessage = PREFIX + "§c" + message;
		log(formattedMessage);
	}

	private static void log(String formattedMessage) {
		client.inGameHud.getChatHud().addMessage(Text.of(formattedMessage));
	}
}
