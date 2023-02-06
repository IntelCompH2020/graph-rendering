package gr.cite.intelcomp.graphexplorer.authorization;

import gr.cite.commons.web.authz.policy.AuthorizationResource;

import java.util.List;
import java.util.UUID;

public class NodeRolesResource extends AuthorizationResource {
	private List<String> nodeRoles;
	private final UUID userId;

	public NodeRolesResource(UUID userId) {
		this.userId = userId;
	}

	public List<String> getNodeRoles() {
		return nodeRoles;
	}

	public void setNodeRoles(List<String> nodeRoles) {
		this.nodeRoles = nodeRoles;
	}

	public UUID getUserId() {
		return userId;
	}
}
