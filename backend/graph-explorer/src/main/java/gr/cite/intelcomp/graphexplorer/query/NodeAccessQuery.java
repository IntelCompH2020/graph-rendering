package gr.cite.intelcomp.graphexplorer.query;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.authorization.Permission;
import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.common.scope.user.UserScope;
import gr.cite.intelcomp.graphexplorer.data.NodeAccessEntity;
import gr.cite.intelcomp.graphexplorer.model.NodeAccess;
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
public class NodeAccessQuery extends QueryBase<NodeAccessEntity> {

	private Collection<UUID> ids;
	private Collection<UUID> nodeIds;
	private Collection<UUID> userIds;
	private Boolean hasUser;
	private Collection<IsActive> isActives;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);


	public NodeAccessQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public NodeAccessQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public NodeAccessQuery ids(Collection<UUID> values) {
		this.ids = values;
		return this;
	}

	public NodeAccessQuery isActive(IsActive value) {
		this.isActives = List.of(value);
		return this;
	}

	public NodeAccessQuery isActive(IsActive... value) {
		this.isActives = Arrays.asList(value);
		return this;
	}

	public NodeAccessQuery isActive(Collection<IsActive> values) {
		this.isActives = values;
		return this;
	}

	public NodeAccessQuery nodeIds(UUID value) {
		this.nodeIds = List.of(value);
		return this;
	}

	public NodeAccessQuery nodeIds(UUID... value) {
		this.nodeIds = Arrays.asList(value);
		return this;
	}

	public NodeAccessQuery nodeIds(Collection<UUID> values) {
		this.nodeIds = values;
		return this;
	}

	public NodeAccessQuery userIds(UUID value) {
		this.userIds = List.of(value);
		return this;
	}

	public NodeAccessQuery userIds(UUID... value) {
		this.userIds = Arrays.asList(value);
		return this;
	}

	public NodeAccessQuery userIds(Collection<UUID> values) {
		this.userIds = values;
		return this;
	}

	public NodeAccessQuery hasUser(Boolean values) {
		this.hasUser = values;
		return this;
	}

	public NodeAccessQuery authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	private final UserScope userScope;
	private final AuthorizationService authService;

	public NodeAccessQuery(
			UserScope userScope,
			AuthorizationService authService
	) {
		this.userScope = userScope;
		this.authService = authService;
	}

	@Override
	protected Class<NodeAccessEntity> entityClass() {
		return NodeAccessEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.nodeIds) || this.isEmpty(this.isActives) || this.isEmpty(this.userIds) || this.isEmpty(this.ids);
	}

	@Override
	protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
		if (this.authorize.contains(AuthorizationFlags.None)) return null;
		if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseNodeAccess)) return null;
		UUID ownerId = null;
		if (this.authorize.contains(AuthorizationFlags.Owner)) ownerId = this.userScope.getUserIdSafe();

		List<Predicate> predicates = new ArrayList<>();
		if (ownerId != null) {
			predicates.add(queryContext.CriteriaBuilder.equal(queryContext.Root.get(NodeAccessEntity._userId), ownerId));
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
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(NodeAccessEntity._id));
			for (UUID item : this.ids) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.nodeIds != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(NodeAccessEntity._nodeId));
			for (UUID item : this.nodeIds) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.userIds != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(NodeAccessEntity._userId));
			for (UUID item : this.userIds) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.isActives != null) {
			CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(NodeAccessEntity._isActive));
			for (IsActive item : this.isActives) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.hasUser != null) {
			Predicate isNull = queryContext.CriteriaBuilder.isNull(queryContext.Root.get(NodeAccessEntity._userId));
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
	protected NodeAccessEntity convert(Tuple tuple, Set<String> columns) {
		NodeAccessEntity item = new NodeAccessEntity();
		item.setId(QueryBase.convertSafe(tuple, columns, NodeAccessEntity._id, UUID.class));
		item.setIsActive(QueryBase.convertSafe(tuple, columns, NodeAccessEntity._isActive, IsActive.class));
		item.setUserId(QueryBase.convertSafe(tuple, columns, NodeAccessEntity._userId, UUID.class));
		item.setNodeId(QueryBase.convertSafe(tuple, columns, NodeAccessEntity._nodeId, UUID.class));
		item.setCreatedAt(QueryBase.convertSafe(tuple, columns, NodeAccessEntity._createdAt, Instant.class));
		item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, NodeAccessEntity._updatedAt, Instant.class));
		return item;
	}

	@Override
	protected String fieldNameOf(FieldResolver item) {
		if (item.match(NodeAccess._id)) return NodeAccessEntity._id;
		else if (item.match(NodeAccess._isActive)) return NodeAccessEntity._isActive;
		else if (item.prefix(NodeAccess._user)) return NodeAccessEntity._userId;
		else if (item.prefix(NodeAccess._node)) return NodeAccessEntity._nodeId;
		else if (item.match(NodeAccess._createdAt)) return NodeAccessEntity._createdAt;
		else if (item.match(NodeAccess._updatedAt)) return NodeAccessEntity._updatedAt;
		else return null;
	}

}