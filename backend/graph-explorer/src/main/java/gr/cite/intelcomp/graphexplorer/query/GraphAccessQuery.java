package gr.cite.intelcomp.graphexplorer.query;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.authorization.Permission;
import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.common.scope.user.UserScope;
import gr.cite.intelcomp.graphexplorer.data.GraphAccessEntity;
import gr.cite.intelcomp.graphexplorer.model.GraphAccess;
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
public class GraphAccessQuery extends QueryBase<GraphAccessEntity> {

	private Collection<UUID> ids;
	private Collection<UUID> graphIds;
	private Collection<UUID> userIds;
	private Boolean hasUser;
	private Collection<IsActive> isActives;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);


	public GraphAccessQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public GraphAccessQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public GraphAccessQuery ids(Collection<UUID> values) {
		this.ids = values;
		return this;
	}

	public GraphAccessQuery isActive(IsActive value) {
		this.isActives = List.of(value);
		return this;
	}

	public GraphAccessQuery isActive(IsActive... value) {
		this.isActives = Arrays.asList(value);
		return this;
	}

	public GraphAccessQuery isActive(Collection<IsActive> values) {
		this.isActives = values;
		return this;
	}

	public GraphAccessQuery graphIds(UUID value) {
		this.graphIds = List.of(value);
		return this;
	}

	public GraphAccessQuery graphIds(UUID... value) {
		this.graphIds = Arrays.asList(value);
		return this;
	}

	public GraphAccessQuery graphIds(Collection<UUID> values) {
		this.graphIds = values;
		return this;
	}

	public GraphAccessQuery userIds(UUID value) {
		this.userIds = List.of(value);
		return this;
	}

	public GraphAccessQuery userIds(UUID... value) {
		this.userIds = Arrays.asList(value);
		return this;
	}

	public GraphAccessQuery userIds(Collection<UUID> values) {
		this.userIds = values;
		return this;
	}

	public GraphAccessQuery hasUser(Boolean values) {
		this.hasUser = values;
		return this;
	}

	public GraphAccessQuery authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	private final UserScope userScope;
	private final AuthorizationService authService;

	public GraphAccessQuery(
			UserScope userScope,
			AuthorizationService authService
	) {
		this.userScope = userScope;
		this.authService = authService;
	}

	@Override
	protected Class<GraphAccessEntity> entityClass() {
		return GraphAccessEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.graphIds) || this.isEmpty(this.isActives) || this.isEmpty(this.userIds) || this.isEmpty(this.ids);
	}

	@Override
	protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
		if (this.authorize.contains(AuthorizationFlags.None)) return null;
		if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseGraphAccess)) return null;
		UUID ownerId = null;
		if (this.authorize.contains(AuthorizationFlags.Owner)) ownerId = this.userScope.getUserIdSafe();

		List<Predicate> predicates = new ArrayList<>();
		if (ownerId != null) {
			predicates.add(queryContext.CriteriaBuilder.equal(queryContext.Root.get(GraphAccessEntity._userId), ownerId));
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
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(GraphAccessEntity._id));
			for (UUID item : this.ids) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.graphIds != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(GraphAccessEntity._graphId));
			for (UUID item : this.graphIds) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.userIds != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(GraphAccessEntity._userId));
			for (UUID item : this.userIds) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.isActives != null) {
			CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(GraphAccessEntity._isActive));
			for (IsActive item : this.isActives) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.hasUser != null) {
			Predicate isNull = queryContext.CriteriaBuilder.isNull(queryContext.Root.get(GraphAccessEntity._userId));
			predicates.add(this.hasUser == true ? isNull.not() : isNull);
		}
		if (predicates.size() > 0) {
			Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
			return queryContext.CriteriaBuilder.and(predicatesArray);
		} else {
			return null;
		}
	}

	@Override
	protected GraphAccessEntity convert(Tuple tuple, Set<String> columns) {
		GraphAccessEntity item = new GraphAccessEntity();
		item.setId(QueryBase.convertSafe(tuple, columns, GraphAccessEntity._id, UUID.class));
		item.setIsActive(QueryBase.convertSafe(tuple, columns, GraphAccessEntity._isActive, IsActive.class));
		item.setUserId(QueryBase.convertSafe(tuple, columns, GraphAccessEntity._userId, UUID.class));
		item.setGraphId(QueryBase.convertSafe(tuple, columns, GraphAccessEntity._graphId, UUID.class));
		item.setCreatedAt(QueryBase.convertSafe(tuple, columns, GraphAccessEntity._createdAt, Instant.class));
		item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, GraphAccessEntity._updatedAt, Instant.class));
		return item;
	}

	@Override
	protected String fieldNameOf(FieldResolver item) {
		if (item.match(GraphAccess._id)) return GraphAccessEntity._id;
		else if (item.match(GraphAccess._isActive)) return GraphAccessEntity._isActive;
		else if (item.prefix(GraphAccess._user)) return GraphAccessEntity._userId;
		else if (item.prefix(GraphAccess._graph)) return GraphAccessEntity._graphId;
		else if (item.match(GraphAccess._createdAt)) return GraphAccessEntity._createdAt;
		else if (item.match(GraphAccess._updatedAt)) return GraphAccessEntity._updatedAt;
		else return null;
	}

}