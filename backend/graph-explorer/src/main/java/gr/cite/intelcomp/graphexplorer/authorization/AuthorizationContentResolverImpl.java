package gr.cite.intelcomp.graphexplorer.authorization;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.commons.web.oidc.principal.CurrentPrincipalResolver;
import gr.cite.commons.web.oidc.principal.extractor.ClaimExtractor;
import gr.cite.intelcomp.graphexplorer.authorization.cache.*;
import gr.cite.intelcomp.graphexplorer.common.JsonHandlingService;
import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.common.scope.user.UserScope;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.data.EdgeAccessEntity;
import gr.cite.intelcomp.graphexplorer.data.GraphAccessEntity;
import gr.cite.intelcomp.graphexplorer.data.NodeAccessEntity;
import gr.cite.intelcomp.graphexplorer.model.*;
import gr.cite.intelcomp.graphexplorer.query.EdgeAccessQuery;
import gr.cite.intelcomp.graphexplorer.query.GraphAccessQuery;
import gr.cite.intelcomp.graphexplorer.query.NodeAccessQuery;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.fieldset.BaseFieldSet;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequestScope
public class AuthorizationContentResolverImpl implements AuthorizationContentResolver {
	private final UserScope userScope;
	private final QueryFactory queryFactory;
	protected final ConventionService conventionService;
	private final AffiliatedNodesCacheService affiliatedNodesCacheService;
	private final PrincipalNodeResourceCacheService principalNodeResourceCacheService;
	private final AffiliatedEdgesCacheService affiliatedEdgesCacheService;
	private final PrincipalEdgeResourceCacheService principalEdgeResourceCacheService;
	private final AffiliatedGraphsCacheService affiliatedGraphsCacheService;
	private final PrincipalGraphResourceCacheService principalGraphResourceCacheService;
	private final ClaimExtractor claimExtractor;
	private final CurrentPrincipalResolver currentPrincipalResolver;
	private final AuthorizationService authorizationService;
	private final JsonHandlingService jsonHandlingService;

	public AuthorizationContentResolverImpl(
			UserScope userScope,
			QueryFactory queryFactory,
			ConventionService conventionService,
			AffiliatedNodesCacheService affiliatedNodesCacheService,
			PrincipalNodeResourceCacheService principalNodeResourceCacheService,
			AffiliatedEdgesCacheService affiliatedEdgesCacheService,
			PrincipalEdgeResourceCacheService principalEdgeResourceCacheService,
			AffiliatedGraphsCacheService affiliatedGraphsCacheService, 
			PrincipalGraphResourceCacheService principalGraphResourceCacheService, 
			ClaimExtractor claimExtractor,
			CurrentPrincipalResolver currentPrincipalResolver,
			AuthorizationService authorizationService,
			JsonHandlingService jsonHandlingService
	) {
		this.userScope = userScope;
		this.queryFactory = queryFactory;
		this.conventionService = conventionService;
		this.affiliatedNodesCacheService = affiliatedNodesCacheService;
		this.principalNodeResourceCacheService = principalNodeResourceCacheService;
		this.affiliatedEdgesCacheService = affiliatedEdgesCacheService;
		this.principalEdgeResourceCacheService = principalEdgeResourceCacheService;
		this.affiliatedGraphsCacheService = affiliatedGraphsCacheService;
		this.principalGraphResourceCacheService = principalGraphResourceCacheService;
		this.claimExtractor = claimExtractor;
		this.currentPrincipalResolver = currentPrincipalResolver;
		this.authorizationService = authorizationService;
		this.jsonHandlingService = jsonHandlingService;
	}

