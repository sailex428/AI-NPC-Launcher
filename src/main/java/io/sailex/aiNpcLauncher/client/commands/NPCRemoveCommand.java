package io.sailex.aiNpcLauncher.client.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.sailex.aiNpcLauncher.client.launcher.ClientProcessManager;
import io.sailex.aiNpcLauncher.client.util.LogUtil;
import lombok.AllArgsConstructor;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

@AllArgsConstructor
public class NPCRemoveCommand {

	private final ClientProcessManager clientProcessManager;

	public LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
		return literal("remove")
				.then(argument("name", StringArgumentType.string()).executes(this::removeNPC));
	}

	private int removeNPC(CommandContext<FabricClientCommandSource> context) {
		String name = StringArgumentType.getString(context, "name");

		LogUtil.info("Removing NPC with name: " + name);
		clientProcessManager.endProcess(name);
		return 1;
	}
}
