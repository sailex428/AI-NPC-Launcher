package io.sailex.aiNpcLauncher.client.launcher;

import io.sailex.aiNpcLauncher.client.constants.ModRepositories;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import me.earth.headlessmc.api.command.CommandException;
import me.earth.headlessmc.api.exit.ExitManager;
import me.earth.headlessmc.launcher.Launcher;
import me.earth.headlessmc.launcher.LauncherBuilder;
import me.earth.headlessmc.launcher.auth.AuthException;
import me.earth.headlessmc.launcher.auth.LaunchAccount;
import me.earth.headlessmc.launcher.auth.ValidatedAccount;
import me.earth.headlessmc.launcher.command.FabricCommand;
import me.earth.headlessmc.launcher.command.download.DownloadCommand;
import me.earth.headlessmc.launcher.command.download.VersionInfo;
import me.earth.headlessmc.launcher.files.FileManager;
import me.earth.headlessmc.launcher.launch.LaunchOptions;
import me.earth.headlessmc.launcher.specifics.VersionSpecificException;
import me.earth.headlessmc.launcher.version.Version;
import me.earth.headlessmc.launcher.version.VersionImpl;
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
		setMcDir();
	}

	public void launch(String npcName, String llmType, String llmModel, boolean isOffline) {
		CompletableFuture.runAsync(() -> {
			try {
				String versionName = SharedConstants.getGameVersion().getName();
				Version version = findOrDownloadFabric(versionName);

				installAiNpcClientMod(version);

				LaunchAccount account = getAccount(npcName, isOffline);
				if (account == null) {
					logResult("Failed to login.");
				}

				FileManager files = launcher.getFileManager()
						.createRelative(UUID.randomUUID().toString());

				LaunchOptions options = LaunchOptions.builder()
						.account(account)
						.additionalJvmArgs(List.of("-Dllm.type=" + llmType, "-Dllm.model=" + llmModel))
						.version(version)
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
				logResult("Launching AI-NPC client!");
			} catch (Exception e) {
				logResult("Failed to setup or launch the game.");
			}
		});
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

	private void setMcDir() {
		String defaultMcDir =
				launcher.getLauncherConfig().getMcFiles().getBase().getPath();
		FileManager fileManager =
				new FileManager(Path.of(defaultMcDir, "ai-npc").toString());

		launcher.getLauncherConfig().setMcFiles(fileManager);
		launcher.getLauncherConfig().setGameDir(fileManager);
	}

	private Version findOrDownloadFabric(String versionName) throws CommandException {
		Version version = getVersion(versionName);
		if (version != null) {
			return version;
		}
		LOGGER.info("Downloading Fabric client...");
		Version neededVersion = VersionImpl.builder().name(versionName).build();

		DownloadCommand downloadCommand = new DownloadCommand(launcher);
		// cache version infos
		downloadCommand.execute(versionName);
		// get the needed version info
		VersionInfo versionInfo = null;
		for (VersionInfo info : downloadCommand.getIterable()) {
			if (info.getName().equals(versionName)) {
				versionInfo = info;
				break;
			}
		}
		// download vanilla
		downloadCommand.execute(versionInfo);

		// download fabric
		FabricCommand fabricCommand = new FabricCommand(launcher);
		fabricCommand.execute(neededVersion);

		return getVersion(versionName);
	}

	private Version getVersion(String versionName) {
		for (Version version : launcher.getVersionService().getContents()) {
			if (version.getName().contains(versionName) && version.getName().contains("fabric")) {
				return version;
			}
		}
		return null;
	}

	private void installAiNpcClientMod(Version version) {
        try {
			LOGGER.info("Downloading AI-NPC mod...");
            launcher.getVersionSpecificModManager().download(version, ModRepositories.AI_NPC);

        	LOGGER.info("Install AI-NPC mod...");
			launcher.getVersionSpecificModManager()
				.install(
						version,
						ModRepositories.AI_NPC,
						Path.of(launcher.getLauncherConfig().getMcFiles().getPath(), "mods"));
		} catch (VersionSpecificException | IOException e) {
			LOGGER.error("Failed to download AI-NPC mod.");
		}
	}

	private LaunchAccount getAccount(String npcName, boolean isOffline) throws Exception {
		if (isOffline) {
			return new LaunchAccount("msa", npcName, UUID.randomUUID().toString(), "", "");
		} else {
			HttpClient httpClient = MinecraftAuth.createHttpClient();
			StepFullJavaSession.FullJavaSession javaSession = MinecraftAuth.JAVA_DEVICE_CODE_LOGIN.getFromInput(
					httpClient, new StepMsaDeviceCode.MsaDeviceCodeCallback(msaDeviceCode -> {
						client.inGameHud.getChatHud().addMessage(Text.of("Go to"));
						client.inGameHud.getChatHud().addMessage(Text.of(msaDeviceCode.getDirectVerificationUri()));
					}));
			ValidatedAccount validatedAccount = new ValidatedAccount(
					javaSession, javaSession.getMcProfile().getMcToken().getAccessToken());
			return validatedAccount.toLaunchAccount();
		}
	}

	private void logResult(String result) {
		LOGGER.info(result);
		client.inGameHud.getChatHud().addMessage(Text.of(result));
	}
}
