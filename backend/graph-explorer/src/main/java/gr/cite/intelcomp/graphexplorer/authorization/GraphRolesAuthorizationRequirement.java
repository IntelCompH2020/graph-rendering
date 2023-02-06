package gr.cite.intelcomp.graphexplorer.authorization;

import gr.cite.commons.web.authz.policy.AuthorizationRequirement;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class GraphRolesAuthorizationRequirement implements AuthorizationRequirement {

	private final Set<String> requiredPermissions;
	private final boolean matchAll;

	public GraphRolesAuthorizationRequirement(Set<String> requiredPermissions) {
		this(false, requiredPermissions);
	}

	public GraphRolesAuthorizationRequirement(String... requiredPermissions) {
		this(false, requiredPermissions);

	}

	public GraphRolesAuthorizationRequirement(boolean matchAll, Set<String> requiredPermissions) {
		this.matchAll = matchAll;
		this.requiredPermissions = requiredPermissions;
	}

	public GraphRolesAuthorizationRequirement(boolean matchAll, String... requiredPermissions) {
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
