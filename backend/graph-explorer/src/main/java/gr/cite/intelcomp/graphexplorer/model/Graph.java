package gr.cite.intelcomp.graphexplorer.model;

import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Graph {

	public final static String _id = "id";
	private UUID id;


	public final static String _name = "name";
	private String name;

	public final static String _description = "description";
	private String description;

	public final static String _createdAt = "createdAt";
	private Instant createdAt;

	public final static String _updatedAt = "updatedAt";
	private Instant updatedAt;

	public final static String _isActive = "isActive";
	private IsActive isActive;

	public final static String _hash = "hash";
	private String hash;

	public final static String _graphNodes = "graphNodes";
	private List<GraphNode> graphNodes;

	public final static String _graphEdges = "graphEdges";
	private List<GraphEdge> graphEdges;

	public final static String _graphAccesses = "graphAccesses";
	private List<GraphAccess> graphAccesses;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public IsActive getIsActive() {
		return isActive;
	}

	public void setIsActive(IsActive isActive) {
		this.isActive = isActive;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public List<GraphNode> getGraphNodes() {
		return graphNodes;
	}

	public void setGraphNodes(List<GraphNode> graphNodes) {
		this.graphNodes = graphNodes;
	}

	public List<GraphEdge> getGraphEdges() {
		return graphEdges;
	}

	public void setGraphEdges(List<GraphEdge> graphEdges) {
		this.graphEdges = graphEdges;
	}

	public List<GraphAccess> getGraphAccesses() {
		return graphAccesses;
	}

	public void setGraphAccesses(List<GraphAccess> graphAccesses) {
		this.graphAccesses = graphAccesses;
	}
}
