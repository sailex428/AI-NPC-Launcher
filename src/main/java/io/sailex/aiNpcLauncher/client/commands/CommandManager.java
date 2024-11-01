package io.sailex.aiNpcLauncher.client.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import io.sailex.aiNpcLauncher.client.launcher.NPCClientLauncher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class CommandManager {

	private final NPCClientLauncher npcClientLauncher;

	public CommandManager(NPCClientLauncher npcClientLauncher) {
		this.npcClientLauncher = npcClientLauncher;
	}

	public void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			new SetConfigCommand().register(dispatcher);
			dispatcher.register(literal("npc")
					.requires(source -> source.hasPermissionLevel(2))
					.then(new NPCCreateCommand(npcClientLauncher).getCommand())
					.then(new NPCRemoveCommand().getCommand()));
		});
	}
}
