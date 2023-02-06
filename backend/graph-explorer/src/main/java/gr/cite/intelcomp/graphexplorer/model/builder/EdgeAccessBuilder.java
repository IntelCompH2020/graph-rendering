package gr.cite.intelcomp.graphexplorer.model.builder;

import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.data.EdgeAccessEntity;
import gr.cite.intelcomp.graphexplorer.model.Edge;
import gr.cite.intelcomp.graphexplorer.model.EdgeAccess;
import gr.cite.intelcomp.graphexplorer.model.User;
import gr.cite.intelcomp.graphexplorer.query.EdgeQuery;
import gr.cite.intelcomp.graphexplorer.query.UserQuery;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EdgeAccessBuilder extends BaseBuilder<EdgeAccess, EdgeAccessEntity> {

	private final QueryFactory queryFactory;
	private final BuilderFactory builderFactory;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	@Autowired
	public EdgeAccessBuilder(
			ConventionService conventionService,
			QueryFactory queryFactory, BuilderFactory builderFactory) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(EdgeAccessBuilder.class)));
		this.queryFactory = queryFactory;
		this.builderFactory = builderFactory;
	}

	public EdgeAccessBuilder authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	@Override
	public List<EdgeAccess> build(FieldSet fields, List<EdgeAccessEntity> data) throws MyApplicationException {
		this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
		this.logger.trace(new DataLogEntry("requested fields", fields));
		if (fields == null || data == null || fields.isEmpty()) return new ArrayList<>();

		FieldSet EdgeFields = fields.extractPrefixed(this.asPrefix(EdgeAccess._edge));
		Map<UUID, Edge> EdgeItemsMap = this.collectEdges(EdgeFields, data);

		FieldSet userFields = fields.extractPrefixed(this.asPrefix(EdgeAccess._user));
		Map<UUID, User> userItemsMap = this.collectUsers(userFields, data);

		List<EdgeAccess> models = new ArrayList<>();

		for (EdgeAccessEntity d : data) {
			EdgeAccess m = new EdgeAccess();
			if (fields.hasField(this.asIndexer(EdgeAccess._id))) m.setId(d.getId());
			if (fields.hasField(this.asIndexer(EdgeAccess._isActive))) m.setIsActive(d.getIsActive());
			if (fields.hasField(this.asIndexer(EdgeAccess._createdAt))) m.setCreatedAt(d.getCreatedAt());
			if (fields.hasField(this.asIndexer(EdgeAccess._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
			if (fields.hasField(this.asIndexer(EdgeAccess._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
			if (!EdgeFields.isEmpty() && EdgeItemsMap != null && EdgeItemsMap.containsKey(d.getEdgeId())) m.setEdge(EdgeItemsMap.get(d.getEdgeId()));
			if (!userFields.isEmpty() && userItemsMap != null && userItemsMap.containsKey(d.getUserId())) m.setUser(userItemsMap.get(d.getUserId()));
			models.add(m);
		}
		this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
		return models;
	}

	private Map<UUID, User> collectUsers(FieldSet fields, List<EdgeAccessEntity> data) throws MyApplicationException {
		if (fields.isEmpty() || data.isEmpty()) return null;
		this.logger.debug("checking related - {}", User.class.getSimpleName());

		Map<UUID, User> itemMap;
		if (!fields.hasOtherField(this.asIndexer(User._id))) {
			itemMap = this.asEmpty(
					data.stream().map(x -> x.getUserId()).distinct().collect(Collectors.toList()),
					x -> {
						User item = new User();
						item.setId(x);
						return item;
					},
					User::getId);
		} else {
			FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(User._id);
			UserQuery q = this.queryFactory.query(UserQuery.class).authorize(this.authorize).ids(data.stream().map(x -> x.getUserId()).distinct().collect(Collectors.toList()));
			itemMap = this.builderFactory.builder(UserBuilder.class).authorize(this.authorize).asForeignKey(q, clone, User::getId);
		}
		if (!fields.hasField(User._id)) {
			itemMap.values().stream().filter(Objects::nonNull).peek(x -> x.setId(null)).collect(Collectors.toList());
		}

		return itemMap;
	}

	private Map<UUID, Edge> collectEdges(FieldSet fields, List<EdgeAccessEntity> data) throws MyApplicationException {
		if (fields.isEmpty() || data.isEmpty()) return null;
		this.logger.debug("checking related - {}", Edge.class.getSimpleName());

		Map<UUID, Edge> itemMap;
		if (!fields.hasOtherField(this.asIndexer(Edge._id))) {
			itemMap = this.asEmpty(
					data.stream().map(x -> x.getEdgeId()).distinct().collect(Collectors.toList()),
					x -> {
						Edge item = new Edge();
						item.setId(x);
						return item;
					},
					Edge::getId);
		} else {
			FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Edge._id);
			EdgeQuery q = this.queryFactory.query(EdgeQuery.class).authorize(this.authorize).ids(data.stream().map(x -> x.getEdgeId()).distinct().collect(Collectors.toList()));
			itemMap = this.builderFactory.builder(EdgeBuilder.class).authorize(this.authorize).asForeignKey(q, clone, Edge::getId);
		}
		if (!fields.hasField(Edge._id)) {
			itemMap.values().stream().filter(Objects::nonNull).peek(x -> x.setId(null)).collect(Collectors.toList());
		}

		return itemMap;
	}

}
