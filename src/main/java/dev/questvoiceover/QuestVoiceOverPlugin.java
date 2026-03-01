package dev.questvoiceover;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.ChatMessage;
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
	@Inject
	private ConfigManager configManager;

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
	public void onChatMessage(ChatMessage chatMessage)
	{
		String observedMessage = formatObservedMessage(chatMessage);
		configManager.setConfiguration("questvoiceover", "lastObservedChatMessage", observedMessage);
		log.debug("Observed chat message: {}", observedMessage);
	}

	@Provides
	QuestVoiceOverConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(QuestVoiceOverConfig.class);
	}

	private String formatObservedMessage(ChatMessage chatMessage)
	{
		String name = clean(chatMessage.getName());
		String message = clean(chatMessage.getMessage());

		if (!name.isEmpty())
		{
			return chatMessage.getType() + " | " + name + ": " + message;
		}

		return chatMessage.getType() + " | " + message;
	}

	private String clean(String value)
	{
		if (value == null)
		{
			return "";
		}

		return Text.removeTags(value).trim();
	}
}
