package gr.cite.intelcomp.graphexplorer.query;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationContentResolver;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.authorization.Permission;
import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.common.scope.user.UserScope;
import gr.cite.intelcomp.graphexplorer.data.EdgeEntity;
import gr.cite.intelcomp.graphexplorer.model.Edge;
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
import java.util.stream.Collectors;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EdgeQuery extends QueryBase<EdgeEntity> {

	private String like;
	private Collection<UUID> ids;
	private Collection<String> codes;
	private Collection<IsActive> isActives;
	private Collection<UUID> excludedIds;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	public EdgeQuery like(String value) {
		this.like = value;
		return this;
	}

	public EdgeQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public EdgeQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public EdgeQuery ids(Collection<UUID> values) {
		this.ids = values;
		return this;
	}

	public EdgeQuery codes(String value) {
		this.codes = List.of(value);
		return this;
	}

	public EdgeQuery codes(String... value) {
		this.codes = Arrays.asList(value);
		return this;
	}

	public EdgeQuery codes(Collection<String> values) {
		this.codes = values;
		return this;
	}

	public EdgeQuery isActive(IsActive value) {
		this.isActives = List.of(value);
		return this;
	}

	public EdgeQuery isActive(IsActive... value) {
		this.isActives = Arrays.asList(value);
		return this;
	}

	public EdgeQuery isActive(Collection<IsActive> values) {
		this.isActives = values;
		return this;
	}

	public EdgeQuery excludedIds(Collection<UUID> values) {
		this.excludedIds = values;
		return this;
	}

	public EdgeQuery excludedIds(UUID value) {
		this.excludedIds = List.of(value);
		return this;
	}

	public EdgeQuery excludedIds(UUID... value) {
		this.excludedIds = Arrays.asList(value);
		return this;
	}

	public EdgeQuery authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	private final UserScope userScope;
	private final AuthorizationService authService;
	private final AuthorizationContentResolver authorizationContentResolver;

	public EdgeQuery(
			UserScope userScope,
			AuthorizationService authService,
			AuthorizationContentResolver authorizationContentResolver) {
		this.userScope = userScope;
		this.authService = authService;
		this.authorizationContentResolver = authorizationContentResolver;
	}

	@Override
	protected Class<EdgeEntity> entityClass() {
		return EdgeEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.ids) || this.isEmpty(this.isActives) || this.isEmpty(this.codes) || this.isEmpty(this.excludedIds);
	}

	@Override
	protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
		if (this.authorize.contains(AuthorizationFlags.None)) return null;
		if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseEdge)) return null;
		List<UUID> allowedEdgeIds = null;
		if (this.authorize.contains(AuthorizationFlags.Affiliated)) allowedEdgeIds = this.authorizationContentResolver.affiliatedEdges(Permission.BrowseEdge);;

		List<Predicate> predicates = new ArrayList<>();
		if (allowedEdgeIds != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EdgeEntity._id));
			for (UUID item : allowedEdgeIds) inClause.value(item);
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
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EdgeEntity._id));
			for (UUID item : this.ids) inClause.value(item);
			predicates.add(inClause);
		}

		if (this.codes != null) {
			CriteriaBuilder.In<String> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EdgeEntity._code));
			for (String item : this.codes) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.like != null && !this.like.isEmpty()) {
			predicates.add(queryContext.CriteriaBuilder.like(queryContext.Root.get(EdgeEntity._name), this.like));
		}
		if (this.isActives != null) {
			CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EdgeEntity._isActive));
			for (IsActive item : this.isActives) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.excludedIds != null) {
			CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(EdgeEntity._id));
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
	protected EdgeEntity convert(Tuple tuple, Set<String> columns) {
		EdgeEntity item = new EdgeEntity();
		item.setId(QueryBase.convertSafe(tuple, columns, EdgeEntity._id, UUID.class));
		item.setCode(QueryBase.convertSafe(tuple, columns, EdgeEntity._code, String.class));
		item.setName(QueryBase.convertSafe(tuple, columns, EdgeEntity._name, String.class));
		item.setDescription(QueryBase.convertSafe(tuple, columns, EdgeEntity._description, String.class));
		item.setConfig(QueryBase.convertSafe(tuple, columns, EdgeEntity._config, String.class));
		item.setCreatedAt(QueryBase.convertSafe(tuple, columns, EdgeEntity._createdAt, Instant.class));
		item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, EdgeEntity._updatedAt, Instant.class));
		item.setIsActive(QueryBase.convertSafe(tuple, columns, EdgeEntity._isActive, IsActive.class));
		return item;
	}

	@Override
	protected String fieldNameOf(FieldResolver item) {
		if (item.match(Edge._id)) return EdgeEntity._id;
		else if (item.match(Edge._code)) return EdgeEntity._code;
		else if (item.match(Edge._name)) return EdgeEntity._name;
		else if (item.match(Edge._description)) return EdgeEntity._description;
		else if (item.match(Edge._config)) return EdgeEntity._config;
		else if (item.prefix(Edge._config)) return EdgeEntity._config;
		else if (item.match(Edge._createdAt)) return EdgeEntity._createdAt;
		else if (item.match(Edge._updatedAt)) return EdgeEntity._updatedAt;
		else if (item.match(Edge._isActive)) return EdgeEntity._isActive;
		else return null;
	}

}
