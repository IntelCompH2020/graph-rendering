package gr.cite.intelcomp.graphexplorer.authorization;

import gr.cite.commons.web.authz.policy.AuthorizationRequirement;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class NodeRolesAuthorizationRequirement implements AuthorizationRequirement {

	private final Set<String> requiredPermissions;
	private final boolean matchAll;

	public NodeRolesAuthorizationRequirement(Set<String> requiredPermissions) {
		this(false, requiredPermissions);
	}

	public NodeRolesAuthorizationRequirement(String... requiredPermissions) {
		this(false, requiredPermissions);

	}

	public NodeRolesAuthorizationRequirement(boolean matchAll, Set<String> requiredPermissions) {
		this.matchAll = matchAll;
		this.requiredPermissions = requiredPermissions;
	}

	public NodeRolesAuthorizationRequirement(boolean matchAll, String... requiredPermissions) {
		this.requiredPermissions = new HashSet<>();
		this.matchAll = matchAll;
		this.requiredPermissions.addAll(Arrays.stream(requiredPermissions).distinct().collect(Collectors.toList()));
	}

	public Set<String> getRequiredPermissions() {
		return requiredPermissions;
	}

	public boolean getMatchAll() {
		return matchAll;
	}
}
