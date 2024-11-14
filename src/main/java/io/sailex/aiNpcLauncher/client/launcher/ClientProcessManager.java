package io.sailex.aiNpcLauncher.client.launcher;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

@Getter
public class ClientProcessManager {

	private final Map<String, Process> npcClientProcesses;
	private ScheduledExecutorService executorService;

	public ClientProcessManager() {
		this.npcClientProcesses = new HashMap<>();
	}

	public void addProcess(String npcName, Process process) {
		npcClientProcesses.put(npcName, process);
	}

	public void endProcess(String npcName) {
		Process process = npcClientProcesses.get(npcName);
		if (process != null) {
			process.destroy();
			npcClientProcesses.remove(npcName);
		}
	}

	public void registerEndProcessOnDisconnect() {
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			executorService = Executors.newSingleThreadScheduledExecutor();
			executorService.schedule(
					() -> {
						npcClientProcesses.forEach((npcName, process) -> process.destroy());
						npcClientProcesses.clear();
					},
					30,
					TimeUnit.SECONDS);
			executorService.shutdown();
		});
	}
}
