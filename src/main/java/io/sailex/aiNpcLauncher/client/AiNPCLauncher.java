package io.sailex.aiNpcLauncher.client;

import io.sailex.aiNpcLauncher.client.commands.CommandManager;
import io.sailex.aiNpcLauncher.client.config.ModConfig;
import io.sailex.aiNpcLauncher.client.launcher.ClientLauncher;
import io.sailex.aiNpcLauncher.client.launcher.ClientProcessManager;
import net.fabricmc.api.ClientModInitializer;

public class AiNPCLauncher implements ClientModInitializer {

	public static final String MOD_ID = "ai-npc-launcher";

	@Override
	public void onInitializeClient() {
		ModConfig.init();

		ClientProcessManager npcClientProcessManager = new ClientProcessManager();
		npcClientProcessManager.registerEndProcessOnDisconnect();

		ClientLauncher clientLauncher = new ClientLauncher(npcClientProcessManager);

		CommandManager commandManager = new CommandManager(clientLauncher, npcClientProcessManager);
		commandManager.register();
	}
}
