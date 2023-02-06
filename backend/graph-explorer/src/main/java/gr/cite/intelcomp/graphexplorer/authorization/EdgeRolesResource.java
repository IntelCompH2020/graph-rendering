package gr.cite.intelcomp.graphexplorer.authorization;

import gr.cite.commons.web.authz.policy.AuthorizationResource;

import java.util.List;
import java.util.UUID;

public class EdgeRolesResource extends AuthorizationResource {
	private List<String> edgeRoles;
	private final UUID userId;

	public EdgeRolesResource(UUID userId) {
		this.userId = userId;
	}

	public List<String> getEdgeRoles() {
		return edgeRoles;
	}

	public void setEdgeRoles(List<String> edgeRoles) {
		this.edgeRoles = edgeRoles;
	}

	public UUID getUserId() {
		return userId;
	}
}
