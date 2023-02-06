package gr.cite.intelcomp.graphexplorer.eventscheduler.manage;

import gr.cite.intelcomp.graphexplorer.common.enums.ScheduledEventType;

public class ScheduledEventCancelData {
	private ScheduledEventType type;
	private String key;
	private String keyType;

	public ScheduledEventType getType() {
		return type;
	}

	public void setType(ScheduledEventType type) {
		this.type = type;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getKeyType() {
		return keyType;
	}

	public void setKeyType(String keyType) {
		this.keyType = keyType;
	}
}
