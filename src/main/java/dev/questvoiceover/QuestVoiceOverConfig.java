package dev.questvoiceover;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("questvoiceover")
public interface QuestVoiceOverConfig extends Config
{
	@ConfigItem(
		keyName = "lastObservedSpeaker",
		name = "Last Observed Speaker",
		description = "The speaker for the latest dialogue detected by the plugin"
	)
	default String lastObservedSpeaker()
	{
		return "";
	}

	@ConfigItem(
		keyName = "lastObservedDialogue",
		name = "Last Observed Dialogue",
		description = "The latest dialogue text detected by the plugin"
	)
	default String lastObservedDialogue()
	{
		return "";
	}

	@ConfigItem(
		keyName = "lastObservedChatMessage",
		name = "Last Observed Chat Message",
		description = "The latest observed dialogue in a combined legacy format"
	)
	default String lastObservedChatMessage()
	{
		return "No chat message observed yet.";
	}
}
