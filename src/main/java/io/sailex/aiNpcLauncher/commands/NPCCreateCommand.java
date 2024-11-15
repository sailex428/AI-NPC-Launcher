package io.sailex.aiNpcLauncher.server.commands;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.sailex.aiNpcLauncher.server.config.ModConfig;
import io.sailex.aiNpcLauncher.server.constants.ConfigConstants;
import io.sailex.aiNpcLauncher.server.launcher.ServerLauncher;
import io.sailex.aiNpcLauncher.server.util.LogUtil;
import java.util.Set;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class NPCCreateCommand {

    private static final String LLM_TYPE = "llm-type";
    private static final String LLM_MODEL = "llm-model";

    private static final Set<String> allowedLLMTypes = Set.of("ollama", "openai");

    private final ServerLauncher serverLauncher;

    public NPCCreateCommand(ServerLauncher serverLauncher) {
        this.serverLauncher = serverLauncher;
    }

    public LiteralArgumentBuilder<ServerCommandSource> getCommand() {
        return literal("add")
                .requires(source -> source.hasPermissionLevel(2))
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

    private int createNPC(CommandContext<ServerCommandSource> context) {
        try {
            String name = StringArgumentType.getString(context, "name");
            boolean isOffline = BoolArgumentType.getBool(context, "isOffline");
            ServerPlayerEntity player = context.getSource().getPlayer();

            if (player == null) {
                LogUtil.error("Command must be executed by a player");
                return 0;
            }

            LogUtil.info("Creating NPC with name: " + name, player);

            String type = ModConfig.getProperty(ConfigConstants.NPC_LLM_TYPE);

            serverLauncher.launch(name, type, getLlmModel(type), isOffline);
            context.getSource().sendFeedback(() -> Text.literal("Successfully created NPC: " + name), false);
            return 1;
        } catch (Exception e) {
            ServerPlayerEntity player = context.getSource().getPlayer();
            LogUtil.error("Failed to create NPC: " + e.getMessage(), player);
            context.getSource().sendError(Text.literal("Failed to create NPC: " + e.getMessage()));
            return 0;
        }
    }

    private int createNPCWithLLM(CommandContext<ServerCommandSource> context) {
        try {
            String name = StringArgumentType.getString(context, "name");
            boolean isOffline = BoolArgumentType.getBool(context, "isOffline");
            String llmType = StringArgumentType.getString(context, LLM_TYPE);
            String llmModel = StringArgumentType.getString(context, LLM_MODEL);
            ServerPlayerEntity player = context.getSource().getPlayer();

            if (player == null) {
                LogUtil.error("Command must be executed by a player");
                return 0;
            }

            LogUtil.info("Creating NPC with name: " + name + ", LLM Type: " + llmType + ", LLM Model: " + llmModel, player);

            serverLauncher.launch(name, llmType, llmModel, isOffline);
            context.getSource().sendFeedback(() -> Text.literal("Successfully created NPC: " + name), false);
            return 1;
        } catch (Exception e) {
            ServerPlayerEntity player = context.getSource().getPlayer();
            LogUtil.error("Failed to create NPC: " + e.getMessage(), player);
            context.getSource().sendError(Text.literal("Failed to create NPC: " + e.getMessage()));
            return 0;
        }
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