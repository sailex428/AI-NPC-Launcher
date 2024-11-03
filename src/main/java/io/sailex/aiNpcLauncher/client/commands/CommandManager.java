package io.sailex.aiNpcLauncher.client.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import io.sailex.aiNpcLauncher.client.launcher.ClientLauncher;
import io.sailex.aiNpcLauncher.client.launcher.ClientProcessManager;
import lombok.AllArgsConstructor;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

@AllArgsConstructor
public class CommandManager {

	private final ClientLauncher clientLauncher;
	private final ClientProcessManager clientProcessManager;

	public void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			new SetConfigCommand().register(dispatcher);
			dispatcher.register(literal("npc")
					.requires(source -> source.hasPermissionLevel(2))
					.then(new NPCCreateCommand(clientLauncher).getCommand())
					.then(new NPCRemoveCommand(clientProcessManager).getCommand()));
		});
	}
}
