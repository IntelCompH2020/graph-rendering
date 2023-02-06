package gr.cite.intelcomp.graphexplorer.query;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationContentResolver;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.authorization.Permission;
import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.common.scope.user.UserScope;
import gr.cite.intelcomp.graphexplorer.data.GraphEdgeEntity;
import gr.cite.intelcomp.graphexplorer.model.GraphEdge;
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
public class GraphEdgeQuery extends QueryBase<GraphEdgeEntity> {

	private Collection<UUID> ids;
	private Collection<UUID> graphIds;
	private Collection<UUID> edgeIds;
	private Collection<IsActive> isActives;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);


	public GraphEdgeQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public GraphEdgeQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public GraphEdgeQuery ids(Collection<UUID> values) {
		this.ids = values;
		return this;
	}

	public GraphEdgeQuery isActive(IsActive value) {
		this.isActives = List.of(value);
		return this;
	}

	public GraphEdgeQuery isActive(IsActive... value) {
		this.isActives = Arrays.asList(value);
		return this;
	}

	public GraphEdgeQuery isActive(Collection<IsActive> values) {
		this.isActives = values;
		return this;
	}

	public GraphEdgeQuery graphIds(UUID value) {
		this.graphIds = List.of(value);
		return this;
	}

	public GraphEdgeQuery graphIds(UUID... value) {
		this.graphIds = Arrays.asList(value);
		return this;
	}

	public GraphEdgeQuery graphIds(Collection<UUID> values) {
		this.graphIds = values;
		return this;
	}

	public GraphEdgeQuery edgeIds(UUID value) {
		this.edgeIds = List.of(value);
		return this;
	}

	public GraphEdgeQuery edgeIds(UUID... value) {
		this.edgeIds = Arrays.asList(value);
		return this;
	}

	public GraphEdgeQuery edgeIds(Collection<UUID> values) {
		this.edgeIds = values;
		return this;
	}

	public GraphEdgeQuery authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	private final UserScope userScope;
	private final AuthorizationService authService;
	private final AuthorizationContentResolver authorizationContentResolver;

	public GraphEdgeQuery(
			UserScope userScope,
			AuthorizationService authService,
			AuthorizationContentResolver authorizationContentResolver) {
		this.userScope = userScope;
		this.authService = authService;
		this.authorizationContentResolver = authorizationContentResolver;
	}

	@Override
	protected Class<GraphEdgeEntity> entityClass() {
		return GraphEdgeEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.graphIds) || this.isEmpty(this.isActives) || this.isEmpty(this.edgeIds) || this.isEmpty(this.ids);
	}

	@Override
	protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
		if (this.authorize.contains(AuthorizationFlags.None)) return null;
		if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseGraphEdge)) return null;
		List<UUID> allowedGraphIds = null;
		if (this.authorize.contains(AuthorizationFlags.Affiliated)) allowedGraphIds = this.authorizationContentResolver.affiliatedGraphs(Permission.BrowseEdgeData);


		List<Predicate> predicates = new ArrayList<>();
		if (allowedGraphIds != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(GraphEdgeEntity._graphId));
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
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(GraphEdgeEntity._id));
			for (UUID item : this.ids) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.graphIds != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(GraphEdgeEntity._graphId));
			for (UUID item : this.graphIds) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.edgeIds != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(GraphEdgeEntity._edgeId));
			for (UUID item : this.edgeIds) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.isActives != null) {
			CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(GraphEdgeEntity._isActive));
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
	protected GraphEdgeEntity convert(Tuple tuple, Set<String> columns) {
		GraphEdgeEntity item = new GraphEdgeEntity();
		item.setId(QueryBase.convertSafe(tuple, columns, GraphEdgeEntity._id, UUID.class));
		item.setIsActive(QueryBase.convertSafe(tuple, columns, GraphEdgeEntity._isActive, IsActive.class));
		item.setEdgeId(QueryBase.convertSafe(tuple, columns, GraphEdgeEntity._edgeId, UUID.class));
		item.setGraphId(QueryBase.convertSafe(tuple, columns, GraphEdgeEntity._graphId, UUID.class));
		item.setCreatedAt(QueryBase.convertSafe(tuple, columns, GraphEdgeEntity._createdAt, Instant.class));
		item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, GraphEdgeEntity._updatedAt, Instant.class));
		return item;
	}

	@Override
	protected String fieldNameOf(FieldResolver item) {
		if (item.match(GraphEdge._id)) return GraphEdgeEntity._id;
		else if (item.match(GraphEdge._isActive)) return GraphEdgeEntity._isActive;
		else if (item.prefix(GraphEdge._edge)) return GraphEdgeEntity._edgeId;
		else if (item.prefix(GraphEdge._graph)) return GraphEdgeEntity._graphId;
		else if (item.match(GraphEdge._createdAt)) return GraphEdgeEntity._createdAt;
		else if (item.match(GraphEdge._updatedAt)) return GraphEdgeEntity._updatedAt;
		else return null;
	}

}