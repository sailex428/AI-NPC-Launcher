package io.sailex.aiNpcLauncher.client;

import io.sailex.aiNpcLauncher.client.commands.CommandManager;
import io.sailex.aiNpcLauncher.client.launcher.NPCClientLauncher;
import net.fabricmc.api.ClientModInitializer;

public class AiNPCLauncher implements ClientModInitializer {

	public static final String MOD_ID = "ai-npc-launcher";

	@Override
	public void onInitializeClient() {
		NPCClientLauncher npcClientLauncher = new NPCClientLauncher();

		CommandManager commandManager = new CommandManager(npcClientLauncher);
		commandManager.register();
	}
}
