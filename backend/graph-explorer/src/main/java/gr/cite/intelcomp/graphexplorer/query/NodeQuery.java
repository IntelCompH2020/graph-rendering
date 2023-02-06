package gr.cite.intelcomp.graphexplorer.query;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationContentResolver;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.authorization.Permission;
import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.common.scope.user.UserScope;
import gr.cite.intelcomp.graphexplorer.data.NodeEntity;
import gr.cite.intelcomp.graphexplorer.model.Node;
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
public class NodeQuery extends QueryBase<NodeEntity> {

	private String like;
	private Collection<UUID> ids;
	private Collection<String> codes;
	private Collection<IsActive> isActives;
	private Collection<UUID> excludedIds;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	public NodeQuery like(String value) {
		this.like = value;
		return this;
	}

	public NodeQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public NodeQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public NodeQuery ids(Collection<UUID> values) {
		this.ids = values;
		return this;
	}

	public NodeQuery codes(String value) {
		this.codes = List.of(value);
		return this;
	}

	public NodeQuery codes(String... value) {
		this.codes = Arrays.asList(value);
		return this;
	}

	public NodeQuery codes(Collection<String> values) {
		this.codes = values;
		return this;
	}

	public NodeQuery isActive(IsActive value) {
		this.isActives = List.of(value);
		return this;
	}

	public NodeQuery isActive(IsActive... value) {
		this.isActives = Arrays.asList(value);
		return this;
	}

	public NodeQuery isActive(Collection<IsActive> values) {
		this.isActives = values;
		return this;
	}

	public NodeQuery excludedIds(Collection<UUID> values) {
		this.excludedIds = values;
		return this;
	}

	public NodeQuery excludedIds(UUID value) {
		this.excludedIds = List.of(value);
		return this;
	}

	public NodeQuery excludedIds(UUID... value) {
		this.excludedIds = Arrays.asList(value);
		return this;
	}

	public NodeQuery authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	private final UserScope userScope;
	private final AuthorizationService authService;
	private final AuthorizationContentResolver authorizationContentResolver;

	public NodeQuery(
			UserScope userScope,
			AuthorizationService authService,
			AuthorizationContentResolver authorizationContentResolver) {
		this.userScope = userScope;
		this.authService = authService;
		this.authorizationContentResolver = authorizationContentResolver;
	}

	@Override
	protected Class<NodeEntity> entityClass() {
		return NodeEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.ids) || this.isEmpty(this.isActives) || this.isEmpty(this.codes) || this.isEmpty(this.excludedIds);
	}

	@Override
	protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
		if (this.authorize.contains(AuthorizationFlags.None)) return null;
		if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseNode)) return null;
		List<UUID> allowedNodeIds = null;
		if (this.authorize.contains(AuthorizationFlags.Affiliated)) allowedNodeIds = this.authorizationContentResolver.affiliatedNodes(Permission.BrowseNode);;

		List<Predicate> predicates = new ArrayList<>();
		if (allowedNodeIds != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(NodeEntity._id));
			for (UUID item : allowedNodeIds) inClause.value(item);
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
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(NodeEntity._id));
			for (UUID item : this.ids) inClause.value(item);
			predicates.add(inClause);
		}

		if (this.codes != null) {
			CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(NodeEntity._code));
			for (String item : this.codes) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.like != null && !this.like.isEmpty()) {
			predicates.add(queryContext.CriteriaBuilder.like(queryContext.Root.get(NodeEntity._name), this.like));
		}
		if (this.isActives != null) {
			CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(NodeEntity._isActive));
			for (IsActive item : this.isActives) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.excludedIds != null) {
			CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(NodeEntity._id));
			for (UUID item : this.excludedIds) notInClause.value(item);
			predicates.add(notInClause.not());
		}
		if (predicates.size() > 0) {
			Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);
			return queryContext.CriteriaBuilder.and(predicatesArray);
		} else {
			return null;
		}
	}

	@Override
	protected NodeEntity convert(Tuple tuple, Set<String> columns) {
		NodeEntity item = new NodeEntity();
		item.setId(QueryBase.convertSafe(tuple, columns, NodeEntity._id, UUID.class));
		item.setCode(QueryBase.convertSafe(tuple, columns, NodeEntity._code, String.class));
		item.setName(QueryBase.convertSafe(tuple, columns, NodeEntity._name, String.class));
		item.setDescription(QueryBase.convertSafe(tuple, columns, NodeEntity._description, String.class));
		item.setConfig(QueryBase.convertSafe(tuple, columns, NodeEntity._config, String.class));
		item.setCreatedAt(QueryBase.convertSafe(tuple, columns, NodeEntity._createdAt, Instant.class));
		item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, NodeEntity._updatedAt, Instant.class));
		item.setIsActive(QueryBase.convertSafe(tuple, columns, NodeEntity._isActive, IsActive.class));
		return item;
	}

	@Override
	protected String fieldNameOf(FieldResolver item) {
		if (item.match(Node._id)) return NodeEntity._id;
		else if (item.match(Node._code)) return NodeEntity._code;
		else if (item.match(Node._name)) return NodeEntity._name;
		else if (item.match(Node._description)) return NodeEntity._description;
		else if (item.match(Node._config)) return NodeEntity._config;
		else if (item.prefix(Node._config)) return NodeEntity._config;
		else if (item.match(Node._createdAt)) return NodeEntity._createdAt;
		else if (item.match(Node._updatedAt)) return NodeEntity._updatedAt;
		else if (item.match(Node._isActive)) return NodeEntity._isActive;
		else return null;
	}

}
