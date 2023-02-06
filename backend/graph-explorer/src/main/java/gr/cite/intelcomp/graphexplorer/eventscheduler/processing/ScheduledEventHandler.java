package gr.cite.intelcomp.graphexplorer.eventscheduler.processing;

import gr.cite.intelcomp.graphexplorer.data.ScheduledEventEntity;

public interface ScheduledEventHandler {
	EventProcessingStatus handle(ScheduledEventEntity scheduledEvent);
}
