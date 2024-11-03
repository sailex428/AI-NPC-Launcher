package io.sailex.aiNpcLauncher.client;

import io.sailex.aiNpcLauncher.client.commands.CommandManager;
import io.sailex.aiNpcLauncher.client.config.ModConfig;
import io.sailex.aiNpcLauncher.client.launcher.ClientLauncher;
import io.sailex.aiNpcLauncher.client.launcher.ClientProcessManager;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class AiNPCLauncher implements ClientModInitializer {

	public static final String MOD_ID = "ai-npc-launcher";
	private static final MinecraftClient client = MinecraftClient.getInstance();

	@Override
	public void onInitializeClient() {
		ModConfig.init();

		ClientProcessManager npcClientProcessManager = new ClientProcessManager();
		ClientLauncher clientLauncher = new ClientLauncher(npcClientProcessManager, client);

		CommandManager commandManager = new CommandManager(clientLauncher, npcClientProcessManager);
		commandManager.register();
	}
}
