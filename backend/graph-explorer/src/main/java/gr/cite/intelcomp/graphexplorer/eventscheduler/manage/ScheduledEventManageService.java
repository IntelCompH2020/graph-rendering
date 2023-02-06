package gr.cite.intelcomp.graphexplorer.eventscheduler.manage;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface ScheduledEventManageService {
	void publishAsync(ScheduledEventPublishData item);

	void cancelAsync(ScheduledEventCancelData item) throws InvalidApplicationException;

	void cancelAsync(UUID id) throws InvalidApplicationException;
}
