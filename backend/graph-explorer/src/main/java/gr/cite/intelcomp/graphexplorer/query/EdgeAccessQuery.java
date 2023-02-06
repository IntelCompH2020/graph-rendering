package gr.cite.intelcomp.graphexplorer.query;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.authorization.Permission;
import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.common.scope.user.UserScope;
import gr.cite.intelcomp.graphexplorer.data.EdgeAccessEntity;
import gr.cite.intelcomp.graphexplorer.model.EdgeAccess;
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
public class EdgeAccessQuery extends QueryBase<EdgeAccessEntity> {

	private Collection<UUID> ids;
	private Collection<UUID> edgeIds;
	private Collection<UUID> userIds;
	private Boolean hasUser;
	private Collection<IsActive> isActives;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);


	public EdgeAccessQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public EdgeAccessQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public EdgeAccessQuery ids(Collection<UUID> values) {
		this.ids = values;
		return this;
	}

	public EdgeAccessQuery isActive(IsActive value) {
		this.isActives = List.of(value);
		return this;
	}

	public EdgeAccessQuery isActive(IsActive... value) {
		this.isActives = Arrays.asList(value);
		return this;
	}

	public EdgeAccessQuery isActive(Collection<IsActive> values) {
		this.isActives = values;
		return this;
	}

	public EdgeAccessQuery edgeIds(UUID value) {
		this.edgeIds = List.of(value);
		return this;
	}

	public EdgeAccessQuery edgeIds(UUID... value) {
		this.edgeIds = Arrays.asList(value);
		return this;
	}

	public EdgeAccessQuery edgeIds(Collection<UUID> values) {
		this.edgeIds = values;
		return this;
	}

	public EdgeAccessQuery userIds(UUID value) {
		this.userIds = List.of(value);
		return this;
	}

	public EdgeAccessQuery userIds(UUID... value) {
		this.userIds = Arrays.asList(value);
		return this;
	}

	public EdgeAccessQuery userIds(Collection<UUID> values) {
		this.userIds = values;
		return this;
	}

	public EdgeAccessQuery hasUser(Boolean values) {
		this.hasUser = values;
		return this;
	}

	public EdgeAccessQuery authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	private final UserScope userScope;
	private final AuthorizationService authService;

	public EdgeAccessQuery(
			UserScope userScope,
			AuthorizationService authService
	) {
		this.userScope = userScope;
		this.authService = authService;
	}

	@Override
	protected Class<EdgeAccessEntity> entityClass() {
		return EdgeAccessEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.edgeIds) || this.isEmpty(this.isActives) || this.isEmpty(this.userIds) || this.isEmpty(this.ids);
	}

	@Override
	protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
		if (this.authorize.contains(AuthorizationFlags.None)) return null;
		if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseEdgeAccess)) return null;
		UUID ownerId = null;
		if (this.authorize.contains(AuthorizationFlags.Owner)) ownerId = this.userScope.getUserIdSafe();

		List<Predicate> predicates = new ArrayList<>();
		if (ownerId != null) {
			predicates.add(queryContext.CriteriaBuilder.equal(queryContext.Root.get(EdgeAccessEntity._userId), ownerId));
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
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EdgeAccessEntity._id));
			for (UUID item : this.ids) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.edgeIds != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EdgeAccessEntity._edgeId));
			for (UUID item : this.edgeIds) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.userIds != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EdgeAccessEntity._userId));
			for (UUID item : this.userIds) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.isActives != null) {
			CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EdgeAccessEntity._isActive));
			for (IsActive item : this.isActives) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.hasUser != null) {
			Predicate isNull = queryContext.CriteriaBuilder.isNull(queryContext.Root.get(EdgeAccessEntity._userId));
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
	protected EdgeAccessEntity convert(Tuple tuple, Set<String> columns) {
		EdgeAccessEntity item = new EdgeAccessEntity();
		item.setId(QueryBase.convertSafe(tuple, columns, EdgeAccessEntity._id, UUID.class));
		item.setIsActive(QueryBase.convertSafe(tuple, columns, EdgeAccessEntity._isActive, IsActive.class));
		item.setUserId(QueryBase.convertSafe(tuple, columns, EdgeAccessEntity._userId, UUID.class));
		item.setEdgeId(QueryBase.convertSafe(tuple, columns, EdgeAccessEntity._edgeId, UUID.class));
		item.setCreatedAt(QueryBase.convertSafe(tuple, columns, EdgeAccessEntity._createdAt, Instant.class));
		item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, EdgeAccessEntity._updatedAt, Instant.class));
		return item;
	}

	@Override
	protected String fieldNameOf(FieldResolver item) {
		if (item.match(EdgeAccess._id)) return EdgeAccessEntity._id;
		else if (item.match(EdgeAccess._isActive)) return EdgeAccessEntity._isActive;
		else if (item.prefix(EdgeAccess._user)) return EdgeAccessEntity._userId;
		else if (item.prefix(EdgeAccess._edge)) return EdgeAccessEntity._edgeId;
		else if (item.match(EdgeAccess._createdAt)) return EdgeAccessEntity._createdAt;
		else if (item.match(EdgeAccess._updatedAt)) return EdgeAccessEntity._updatedAt;
		else return null;
	}

}