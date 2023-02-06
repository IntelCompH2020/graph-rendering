package gr.cite.intelcomp.graphexplorer.model;

import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;

import java.time.Instant;
import java.util.UUID;

public class EdgeAccess {

	private UUID id;
	public final static String _id = "id";

	private User user;
	public final static String _user = "user";

	private Edge edge;
	public final static String _edge = "edge";

	private IsActive isActive;
	public final static String _isActive = "isActive";

	private String hash;
	public final static String _hash = "hash";

	private Instant createdAt;
	public final static String _createdAt = "createdAt";

	private Instant updatedAt;
	public final static String _updatedAt = "updatedAt";

	//private String updatedAt;
	public final static String _config = "config";


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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Edge getEdge() {
		return edge;
	}

	public void setEdge(Edge edge) {
		this.edge = edge;
	}

	public IsActive getIsActive() {
		return isActive;
	}

	public void setIsActive(IsActive isActive) {
		this.isActive = isActive;
	}
}
