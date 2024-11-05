package io.sailex.aiNpcLauncher.client.config;

import static io.sailex.aiNpcLauncher.client.AiNPCLauncher.MOD_ID;

import io.sailex.aiNpcLauncher.client.constants.ConfigConstants;
import io.sailex.aiNpcLauncher.client.exception.InvalidPropertyValueException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModConfig {

	private static final Logger LOGGER = LogManager.getLogger(ModConfig.class);
	private static final String CONFIG_NAME = "ai-npc-config.properties";
	private static Properties properties;
	private static File configFile;

	public static void init() {
		properties = new Properties();

		File configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), MOD_ID);
		configDir.mkdirs();
		configFile = new File(configDir, CONFIG_NAME);

		if (configFile.exists()) {
			loadProperties();
		} else {
			setDefaultProperties();
			saveProperties();
		}
	}

	private static void setDefaultProperties() {
		properties.setProperty(ConfigConstants.NPC_LLM_OLLAMA_URL, "http://localhost:11434/api/generate");
		properties.setProperty(ConfigConstants.NPC_LLM_OLLAMA_MODEL, "gemma2");

		properties.setProperty(ConfigConstants.NPC_LLM_OPENAI_MODEL, "gpt-4o-mini");
		properties.setProperty(ConfigConstants.NPC_LLM_OPENAI_API_KEY, "your-api-key");
		properties.setProperty(ConfigConstants.NPC_LLM_TYPE, "openai");
	}

	public static boolean saveProperties() {
		try (FileOutputStream out = new FileOutputStream(configFile)) {
			properties.store(out, "Configuration file for " + MOD_ID);
			return true;
		} catch (IOException e) {
			LOGGER.error("Failed to save config file!", e);
			return false;
		}
	}

	private static void loadProperties() {
		try (FileInputStream in = new FileInputStream(configFile)) {
			properties.load(in);
		} catch (IOException e) {
			LOGGER.error("Failed to load config file!", e);
			setDefaultProperties();
		}
	}

	public static String getProperty(String key) {
		if (!properties.containsKey(key)) {
			LOGGER.error("Property key '{}' not found!", key);
			throw new InvalidPropertyValueException("Property key not found: " + key);
		}
		return properties.getProperty(key);
	}

	public static boolean setProperty(String key, String value) {
		properties.setProperty(key, value);
		return saveProperties();
	}
}
