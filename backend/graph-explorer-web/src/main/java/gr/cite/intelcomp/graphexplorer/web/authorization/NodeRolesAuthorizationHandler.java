package gr.cite.intelcomp.graphexplorer.web.authorization;

import gr.cite.commons.web.authz.configuration.AuthorizationConfiguration;
import gr.cite.commons.web.authz.handler.AuthorizationHandler;
import gr.cite.commons.web.authz.handler.AuthorizationHandlerContext;
import gr.cite.commons.web.authz.policy.AuthorizationRequirement;
import gr.cite.commons.web.oidc.principal.MyPrincipal;
import gr.cite.commons.web.oidc.principal.extractor.ClaimExtractor;
import gr.cite.intelcomp.graphexplorer.authorization.NodeRolesAuthorizationRequirement;
import gr.cite.intelcomp.graphexplorer.authorization.NodeRolesResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("nodeRolesAuthorizationHandler")
public class NodeRolesAuthorizationHandler extends AuthorizationHandler<NodeRolesAuthorizationRequirement> {

	private final AuthorizationConfiguration configuration;
	private final MyCustomPermissionAttributesConfiguration myConfiguration;
	private final ClaimExtractor claimExtractor;

	@Autowired
	public NodeRolesAuthorizationHandler(AuthorizationConfiguration configuration, MyCustomPermissionAttributesConfiguration myConfiguration, ClaimExtractor claimExtractor) {
		this.configuration = configuration;
		this.myConfiguration = myConfiguration;
		this.claimExtractor = claimExtractor;
	}

	@Override
	public int handleRequirement(AuthorizationHandlerContext context, Object resource, AuthorizationRequirement requirement) {
		NodeRolesAuthorizationRequirement req = (NodeRolesAuthorizationRequirement) requirement;
		if (req.getRequiredPermissions() == null) return ACCESS_NOT_DETERMINED;

		NodeRolesResource rs = (NodeRolesResource) resource;

		boolean isAuthenticated = ((MyPrincipal) context.getPrincipal()).isAuthenticated();
		if (!isAuthenticated) return ACCESS_NOT_DETERMINED;

		if (myConfiguration.getMyPolicies() == null) return ACCESS_NOT_DETERMINED;

		int hits = 0;
		if (isAuthenticated) {

			List<String> roles = rs != null && rs.getNodeRoles() != null ? rs.getNodeRoles() : null;

			for (String permission : req.getRequiredPermissions()) {
				MyCustomPermissionAttributesProperties.MyPermission policy = myConfiguration.getMyPolicies().get(permission);
				boolean hasPermission = hasPermission(policy.getNode(), roles);
				if (hasPermission) hits += 1;
			}
		}
		if ((req.getMatchAll() && req.getRequiredPermissions().size() == hits) || (!req.getMatchAll() && hits > 0)) return ACCESS_GRANTED;

		return ACCESS_NOT_DETERMINED;
	}

	private Boolean hasPermission(NodeRole nodeRole, List<String> roles) {
		if (roles == null) return false;
		if (nodeRole == null || nodeRole.getRoles() == null) return false;
		Boolean hasRole = false;
		for (String role : nodeRole.getRoles()) {
			if (roles.contains(role)) return true;
		}
		return false;
	}

	@Override
	public Class<? extends AuthorizationRequirement> supporting() {
		return NodeRolesAuthorizationRequirement.class;
	}

}
