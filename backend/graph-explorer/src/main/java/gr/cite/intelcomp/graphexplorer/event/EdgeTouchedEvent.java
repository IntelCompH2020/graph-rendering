package gr.cite.intelcomp.graphexplorer.event;

import java.util.UUID;

public class EdgeTouchedEvent {
	public EdgeTouchedEvent() {
	}

	public EdgeTouchedEvent(UUID id) {
		this.id = id;
	}

	private UUID id;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}
}
