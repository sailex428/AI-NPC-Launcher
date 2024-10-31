package io.sailex.aiNpcLauncher.client.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Set;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class NPCCreateCommand {

    private static final String LLM_TYPE = "llm-type";
    private static final String LLM_MODEL = "llm-model";

    private static final Set<String> allowedLLMTypes = Set.of("ollama", "openai");

    public LiteralArgumentBuilder<FabricClientCommandSource> getCommand() {
        return literal("add")
                .then(argument("name", StringArgumentType.string())
                        .executes(this::createNPCAtCurrentPosition)
                        .then(argument(LLM_TYPE, StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    for (String llmType : allowedLLMTypes) {
                                        builder.suggest(llmType);
                                    }
                                    return builder.buildFuture();
                                })
                                .then(argument(LLM_MODEL, StringArgumentType.string())
                                        .executes(this::createNPCAtCurrentPosition)
                                        .then(argument("x", IntegerArgumentType.integer())
                                                .then(argument("y", IntegerArgumentType.integer())
                                                        .then(argument("z", IntegerArgumentType.integer())
                                                                .executes(this::createNPCAtSpecifiedPosition)))))));
    }

}
