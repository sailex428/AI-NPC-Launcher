package io.sailex.aiNpcLauncher.client;

import io.sailex.aiNpcLauncher.client.commands.CommandManager;
import net.fabricmc.api.ClientModInitializer;

public class AiNPCLauncher implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CommandManager commandManager = new CommandManager();
        commandManager.register();
    }
}
