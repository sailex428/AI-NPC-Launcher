package io.sailex.aiNpcLauncher.client.launcher;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import me.earth.headlessmc.api.exit.ExitManager;
import me.earth.headlessmc.launcher.Launcher;
import me.earth.headlessmc.launcher.LauncherBuilder;
import me.earth.headlessmc.launcher.LauncherProperties;
import me.earth.headlessmc.launcher.auth.AuthException;
import me.earth.headlessmc.launcher.auth.LaunchAccount;
import me.earth.headlessmc.launcher.auth.ValidatedAccount;
import me.earth.headlessmc.launcher.files.FileManager;
import me.earth.headlessmc.launcher.launch.LaunchOptions;
import me.earth.headlessmc.launcher.version.Version;
import net.lenni0451.commons.httpclient.HttpClient;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.java.session.StepFullJavaSession;
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientLauncher {

	private final ClientProcessManager npcClientProcesses;
	private static final Logger LOGGER = LogManager.getLogger(ClientLauncher.class);
	private final Launcher launcher;
	private final MinecraftClient client;

	public ClientLauncher(ClientProcessManager npcClientProcesses, MinecraftClient client) {
		this.npcClientProcesses = npcClientProcesses;
		this.launcher = initLauncher();
		this.client = client;
	}

	public void launch(String npcName, String llmType, String llmModel, boolean isOffline) {
		CompletableFuture.runAsync(() -> {
			try {
				LaunchAccount account = getAccount(isOffline);

				if (account == null) {
					logResult("Failed to login.");
				}

				String versionName = SharedConstants.getGameVersion().getName();
				Version baseVersion = getVersion(versionName);

				UUID uuid = UUID.fromString(launcher.getConfig()
						.get(
								LauncherProperties.EXTRACTED_FILE_CACHE_UUID,
								UUID.randomUUID().toString()));
				FileManager files = launcher.getFileManager().createRelative(uuid.toString());

				LaunchOptions options = LaunchOptions.builder()
						.account(account)
						.additionalJvmArgs(List.of("-Dllm.type=" + llmType,
								"-Dllm.model=" + llmModel,
								isOffline ? "-Dhmc.offline.username=" + npcName : ""
						))
						.version(baseVersion)
						.launcher(launcher)
						.files(files)
						.parseFlags(launcher, false)
						.lwjgl(false)
						.prepare(false)
						.build();

				Process process = launcher.getProcessFactory().run(options);

				if (process == null) {
					launcher.getExitManager().exit(0);
					String result = "Failed to launch the game.";
					LOGGER.error(result);
					client.inGameHud.getChatHud().addMessage(Text.of(result));
				}

				npcClientProcesses.addProcess(npcName, process);
				logResult("Launched client successfully!");
			} catch (Exception e) {
				logResult("Failed to setup or launch the game.");
			}
		});
	}

	private LaunchAccount getAccount(boolean isOffline) throws Exception {
		if (isOffline) {
			return launcher.getAccountManager().getOfflineAccount(launcher.getConfig());
		} else {
			HttpClient httpClient = MinecraftAuth.createHttpClient();
			StepFullJavaSession.FullJavaSession javaSession = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.getFromInput(
					httpClient, new StepMsaDeviceCode.MsaDeviceCodeCallback(msaDeviceCode -> client.inGameHud
							.getChatHud()
							.addMessage(Text.of("Go to " + msaDeviceCode.getDirectVerificationUri()))));
			ValidatedAccount validatedAccount = new ValidatedAccount(
					javaSession, javaSession.getMcProfile().getMcToken().getAccessToken());
			return validatedAccount.toLaunchAccount();
		}
	}

	private Version getVersion(String versionName) {
		for (Version version : launcher.getVersionService().getContents()) {
			if (version.getName().contains(versionName) && version.getName().contains("fabric")) {
				return version;
			}
		}
		return null;
	}

	private Launcher initLauncher() {
		LauncherBuilder builder = new LauncherBuilder();
		builder.exitManager(new ExitManager());
		builder.initLogging();

		try {
			return builder.buildDefault();
		} catch (AuthException e) {
			throw new RuntimeException(e);
		}
	}

	private void logResult(String result) {
		LOGGER.info(result);
		client.inGameHud.getChatHud().addMessage(Text.of(result));
	}

}
