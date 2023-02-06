package gr.cite.intelcomp.graphexplorer.web.authorization;

import gr.cite.commons.web.authz.configuration.AuthorizationConfiguration;
import gr.cite.commons.web.authz.handler.AuthorizationHandler;
import gr.cite.commons.web.authz.handler.AuthorizationHandlerContext;
import gr.cite.commons.web.authz.policy.AuthorizationRequirement;
import gr.cite.commons.web.oidc.principal.MyPrincipal;
import gr.cite.commons.web.oidc.principal.extractor.ClaimExtractor;
import gr.cite.intelcomp.graphexplorer.authorization.EdgeRolesAuthorizationRequirement;
import gr.cite.intelcomp.graphexplorer.authorization.EdgeRolesResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("edgeRolesAuthorizationHandler")
public class EdgeRolesAuthorizationHandler extends AuthorizationHandler<EdgeRolesAuthorizationRequirement> {

	private final AuthorizationConfiguration configuration;
	private final MyCustomPermissionAttributesConfiguration myConfiguration;
	private final ClaimExtractor claimExtractor;

	@Autowired
	public EdgeRolesAuthorizationHandler(AuthorizationConfiguration configuration, MyCustomPermissionAttributesConfiguration myConfiguration, ClaimExtractor claimExtractor) {
		this.configuration = configuration;
		this.myConfiguration = myConfiguration;
		this.claimExtractor = claimExtractor;
	}

	@Override
	public int handleRequirement(AuthorizationHandlerContext context, Object resource, AuthorizationRequirement requirement) {
		EdgeRolesAuthorizationRequirement req = (EdgeRolesAuthorizationRequirement) requirement;
		if (req.getRequiredPermissions() == null) return ACCESS_NOT_DETERMINED;

		EdgeRolesResource rs = (EdgeRolesResource) resource;

		boolean isAuthenticated = ((MyPrincipal) context.getPrincipal()).isAuthenticated();
		if (!isAuthenticated) return ACCESS_NOT_DETERMINED;

		if (myConfiguration.getMyPolicies() == null) return ACCESS_NOT_DETERMINED;

		int hits = 0;
		if (isAuthenticated) {

			List<String> roles = rs != null && rs.getEdgeRoles() != null ? rs.getEdgeRoles() : null;

			for (String permission : req.getRequiredPermissions()) {
				MyCustomPermissionAttributesProperties.MyPermission policy = myConfiguration.getMyPolicies().get(permission);
				boolean hasPermission = hasPermission(policy.getEdge(), roles);
				if (hasPermission) hits += 1;
			}
		}
		if ((req.getMatchAll() && req.getRequiredPermissions().size() == hits) || (!req.getMatchAll() && hits > 0)) return ACCESS_GRANTED;

		return ACCESS_NOT_DETERMINED;
	}

	private Boolean hasPermission(EdgeRole EdgeRole, List<String> roles) {
		if (roles == null) return false;
		if (EdgeRole == null || EdgeRole.getRoles() == null) return false;
		Boolean hasRole = false;
		for (String role : EdgeRole.getRoles()) {
			if (roles.contains(role)) return true;
		}
		return false;
	}

	@Override
	public Class<? extends AuthorizationRequirement> supporting() {
		return EdgeRolesAuthorizationRequirement.class;
	}

}
