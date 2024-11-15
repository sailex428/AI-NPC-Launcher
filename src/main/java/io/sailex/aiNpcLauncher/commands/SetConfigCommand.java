package io.sailex.aiNpcLauncher.client.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.sailex.aiNpcLauncher.client.config.ModConfig;
import io.sailex.aiNpcLauncher.client.constants.ConfigConstants;
import io.sailex.aiNpcLauncher.client.util.LogUtil;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class SetConfigCommand {

	public void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(literal("setconfig")
				.requires(source -> source.hasPermissionLevel(2))
				.then(argument("key", StringArgumentType.word())
						.suggests((context, builder) -> {
							for (String key : ConfigConstants.ALLOWED_KEYS) {
								builder.suggest(key);
							}
							return builder.buildFuture();
						})
						.then(argument("value", StringArgumentType.string()).executes(this::setConfig))));
	}

	private int setConfig(CommandContext<FabricClientCommandSource> context) {
		String propertyKey = StringArgumentType.getString(context, "key");
		String propertyValue = StringArgumentType.getString(context, "value");

		if (!ConfigConstants.ALLOWED_KEYS.contains(propertyKey)) {
			LogUtil.error("Invalid property key!");
			return 0;
		}

		if (ModConfig.setProperty(propertyKey, propertyValue)) {
			LogUtil.info("Saved property successfully!");
			return 1;
		}

		LogUtil.error("Failed to save property!");
		return 0;
	}
}
