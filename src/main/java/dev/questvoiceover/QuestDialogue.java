package dev.questvoiceover;

final class QuestDialogue
{
	private final String speaker;
	private final String text;
	private final String combinedText;

	QuestDialogue(String speaker, String text, String combinedText)
	{
		this.speaker = speaker;
		this.text = text;
		this.combinedText = combinedText;
	}

	String getSpeaker()
	{
		return speaker;
	}

	String getText()
	{
		return text;
	}

	String getCombinedText()
	{
		return combinedText;
	}
}
