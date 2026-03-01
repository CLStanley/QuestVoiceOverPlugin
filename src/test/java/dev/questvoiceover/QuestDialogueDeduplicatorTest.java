package dev.questvoiceover;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class QuestDialogueDeduplicatorTest
{
	@Test
	public void shouldSuppressOnlyExactConsecutiveDuplicateDialogue()
	{
		QuestDialogueDeduplicator deduplicator = new QuestDialogueDeduplicator();

		assertTrue(deduplicator.shouldProcess("Cook", "Hello there."));
		assertFalse(deduplicator.shouldProcess("Cook", "Hello there."));
		assertTrue(deduplicator.shouldProcess("Cook", "Need anything else?"));
		assertTrue(deduplicator.shouldProcess("Duke", "Hello there."));
	}

	@Test
	public void shouldTreatNullSpeakerAsEmptyStringForDeduplication()
	{
		QuestDialogueDeduplicator deduplicator = new QuestDialogueDeduplicator();

		assertTrue(deduplicator.shouldProcess(null, "A mysterious force..."));
		assertFalse(deduplicator.shouldProcess("", "A mysterious force..."));
	}
}
