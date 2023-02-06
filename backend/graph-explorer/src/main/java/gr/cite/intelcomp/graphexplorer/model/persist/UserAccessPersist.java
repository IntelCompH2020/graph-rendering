package gr.cite.intelcomp.graphexplorer.model.persist;

import gr.cite.intelcomp.graphexplorer.common.validation.FieldNotNullIfOtherSet;
import gr.cite.intelcomp.graphexplorer.common.validation.ValidId;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public class UserAccessPersist {
	@ValidId(message = "{validation.invalidid}")
	@NotNull(message = "{validation.empty}")
	private UUID id;

	@NotNull(message = "{validation.empty}")
	@NotEmpty(message = "{validation.empty}")
	private String hash;

	@NotNull(message = "{validation.empty}")
	private List<UUID> nodeIds;
	
	@NotNull(message = "{validation.empty}")
	private List<UUID> edgeIds;
	
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

	public List<UUID> getNodeIds() {
		return nodeIds;
	}

	public void setNodeIds(List<UUID> nodeIds) {
		this.nodeIds = nodeIds;
	}

	public List<UUID> getEdgeIds() {
		return edgeIds;
	}

	public void setEdgeIds(List<UUID> edgeIds) {
		this.edgeIds = edgeIds;
	}
}
