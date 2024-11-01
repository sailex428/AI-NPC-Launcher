package io.sailex.aiNpcLauncher.client.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.AllArgsConstructor;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

@AllArgsConstructor
public class NPCRemoveCommand {

	public LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
		return literal("remove")
				.then(argument("name", StringArgumentType.string()).executes(this::removeNPC));
	}

	private int removeNPC(CommandContext<FabricClientCommandSource> context) {
		String name = StringArgumentType.getString(context, "name");

		context.getSource().sendFeedback(Text.of("Removing NPC with name: " + name));
		return 1;
	}
}
