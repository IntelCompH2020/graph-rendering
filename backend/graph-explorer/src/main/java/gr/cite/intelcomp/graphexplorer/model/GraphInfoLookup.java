package gr.cite.intelcomp.graphexplorer.model;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class GraphInfoLookup {

	@NotNull(message = "{validation.empty}")
	private List<UUID> nodeIds;

	@NotNull(message = "{validation.empty}")
	private List<UUID> edgeIds;

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
