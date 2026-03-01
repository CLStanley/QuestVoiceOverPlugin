package dev.questvoiceover;

import java.awt.Toolkit;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

@Singleton
@Slf4j
class DebugQuestDialoguePlaybackService implements QuestDialoguePlaybackService
{
	private static final String CONFIG_GROUP = "questvoiceover";

	private final QuestVoiceOverConfig config;
	private final ConfigManager configManager;

	@Inject
	private DebugQuestDialoguePlaybackService(QuestVoiceOverConfig config, ConfigManager configManager)
	{
		this.config = config;
		this.configManager = configManager;
	}

	@Override
	public void play(QuestDialogue dialogue)
	{
		configManager.setConfiguration(CONFIG_GROUP, "lastPlaybackTrigger", dialogue.getCombinedText());

		if (!config.enableTestBeepPlayback())
		{
			log.debug("Playback trigger observed for '{}'", dialogue.getCombinedText());
			return;
		}

		try
		{
			Toolkit.getDefaultToolkit().beep();
		}
		catch (Exception ex)
		{
			log.warn("Unable to emit test playback beep for '{}'", dialogue.getCombinedText(), ex);
		}
	}
}
