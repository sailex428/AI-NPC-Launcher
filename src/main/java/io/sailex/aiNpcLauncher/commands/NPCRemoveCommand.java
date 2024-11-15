package io.sailex.aiNpcLauncher.server.commands;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.sailex.aiNpcLauncher.server.launcher.ServerProcessManager;
import io.sailex.aiNpcLauncher.server.util.LogUtil;
import lombok.AllArgsConstructor;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@AllArgsConstructor
public class NPCRemoveCommand {

    private final ServerProcessManager serverProcessManager;

    public LiteralArgumentBuilder<ServerCommandSource> getCommand() {
        return literal("remove")
                .requires(source -> source.hasPermissionLevel(2)) // Requires op level 2
                .then(argument("name", StringArgumentType.string())
                        .executes(this::removeNPC));
    }

    private int removeNPC(CommandContext<ServerCommandSource> context) {
        try {
            String name = StringArgumentType.getString(context, "name");
            ServerPlayerEntity player = context.getSource().getPlayer();

            if (player == null) {
                LogUtil.error("Command must be executed by a player");
                return 0;
            }

            LogUtil.info("Removing NPC with name: " + name, player);
            
            if (serverProcessManager.endProcess(name)) {
                context.getSource().sendFeedback(() -> Text.literal("Successfully removed NPC: " + name), false);
                return 1;
            } else {
                LogUtil.error("No NPC found with name: " + name, player);
                context.getSource().sendError(Text.literal("No NPC found with name: " + name));
                return 0;
            }
        } catch (Exception e) {
            ServerPlayerEntity player = context.getSource().getPlayer();
            LogUtil.error("Failed to remove NPC: " + e.getMessage(), player);
            context.getSource().sendError(Text.literal("Failed to remove NPC: " + e.getMessage()));
            return 0;
        }
    }
}