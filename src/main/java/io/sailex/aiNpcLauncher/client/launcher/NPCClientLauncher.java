package io.sailex.aiNpcLauncher.client.launcher;

import java.io.IOException;
import java.util.List;
import me.earth.headlessmc.api.exit.ExitManager;
import me.earth.headlessmc.launcher.Launcher;
import me.earth.headlessmc.launcher.LauncherBuilder;
import me.earth.headlessmc.launcher.auth.AuthException;
import me.earth.headlessmc.launcher.launch.InMemoryLauncher;
import me.earth.headlessmc.launcher.launch.JavaLaunchCommandBuilder;
import me.earth.headlessmc.launcher.launch.LaunchException;
import me.earth.headlessmc.launcher.launch.LaunchOptions;
import me.earth.headlessmc.launcher.version.Version;
import me.earth.headlessmc.launcher.version.VersionImpl;
import net.minecraft.SharedConstants;

public class NPCClientLauncher {

	public void launch(String name, String llmType, String llmModel) {
		try {
			String versionName = SharedConstants.getGameVersion().getName();
			Version version = VersionImpl.builder().name(versionName).build();

			Launcher launcher = initLauncher();
			LaunchOptions options = LaunchOptions.builder()
					.additionalJvmArgs(List.of("-Dllm.type=" + llmType, "-Dllm.model=" + llmModel))
					.launcher(launcher)
					.version(version)
					.lwjgl(false)
					.build();

			InMemoryLauncher clientLauncher = new InMemoryLauncher(
					options,
					JavaLaunchCommandBuilder.builder().build(),
					version,
					launcher.getJavaService().getCurrent());
			clientLauncher.launch();
		} catch (IOException | AuthException | LaunchException e) {
			throw new RuntimeException(e);
		}
	}

	private Launcher initLauncher() throws AuthException {
		LauncherBuilder builder = new LauncherBuilder();
		builder.exitManager(new ExitManager());
		builder.initLogging();

		return builder.buildDefault();
	}
}
