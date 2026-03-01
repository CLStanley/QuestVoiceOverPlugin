package dev.questvoiceover;

import java.nio.file.Files;
import java.nio.file.Path;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class QuestVoiceOverPluginTest
{
	private static final Path BOLT_USER_HOME = Path.of(
		System.getProperty("user.home"),
		".var",
		"app",
		"com.adamcake.Bolt",
		"data",
		"bolt-launcher");

	private static final String RUNELITE_CREDENTIALS = ".runelite/credentials.properties";
	private static final String DEV_WM_CLASS = "questvoiceover-dev";

	public static void main(String[] args) throws Exception
	{
		useDevWindowClass();
		useBoltCredentialsWhenAvailable();
		ExternalPluginManager.loadBuiltin(QuestVoiceOverPlugin.class);
		RuneLite.main(args);
	}

	private static void useDevWindowClass()
	{
		System.setProperty("sun.awt.X11.XWMClass", DEV_WM_CLASS);
	}

	private static void useBoltCredentialsWhenAvailable()
	{
		Path currentCredentials = Path.of(System.getProperty("user.home"), RUNELITE_CREDENTIALS);
		Path boltCredentials = BOLT_USER_HOME.resolve(RUNELITE_CREDENTIALS);

		if (!Files.exists(currentCredentials) && Files.exists(boltCredentials))
		{
			System.setProperty("user.home", BOLT_USER_HOME.toString());
		}
	}
}
