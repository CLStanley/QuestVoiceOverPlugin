package dev.questvoiceover;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class QuestVoiceOverPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(QuestVoiceOverPlugin.class);
		RuneLite.main(args);
	}
}
