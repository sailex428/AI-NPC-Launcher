package io.sailex.aiNpcLauncher.client.commands;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CommandManager {

    public void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            new SetConfigCommand().register(dispatcher);
            dispatcher.register(literal("npc")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(new NPCCreateCommand().getCommand())
                    .then(new NPCRemoveCommand().getCommand()));
        });
    }

}
