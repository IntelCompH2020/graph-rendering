package gr.cite.intelcomp.graphexplorer.query;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationContentResolver;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.authorization.Permission;
import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.common.scope.user.UserScope;
import gr.cite.intelcomp.graphexplorer.data.GraphEntity;
import gr.cite.intelcomp.graphexplorer.model.Graph;
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
public class GraphQuery extends QueryBase<GraphEntity> {

	private String like;
	private Collection<UUID> ids;
	private Collection<String> codes;
	private Collection<IsActive> isActives;
	private Collection<UUID> excludedIds;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	public GraphQuery like(String value) {
		this.like = value;
		return this;
	}

	public GraphQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public GraphQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public GraphQuery ids(Collection<UUID> values) {
		this.ids = values;
		return this;
	}

	public GraphQuery codes(String value) {
		this.codes = List.of(value);
		return this;
	}

	public GraphQuery codes(String... value) {
		this.codes = Arrays.asList(value);
		return this;
	}

	public GraphQuery codes(Collection<String> values) {
		this.codes = values;
		return this;
	}

	public GraphQuery isActive(IsActive value) {
		this.isActives = List.of(value);
		return this;
	}

	public GraphQuery isActive(IsActive... value) {
		this.isActives = Arrays.asList(value);
		return this;
	}

	public GraphQuery isActive(Collection<IsActive> values) {
		this.isActives = values;
		return this;
	}

	public GraphQuery excludedIds(Collection<UUID> values) {
		this.excludedIds = values;
		return this;
	}

	public GraphQuery excludedIds(UUID value) {
		this.excludedIds = List.of(value);
		return this;
	}

	public GraphQuery excludedIds(UUID... value) {
		this.excludedIds = Arrays.asList(value);
		return this;
	}

	public GraphQuery authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	private final UserScope userScope;
	private final AuthorizationService authService;
	private final AuthorizationContentResolver authorizationContentResolver;

	public GraphQuery(
			UserScope userScope,
			AuthorizationService authService,
			AuthorizationContentResolver authorizationContentResolver) {
		this.userScope = userScope;
		this.authService = authService;
		this.authorizationContentResolver = authorizationContentResolver;
	}

	@Override
	protected Class<GraphEntity> entityClass() {
		return GraphEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.ids) || this.isEmpty(this.isActives) || this.isEmpty(this.codes) || this.isEmpty(this.excludedIds);
	}

	@Override
	protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
		if (this.authorize.contains(AuthorizationFlags.None)) return null;
		if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseGraph)) return null;
		List<UUID> allowedGraphIds = null;
		if (this.authorize.contains(AuthorizationFlags.Affiliated)) allowedGraphIds = this.authorizationContentResolver.affiliatedGraphs(Permission.BrowseGraph);;

		List<Predicate> predicates = new ArrayList<>();
		if (allowedGraphIds != null) {
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(GraphEntity._id));
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
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(GraphEntity._id));
			for (UUID item : this.ids) inClause.value(item);
			predicates.add(inClause);
		}

		if (this.like != null && !this.like.isEmpty()) {
			predicates.add(queryContext.CriteriaBuilder.like(queryContext.Root.get(GraphEntity._name), this.like));
		}
		if (this.isActives != null) {
			CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(GraphEntity._isActive));
			for (IsActive item : this.isActives) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.excludedIds != null) {
			CriteriaBuilder.In<UUID> notInClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(GraphEntity._id));
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
	protected GraphEntity convert(Tuple tuple, Set<String> columns) {
		GraphEntity item = new GraphEntity();
		item.setId(QueryBase.convertSafe(tuple, columns, GraphEntity._id, UUID.class));
		item.setName(QueryBase.convertSafe(tuple, columns, GraphEntity._name, String.class));
		item.setDescription(QueryBase.convertSafe(tuple, columns, GraphEntity._description, String.class));
		item.setCreatedAt(QueryBase.convertSafe(tuple, columns, GraphEntity._createdAt, Instant.class));
		item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, GraphEntity._updatedAt, Instant.class));
		item.setIsActive(QueryBase.convertSafe(tuple, columns, GraphEntity._isActive, IsActive.class));
		return item;
	}

	@Override
	protected String fieldNameOf(FieldResolver item) {
		if (item.match(Graph._id)) return GraphEntity._id;
		else if (item.match(Graph._name)) return GraphEntity._name;
		else if (item.match(Graph._description)) return GraphEntity._description;
		else if (item.match(Graph._createdAt)) return GraphEntity._createdAt;
		else if (item.match(Graph._updatedAt)) return GraphEntity._updatedAt;
		else if (item.match(Graph._isActive)) return GraphEntity._isActive;
		else return null;
	}

}