	public GraphRolesResource graphAffiliation(UUID graphId) {
		UUID userId = this.userScope.getUserIdSafe();
		GraphRolesResource resource = new GraphRolesResource(userId);
		if (userId == null) return resource;

		String entityType = "graph";
		PrincipalGraphResourceCacheService.PrincipalGraphResourceCacheValue cacheValue = this.principalGraphResourceCacheService.lookup(this.principalGraphResourceCacheService.buildKey(userId, graphId, entityType));
		if (cacheValue == null) {
			resource = this.resolveGraphAffiliation(graphId);
			cacheValue = new PrincipalGraphResourceCacheService.PrincipalGraphResourceCacheValue(userId, graphId, entityType, resource);
			this.principalGraphResourceCacheService.put(cacheValue);
		}
		return cacheValue.getGraphRolesResource();
	}

	private GraphRolesResource resolveGraphAffiliation(UUID graphId) {
		UUID userId = this.userScope.getUserIdSafe();
		GraphRolesResource resource = new GraphRolesResource(userId);
		if (userId == null) return resource;
		List<GraphAccessEntity> graphAccesses = this.queryFactory.query(GraphAccessQuery.class).isActive(IsActive.ACTIVE).
				graphIds(graphId).collectAs(new BaseFieldSet().ensure(GraphAccess._id).ensure(this.conventionService.asIndexer(GraphAccess._graph, Graph._id)).ensure(GraphAccess._user));

		List<UUID> graphIds = new ArrayList<>();
		for (GraphAccessEntity graphAccessEntity : graphAccesses) {
			if (graphAccessEntity.getUserId() == null || graphAccessEntity.getUserId().equals(userId)) {
				graphIds.add(graphAccessEntity.getGraphId());
			}
		}
		if (graphIds.contains(graphId)) {
			List<String> roles = claimExtractor.roles(this.currentPrincipalResolver.currentPrincipal());
			resource.setGraphRoles(roles);
		}

		return resource;
	}
	public List<UUID> affiliatedGraphs(String... permissions) {
		UUID userId = this.userScope.getUserIdSafe();
		if (userId == null) return new ArrayList<>();

		AffiliatedGraphsCacheService.AffiliatedGraphsCacheValue cacheValue = this.affiliatedGraphsCacheService.lookup(this.affiliatedGraphsCacheService.buildKey(userId, permissions != null ? List.of(permissions) : null));
		if (cacheValue == null) {
			List<UUID> graphIds = this.resolveAffiliatedGraphs(permissions);

			cacheValue = new AffiliatedGraphsCacheService.AffiliatedGraphsCacheValue(userId, graphIds, permissions != null ? List.of(permissions) : null);
			this.affiliatedGraphsCacheService.put(cacheValue);
		}
		return cacheValue.getGraphIds();
	}

	private List<UUID> resolveAffiliatedGraphs(String... permissions) {
		UUID userId = this.userScope.getUserIdSafe();
		List<UUID> graphIds = new ArrayList<>();

		if (userId == null) return graphIds;
		List<String> roles = claimExtractor.roles(this.currentPrincipalResolver.currentPrincipal());

		List<GraphAccessEntity> graphAccesses = this.queryFactory.query(GraphAccessQuery.class).isActive(IsActive.ACTIVE).collectAs(new BaseFieldSet().ensure(GraphAccess._id).ensure(this.conventionService.asIndexer(GraphAccess._graph, Graph._id)).ensure(GraphAccess._user));

		for (GraphAccessEntity graphAccessEntity : graphAccesses) {
			if (graphAccessEntity.getUserId() == null || graphAccessEntity.getUserId().equals(userId)) {
				GraphRolesResource resource = new GraphRolesResource(userId);
				resource.setGraphRoles(roles);
				Boolean isPermitted = this.authorizationService.authorizeAtLeastOne(List.of(resource), true, permissions);
				if (isPermitted) graphIds.add(graphAccessEntity.getGraphId());
			}
		}

		return graphIds.stream().distinct().collect(Collectors.toList());
	}

