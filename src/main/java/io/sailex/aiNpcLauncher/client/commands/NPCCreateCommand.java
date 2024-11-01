package io.sailex.aiNpcLauncher.client.commands;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.sailex.aiNpcLauncher.client.config.ModConfig;
import io.sailex.aiNpcLauncher.client.constants.ConfigConstants;
import io.sailex.aiNpcLauncher.client.launcher.NPCClientLauncher;
import java.util.Set;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class NPCCreateCommand {

	private static final String LLM_TYPE = "llm-type";
	private static final String LLM_MODEL = "llm-model";

	private static final Set<String> allowedLLMTypes = Set.of("ollama", "openai");

	private final NPCClientLauncher npcClientLauncher;

	public NPCCreateCommand(NPCClientLauncher npcClientLauncher) {
		this.npcClientLauncher = npcClientLauncher;
	}

	public LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
		return literal("add")
				.then(argument("name", StringArgumentType.string())
						.executes(this::createNPC)
						.then(argument(LLM_TYPE, StringArgumentType.string())
								.suggests((context, builder) -> {
									for (String llmType : allowedLLMTypes) {
										builder.suggest(llmType);
									}
									return builder.buildFuture();
								})
								.then(argument(LLM_MODEL, StringArgumentType.string())
										.executes(this::createNPCWithLLM))));
	}

	private int createNPC(CommandContext<FabricClientCommandSource> context) {
		String name = StringArgumentType.getString(context, "name");

		context.getSource().sendFeedback(Text.of("Creating NPC with name: " + name));

		String type = ModConfig.getProperty(ConfigConstants.NPC_LLM_TYPE);

		npcClientLauncher.launch(name, type, getLlmModel(type));
		return 1;
	}

	private String getLlmModel(String type) {
		if (type.equals("ollama")) {
			return ModConfig.getProperty(ConfigConstants.NPC_LLM_OLLAMA_MODEL);
		} else {
			return ModConfig.getProperty(ConfigConstants.NPC_LLM_OPENAI_MODEL);
		}
	}

	private int createNPCWithLLM(CommandContext<FabricClientCommandSource> context) {
		String name = StringArgumentType.getString(context, "name");
		String llmType = StringArgumentType.getString(context, LLM_TYPE);
		String llmModel = StringArgumentType.getString(context, LLM_MODEL);

		context.getSource()
				.sendFeedback(Text.of(
						"Creating NPC with name: " + name + ", LLM Type: " + llmType + ", LLM Model: " + llmModel));

		npcClientLauncher.launch(name, llmType, llmModel);
		return 1;
	}
}
