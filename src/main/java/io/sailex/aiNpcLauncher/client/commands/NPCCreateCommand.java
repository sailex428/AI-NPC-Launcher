package io.sailex.aiNpcLauncher.client.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.sailex.aiNpcLauncher.client.config.ModConfig;
import io.sailex.aiNpcLauncher.client.constants.ConfigConstants;
import io.sailex.aiNpcLauncher.client.launcher.ClientLauncher;
import io.sailex.aiNpcLauncher.client.util.LogUtil;
import java.util.Set;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class NPCCreateCommand {

	private static final String LLM_TYPE = "llm-type";
	private static final String LLM_MODEL = "llm-model";

	private static final Set<String> allowedLLMTypes = Set.of("ollama", "openai");

	private final ClientLauncher clientLauncher;

	public NPCCreateCommand(ClientLauncher clientLauncher) {
		this.clientLauncher = clientLauncher;
	}

	public LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
		return literal("add")
				.then(argument("name", StringArgumentType.string())
						.then(argument("isOffline", BoolArgumentType.bool())
								.executes(this::createNPC)
								.then(argument(LLM_TYPE, StringArgumentType.string())
										.suggests((context, builder) -> {
											for (String llmType : allowedLLMTypes) {
												builder.suggest(llmType);
											}
											return builder.buildFuture();
										})
										.then(argument(LLM_MODEL, StringArgumentType.string())
												.executes(this::createNPCWithLLM)))));
	}

	private int createNPC(CommandContext<FabricClientCommandSource> context) {
		String name = StringArgumentType.getString(context, "name");
		boolean isOffline = BoolArgumentType.getBool(context, "isOffline");

		LogUtil.info("Creating NPC with name: " + name);

		String type = ModConfig.getProperty(ConfigConstants.NPC_LLM_TYPE);

		clientLauncher.launch(name, type, getLlmModel(type), isOffline);
		return 1;
	}

	private int createNPCWithLLM(CommandContext<FabricClientCommandSource> context) {
		String name = StringArgumentType.getString(context, "name");
		boolean isOffline = BoolArgumentType.getBool(context, "isOffline");
		String llmType = StringArgumentType.getString(context, LLM_TYPE);
		String llmModel = StringArgumentType.getString(context, LLM_MODEL);

		LogUtil.info(("Creating NPC with name: " + name + ", LLM Type: " + llmType + ", LLM Model: " + llmModel));

		clientLauncher.launch(name, llmType, llmModel, isOffline);
		return 1;
	}

	private String getLlmModel(String type) {
		if (type.equals("ollama")) {
			return ModConfig.getProperty(ConfigConstants.NPC_LLM_OLLAMA_MODEL);
		} else if (type.equals("openai")) {
			return ModConfig.getProperty(ConfigConstants.NPC_LLM_OPENAI_MODEL);
		} else {
			throw new IllegalArgumentException("Invalid LLM type: " + type);
		}
	}
}
