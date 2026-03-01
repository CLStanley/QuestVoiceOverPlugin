package dev.questvoiceover;

final class QuestDialogueDeduplicator
{
	private String lastSpeaker = "";
	private String lastDialogue = "";

	boolean shouldProcess(String speaker, String dialogue)
	{
		String normalizedSpeaker = speaker == null ? "" : speaker;
		if (normalizedSpeaker.equals(lastSpeaker) && dialogue.equals(lastDialogue))
		{
			return false;
		}

		lastSpeaker = normalizedSpeaker;
		lastDialogue = dialogue;
		return true;
	}
}