	public NodeRolesResource nodeAffiliation(UUID nodeId) {
		UUID userId = this.userScope.getUserIdSafe();
		NodeRolesResource resource = new NodeRolesResource(userId);
		if (userId == null) return resource;

		String entityType = "node";
		PrincipalNodeResourceCacheService.PrincipalNodeResourceCacheValue cacheValue = this.principalNodeResourceCacheService.lookup(this.principalNodeResourceCacheService.buildKey(userId, nodeId, entityType));
		if (cacheValue == null) {
			resource = this.resolveNodeAffiliation(nodeId);
			cacheValue = new PrincipalNodeResourceCacheService.PrincipalNodeResourceCacheValue(userId, nodeId, entityType, resource);
			this.principalNodeResourceCacheService.put(cacheValue);
		}
		return cacheValue.getNodeRolesResource();
	}

	private NodeRolesResource resolveNodeAffiliation(UUID nodeId) {
		UUID userId = this.userScope.getUserIdSafe();
		NodeRolesResource resource = new NodeRolesResource(userId);
		if (userId == null) return resource;
		List<NodeAccessEntity> nodeAccesses = this.queryFactory.query(NodeAccessQuery.class).isActive(IsActive.ACTIVE).
				nodeIds(nodeId).collectAs(new BaseFieldSet().ensure(NodeAccess._id).ensure(this.conventionService.asIndexer(NodeAccess._node, Node._id)).ensure(NodeAccess._user));

		List<UUID> nodeIds = new ArrayList<>();
		for (NodeAccessEntity nodeAccessEntity : nodeAccesses) {
			if (nodeAccessEntity.getUserId() == null || nodeAccessEntity.getUserId().equals(userId)) {
				nodeIds.add(nodeAccessEntity.getNodeId());
			}
		}
		if (nodeIds.contains(nodeId)) {
			List<String> roles = claimExtractor.roles(this.currentPrincipalResolver.currentPrincipal());
			resource.setNodeRoles(roles);
		}

		return resource;
	}
	public List<UUID> affiliatedNodes(String... permissions) {
		UUID userId = this.userScope.getUserIdSafe();
		if (userId == null) return new ArrayList<>();

		AffiliatedNodesCacheService.AffiliatedNodesCacheValue cacheValue = this.affiliatedNodesCacheService.lookup(this.affiliatedNodesCacheService.buildKey(userId, permissions != null ? List.of(permissions) : null));
		if (cacheValue == null) {
			List<UUID> nodeIds = this.resolveAffiliatedNodes(permissions);

			cacheValue = new AffiliatedNodesCacheService.AffiliatedNodesCacheValue(userId, nodeIds, permissions != null ? List.of(permissions) : null);
			this.affiliatedNodesCacheService.put(cacheValue);
		}
		return cacheValue.getNodeIds();
	}

	private List<UUID> resolveAffiliatedNodes(String... permissions) {
		UUID userId = this.userScope.getUserIdSafe();
		List<UUID> nodeIds = new ArrayList<>();

		if (userId == null) return nodeIds;
		List<String> roles = claimExtractor.roles(this.currentPrincipalResolver.currentPrincipal());

		List<NodeAccessEntity> nodeAccesses = this.queryFactory.query(NodeAccessQuery.class).isActive(IsActive.ACTIVE).collectAs(new BaseFieldSet().ensure(NodeAccess._id).ensure(this.conventionService.asIndexer(NodeAccess._node, Node._id)).ensure(NodeAccess._user));

		for (NodeAccessEntity nodeAccessEntity : nodeAccesses) {
			if (nodeAccessEntity.getUserId() == null || nodeAccessEntity.getUserId().equals(userId)) {
				NodeRolesResource resource = new NodeRolesResource(userId);
				resource.setNodeRoles(roles);
				Boolean isPermitted = this.authorizationService.authorizeAtLeastOne(List.of(resource), true, permissions);
				if (isPermitted) nodeIds.add(nodeAccessEntity.getNodeId());
			}
		}

		return nodeIds.stream().distinct().collect(Collectors.toList());
	}

