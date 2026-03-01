package dev.questvoiceover;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Quest Voice Over Plugin"
)
public class QuestVoiceOverPlugin extends Plugin
{
	private static final String CONFIG_GROUP = "questvoiceover";
	private static final String PLAYER_SPEAKER_FALLBACK = "Player";

	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private QuestDialoguePlaybackService playbackService;

	private final QuestDialogueDeduplicator deduplicator = new QuestDialogueDeduplicator();

	@Override
	protected void startUp() throws Exception
	{
		log.debug("Quest Voice Over Plugin started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("Quest Voice Over Plugin stopped!");
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		Widget npcDialogueTextWidget = client.getWidget(InterfaceID.ChatLeft.TEXT);
		if (npcDialogueTextWidget != null)
		{
			String speaker = cleanWidgetText(InterfaceID.ChatLeft.NAME);
			String dialogue = clean(npcDialogueTextWidget.getText());
			observeDialogue(speaker, dialogue);
			return;
		}

		Widget playerDialogueTextWidget = client.getWidget(InterfaceID.ChatRight.TEXT);
		if (playerDialogueTextWidget != null)
		{
			String speaker = getPlayerSpeakerName();
			String dialogue = clean(playerDialogueTextWidget.getText());
			observeDialogue(speaker, dialogue);
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType() != ChatMessageType.DIALOG && chatMessage.getType() != ChatMessageType.MESBOX)
		{
			return;
		}

		String speaker = clean(chatMessage.getName());
		String dialogue = clean(chatMessage.getMessage());

		if (speaker.isEmpty() && dialogue.isEmpty())
		{
			return;
		}

		observeDialogue(speaker, dialogue);
	}

	@Provides
	QuestVoiceOverConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(QuestVoiceOverConfig.class);
	}

	@Provides
	QuestDialoguePlaybackService providePlaybackService(DebugQuestDialoguePlaybackService playbackService)
	{
		return playbackService;
	}

	private void observeDialogue(String speaker, String dialogue)
	{
		if (dialogue.isEmpty())
		{
			return;
		}

		speaker = speaker == null ? "" : speaker;
		if (!deduplicator.shouldProcess(speaker, dialogue))
		{
			return;
		}

		String combinedDialogue = formatCombinedDialogue(speaker, dialogue);
		QuestDialogue observedDialogue = new QuestDialogue(speaker, dialogue, combinedDialogue);
		recordObservedDialogue(observedDialogue);
		playbackService.play(observedDialogue);

		log.debug("Observed dialogue speaker='{}' dialogue='{}'", speaker, dialogue);
	}

	private String formatCombinedDialogue(String speaker, String dialogue)
	{
		if (!speaker.isEmpty())
		{
			return speaker + ": " + dialogue;
		}

		return dialogue;
	}

	private String cleanWidgetText(int packedWidgetId)
	{
		Widget widget = client.getWidget(packedWidgetId);
		if (widget == null)
		{
			return "";
		}

		return clean(widget.getText());
	}

	private String getPlayerSpeakerName()
	{
		if (client.getLocalPlayer() == null)
		{
			return PLAYER_SPEAKER_FALLBACK;
		}

		String playerName = clean(client.getLocalPlayer().getName());
		return playerName.isEmpty() ? PLAYER_SPEAKER_FALLBACK : playerName;
	}

	private String clean(String value)
	{
		if (value == null)
		{
			return "";
		}

		return Text.removeTags(value).replace('\u00A0', ' ').trim();
	}

	private void recordObservedDialogue(QuestDialogue dialogue)
	{
		configManager.setConfiguration(CONFIG_GROUP, "lastObservedSpeaker", dialogue.getSpeaker());
		configManager.setConfiguration(CONFIG_GROUP, "lastObservedDialogue", dialogue.getText());
		configManager.setConfiguration(CONFIG_GROUP, "lastObservedChatMessage", dialogue.getCombinedText());
	}
}
