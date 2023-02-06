package gr.cite.intelcomp.graphexplorer.query;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.authorization.Permission;
import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.common.scope.user.UserScope;
import gr.cite.intelcomp.graphexplorer.data.UserEntity;
import gr.cite.intelcomp.graphexplorer.model.User;
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
public class UserQuery extends QueryBase<UserEntity> {

	private String like;
	private Collection<UUID> ids;
	private Collection<IsActive> isActives;

	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	private final UserScope userScope;
	private final AuthorizationService authService;

	public UserQuery(
			UserScope userScope,
			AuthorizationService authService
	) {
		this.userScope = userScope;
		this.authService = authService;
	}

	public UserQuery like(String value) {
		this.like = value;
		return this;
	}

	public UserQuery ids(UUID value) {
		this.ids = List.of(value);
		return this;
	}

	public UserQuery ids(UUID... value) {
		this.ids = Arrays.asList(value);
		return this;
	}

	public UserQuery ids(Collection<UUID> values) {
		this.ids = values;
		return this;
	}

	public UserQuery isActive(IsActive value) {
		this.isActives = List.of(value);
		return this;
	}

	public UserQuery isActive(IsActive... value) {
		this.isActives = Arrays.asList(value);
		return this;
	}

	public UserQuery isActive(Collection<IsActive> values) {
		this.isActives = values;
		return this;
	}

	public UserQuery authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	@Override
	protected Class<UserEntity> entityClass() {
		return UserEntity.class;
	}

	@Override
	protected Boolean isFalseQuery() {
		return this.isEmpty(this.ids) || this.isEmpty(this.isActives);
	}

	@Override
	protected <X, Y> Predicate applyAuthZ(QueryContext<X, Y> queryContext) {
		if (this.authorize.contains(AuthorizationFlags.None)) return null;
		if (this.authorize.contains(AuthorizationFlags.Permission) && this.authService.authorize(Permission.BrowseUser)) return null;
		UUID ownerId = null;
		if (this.authorize.contains(AuthorizationFlags.Owner)) ownerId = this.userScope.getUserIdSafe();

		List<Predicate> predicates = new ArrayList<>();
		if (ownerId != null) {
			predicates.add(queryContext.CriteriaBuilder.equal(queryContext.Root.get(User._id), ownerId));
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
			CriteriaBuilder.In<UUID> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserEntity._id));
			for (UUID item : this.ids) inClause.value(item);
			predicates.add(inClause);
		}
		if (this.like != null && !this.like.isEmpty()) {
			predicates.add(queryContext.CriteriaBuilder.or(queryContext.CriteriaBuilder.like(queryContext.Root.get(UserEntity._firstName), this.like),
					queryContext.CriteriaBuilder.like(queryContext.Root.get(UserEntity._lastName), this.like)
			));
		}
		if (this.isActives != null) {
			CriteriaBuilder.In<IsActive> inClause = queryContext.CriteriaBuilder.in(queryContext.Root.get(UserEntity._isActive));
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
	protected UserEntity convert(Tuple tuple, Set<String> columns) {
		UserEntity item = new UserEntity();
		item.setId(QueryBase.convertSafe(tuple, columns, UserEntity._id, UUID.class));
		item.setFirstName(QueryBase.convertSafe(tuple, columns, UserEntity._firstName, String.class));
		item.setLastName(QueryBase.convertSafe(tuple, columns, UserEntity._lastName, String.class));
		item.setTimezone(QueryBase.convertSafe(tuple, columns, UserEntity._timezone, String.class));
		item.setCulture(QueryBase.convertSafe(tuple, columns, UserEntity._culture, String.class));
		item.setLanguage(QueryBase.convertSafe(tuple, columns, UserEntity._language, String.class));
		item.setSubjectId(QueryBase.convertSafe(tuple, columns, UserEntity._subjectId, String.class));
		item.setCreatedAt(QueryBase.convertSafe(tuple, columns, UserEntity._createdAt, Instant.class));
		item.setUpdatedAt(QueryBase.convertSafe(tuple, columns, UserEntity._updatedAt, Instant.class));
		item.setIsActive(QueryBase.convertSafe(tuple, columns, UserEntity._isActive, IsActive.class));
		return item;
	}

	@Override
	protected String fieldNameOf(FieldResolver item) {
		if (item.match(User._id)) return UserEntity._id;
		else if (item.match(User._firstName)) return UserEntity._firstName;
		else if (item.match(User._lastName)) return UserEntity._lastName;
		else if (item.match(User._timezone)) return UserEntity._timezone;
		else if (item.match(User._culture)) return UserEntity._culture;
		else if (item.match(User._language)) return UserEntity._language;
		else if (item.match(User._subjectId)) return UserEntity._subjectId;
		else if (item.match(User._isActive)) return UserEntity._isActive;
		else if (item.match(User._createdAt)) return UserEntity._createdAt;
		else if (item.match(User._updatedAt)) return UserEntity._updatedAt;
		else if (item.match(User._hash)) return UserEntity._updatedAt;
		else return null;
	}
}
