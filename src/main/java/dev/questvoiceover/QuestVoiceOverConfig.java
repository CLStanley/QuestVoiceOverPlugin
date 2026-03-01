package dev.questvoiceover;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("questvoiceover")
public interface QuestVoiceOverConfig extends Config
{
	@ConfigItem(
		keyName = "lastObservedChatMessage",
		name = "Last Observed Chat Message",
		description = "The latest chat message detected by the plugin"
	)
	default String lastObservedChatMessage()
	{
		return "No chat message observed yet.";
	}
}
