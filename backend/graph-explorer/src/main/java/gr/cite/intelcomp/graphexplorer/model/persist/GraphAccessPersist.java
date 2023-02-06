package gr.cite.intelcomp.graphexplorer.model.persist;

import gr.cite.intelcomp.graphexplorer.common.validation.ValidId;

import javax.validation.constraints.NotNull;
import java.util.UUID;


public class GraphAccessPersist {

	@ValidId(message = "{validation.invalidid}")
	private UUID id;

	@ValidId(message = "{validation.invalidid}")
	@NotNull(message = "{validation.empty}")
	private UUID userId;

	@ValidId(message = "{validation.invalidid}")
	@NotNull(message = "{validation.empty}")
	private UUID graphId;

	private String hash;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public UUID getGraphId() {
		return graphId;
	}

	public void setGraphId(UUID graphId) {
		this.graphId = graphId;
	}
}
