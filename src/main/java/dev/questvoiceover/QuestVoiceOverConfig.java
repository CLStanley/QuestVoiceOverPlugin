package dev.questvoiceover;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("questvoiceover")
public interface QuestVoiceOverConfig extends Config
{
	@ConfigItem(
		keyName = "enableTestBeepPlayback",
		name = "Enable Test Beep Playback",
		description = "Emit a simple system beep whenever new dialogue is detected for playback testing",
		position = 0
	)
	default boolean enableTestBeepPlayback()
	{
		return false;
	}

	@ConfigItem(
		keyName = "lastObservedSpeaker",
		name = "Last Observed Speaker",
		description = "The speaker for the latest dialogue detected by the plugin",
		position = 1
	)
	default String lastObservedSpeaker()
	{
		return "";
	}

	@ConfigItem(
		keyName = "lastObservedDialogue",
		name = "Last Observed Dialogue",
		description = "The latest dialogue text detected by the plugin",
		position = 2
	)
	default String lastObservedDialogue()
	{
		return "";
	}

	@ConfigItem(
		keyName = "lastObservedChatMessage",
		name = "Last Observed Chat Message",
		description = "The latest observed dialogue in a combined legacy format",
		position = 3
	)
	default String lastObservedChatMessage()
	{
		return "No chat message observed yet.";
	}

	@ConfigItem(
		keyName = "lastPlaybackTrigger",
		name = "Last Playback Trigger",
		description = "The most recent dialogue line passed to the playback pipeline",
		position = 4
	)
	default String lastPlaybackTrigger()
	{
		return "No playback triggered yet.";
	}
}
