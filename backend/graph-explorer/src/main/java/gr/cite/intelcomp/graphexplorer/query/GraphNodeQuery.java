package gr.cite.intelcomp.graphexplorer.query;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationContentResolver;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.authorization.Permission;
import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.common.scope.user.UserScope;
import gr.cite.intelcomp.graphexplorer.data.GraphNodeEntity;
import gr.cite.intelcomp.graphexplorer.model.GraphNode;
import gr.cite.tools.data.query.FieldResolver;
import gr.cite.tools.data.query.QueryBase;
import gr.cite.tools.data.query.QueryContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GraphNodeQuery extends QueryBase<GraphNodeEntity> {

	private Collection<UUID> ids;
	private Collection<UUID> graphIds;
	private Collection<UUID> nodeIds;
	private Collection<IsActive> isActives;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);


	public GraphNodeQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public GraphNodeQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public GraphNodeQuery ids(Collection<UUID> values) {
		this.ids = values;
		return this;
	}

	public GraphNodeQuery isActive(IsActive value) {
		this.isActives = List.of(value);
		return this;
	}

	public GraphNodeQuery isActive(IsActive... value) {
		this.isActives = Arrays.asList(value);
		return this;
	}

	public GraphNodeQuery isActive(Collection<IsActive> values) {
		this.isActives = values;
		return this;
	}

	public GraphNodeQuery graphIds(UUID value) {
		this.graphIds = List.of(value);
		return this;
	}

	public GraphNodeQuery graphIds(UUID... value) {
		this.graphIds = Arrays.asList(value);
		return this;
	}

	public GraphNodeQuery graphIds(Collection<UUID> values) {
		this.graphIds = values;
		return this;
	}

	public GraphNodeQuery nodeIds(UUID value) {
		this.nodeIds = List.of(value);
		return this;
	}

	public GraphNodeQuery nodeIds(UUID... value) {
		this.nodeIds = Arrays.asList(value);
		return this;
	}

	public GraphNodeQuery nodeIds(Collection<UUID> values) {
		this.nodeIds = values;
		return this;
	}

	public GraphNodeQuery authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	private final UserScope userScope;
	private final AuthorizationService authService;
	private final AuthorizationContentResolver authorizationContentResolver;

	public GraphNodeQuery(
			UserScope userScope,
			AuthorizationService authService,
			AuthorizationContentResolver authorizationContentResolver) {
		this.userScope = userScope;
		this.authService = authService;
		this.authorizationContentResolver = authorizationContentResolver;
	}

	@Override
	protected Class<GraphNodeEntity> entityClass() {
		return GraphNodeEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.graphIds) || this.isEmpty(this.isActives) || this.isEmpty(this.nodeIds) || this.isEmpty(this.ids);
	}

	@Override
	protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
		if (this.authorize.contains(AuthorizationFlags.None)) return null;
		if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseGraphNode)) return null;
		List<UUID> allowedGraphIds = null;
		if (this.authorize.contains(AuthorizationFlags.Affiliated)) allowedGraphIds = this.authorizationContentResolver.affiliatedGraphs(Permission.BrowseNodeData);


		List<Predicate> predicates = new ArrayList<>();
		if (allowedGraphIds != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(GraphNodeEntity._graphId));
			for (UUID item : allowedGraphIds) inClause.value(item);
			predicates.add(inClause);
		}
		if (predicates.size() > 0) {
			Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
			return queryContext.CriteriaBuilder.and(predicatesArray);
		} else {
			return queryContext.CriteriaBuilder.or(); //Creates a false query
		}
	}

	@Override
	protected <X, Y> Predicate applyFilters(QueryContext<X, Y> queryContext) {
		List<Predicate> predicates = new ArrayList<>();
		if (this.ids != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(GraphNodeEntity._id));
			for (UUID item : this.ids) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.graphIds != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(GraphNodeEntity._graphId));
			for (UUID item : this.graphIds) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.nodeIds != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(GraphNodeEntity._nodeId));
			for (UUID item : this.nodeIds) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.isActives != null) {
			CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(GraphNodeEntity._isActive));
			for (IsActive item : this.isActives) inClause.value(item);
			predicates.add(inClause);
		}
		if (predicates.size() > 0) {
			Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
			return queryContext.CriteriaBuilder.and(predicatesArray);
		} else {
			return null;
		}
	}

	@Override
	protected GraphNodeEntity convert(Tuple tuple, Set<String> columns) {
		GraphNodeEntity item = new GraphNodeEntity();
		item.setId(QueryBase.convertSafe(tuple, columns, GraphNodeEntity._id, UUID.class));
		item.setIsActive(QueryBase.convertSafe(tuple, columns, GraphNodeEntity._isActive, IsActive.class));
		item.setNodeId(QueryBase.convertSafe(tuple, columns, GraphNodeEntity._nodeId, UUID.class));
		item.setGraphId(QueryBase.convertSafe(tuple, columns, GraphNodeEntity._graphId, UUID.class));
		item.setCreatedAt(QueryBase.convertSafe(tuple, columns, GraphNodeEntity._createdAt, Instant.class));
		item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, GraphNodeEntity._updatedAt, Instant.class));
		return item;
	}

	@Override
	protected String fieldNameOf(FieldResolver item) {
		if (item.match(GraphNode._id)) return GraphNodeEntity._id;
		else if (item.match(GraphNode._isActive)) return GraphNodeEntity._isActive;
		else if (item.prefix(GraphNode._node)) return GraphNodeEntity._nodeId;
		else if (item.prefix(GraphNode._graph)) return GraphNodeEntity._graphId;
		else if (item.match(GraphNode._createdAt)) return GraphNodeEntity._createdAt;
		else if (item.match(GraphNode._updatedAt)) return GraphNodeEntity._updatedAt;
		else return null;
	}

}