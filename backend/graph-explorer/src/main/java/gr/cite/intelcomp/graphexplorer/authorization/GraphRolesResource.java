package gr.cite.intelcomp.graphexplorer.authorization;

import gr.cite.commons.web.authz.policy.AuthorizationResource;

import java.util.List;
import java.util.UUID;

public class GraphRolesResource extends AuthorizationResource {
	private List<String> graphRoles;
	private final UUID userId;

	public GraphRolesResource(UUID userId) {
		this.userId = userId;
	}

	public List<String> getGraphRoles() {
		return graphRoles;
	}

	public void setGraphRoles(List<String> graphRoles) {
		this.graphRoles = graphRoles;
	}

	public UUID getUserId() {
		return userId;
	}
}