	public EdgeRolesResource edgeAffiliation(UUID edgeId) {
		UUID userId = this.userScope.getUserIdSafe();
		EdgeRolesResource resource = new EdgeRolesResource(userId);
		if (userId == null) return resource;

		String entityType = "edge";
		PrincipalEdgeResourceCacheService.PrincipalEdgeResourceCacheValue cacheValue = this.principalEdgeResourceCacheService.lookup(this.principalEdgeResourceCacheService.buildKey(userId, edgeId, entityType));
		if (cacheValue == null) {
			resource = this.resolveEdgeAffiliation(edgeId);
			cacheValue = new PrincipalEdgeResourceCacheService.PrincipalEdgeResourceCacheValue(userId, edgeId, entityType, resource);
			this.principalEdgeResourceCacheService.put(cacheValue);
		}
		return cacheValue.getEdgeRolesResource();
	}

	private EdgeRolesResource resolveEdgeAffiliation(UUID edgeId) {
		UUID userId = this.userScope.getUserIdSafe();
		EdgeRolesResource resource = new EdgeRolesResource(userId);
		if (userId == null) return resource;
		List<EdgeAccessEntity> edgeAccesses = this.queryFactory.query(EdgeAccessQuery.class).isActive(IsActive.ACTIVE).
				edgeIds(edgeId).collectAs(new BaseFieldSet().ensure(EdgeAccess._id).ensure(this.conventionService.asIndexer(EdgeAccess._edge, Edge._id)).ensure(EdgeAccess._user));

		List<UUID> edgeIds = new ArrayList<>();
		for (EdgeAccessEntity edgeAccessEntity : edgeAccesses) {
			if (edgeAccessEntity.getUserId() == null || edgeAccessEntity.getUserId().equals(userId)) {
				edgeIds.add(edgeAccessEntity.getEdgeId());
			}
		}
		if (edgeIds.contains(edgeId)) {
			List<String> roles = claimExtractor.roles(this.currentPrincipalResolver.currentPrincipal());
			resource.setEdgeRoles(roles);
		}

		return resource;
	}
	public List<UUID> affiliatedEdges(String... permissions) {
		UUID userId = this.userScope.getUserIdSafe();
		if (userId == null) return new ArrayList<>();

		AffiliatedEdgesCacheService.AffiliatedEdgesCacheValue cacheValue = this.affiliatedEdgesCacheService.lookup(this.affiliatedEdgesCacheService.buildKey(userId, permissions != null ? List.of(permissions) : null));
		if (cacheValue == null) {
			List<UUID> edgeIds = this.resolveAffiliatedEdges(permissions);

			cacheValue = new AffiliatedEdgesCacheService.AffiliatedEdgesCacheValue(userId, edgeIds, permissions != null ? List.of(permissions) : null);
			this.affiliatedEdgesCacheService.put(cacheValue);
		}
		return cacheValue.getEdgeIds();
	}

	private List<UUID> resolveAffiliatedEdges(String... permissions) {
		UUID userId = this.userScope.getUserIdSafe();
		List<UUID> edgeIds = new ArrayList<>();

		if (userId == null) return edgeIds;
		List<String> roles = claimExtractor.roles(this.currentPrincipalResolver.currentPrincipal());

		List<EdgeAccessEntity> edgeAccesses = this.queryFactory.query(EdgeAccessQuery.class).isActive(IsActive.ACTIVE).collectAs(new BaseFieldSet().ensure(EdgeAccess._id).ensure(this.conventionService.asIndexer(EdgeAccess._edge, Edge._id)).ensure(EdgeAccess._user));

		for (EdgeAccessEntity edgeAccessEntity : edgeAccesses) {
			if (edgeAccessEntity.getUserId() == null || edgeAccessEntity.getUserId().equals(userId)) {
				EdgeRolesResource resource = new EdgeRolesResource(userId);
				resource.setEdgeRoles(roles);
				Boolean isPermitted = this.authorizationService.authorizeAtLeastOne(List.of(resource), true, permissions);
				if (isPermitted) edgeIds.add(edgeAccessEntity.getEdgeId());
			}
		}

		return edgeIds.stream().distinct().collect(Collectors.toList());
	}
}
