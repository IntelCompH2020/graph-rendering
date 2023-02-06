package gr.cite.intelcomp.graphexplorer.model.builder;

import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.data.GraphAccessEntity;
import gr.cite.intelcomp.graphexplorer.model.Graph;
import gr.cite.intelcomp.graphexplorer.model.GraphAccess;
import gr.cite.intelcomp.graphexplorer.model.User;
import gr.cite.intelcomp.graphexplorer.query.GraphQuery;
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
public class GraphAccessBuilder extends BaseBuilder<GraphAccess, GraphAccessEntity> {

	private final QueryFactory queryFactory;
	private final BuilderFactory builderFactory;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	@Autowired
	public GraphAccessBuilder(
			ConventionService conventionService,
			QueryFactory queryFactory, BuilderFactory builderFactory) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(GraphAccessBuilder.class)));
		this.queryFactory = queryFactory;
		this.builderFactory = builderFactory;
	}

	public GraphAccessBuilder authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	@Override
	public List<GraphAccess> build(FieldSet fields, List<GraphAccessEntity> data) throws MyApplicationException {
		this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
		this.logger.trace(new DataLogEntry("requested fields", fields));
		if (fields == null || data == null || fields.isEmpty()) return new ArrayList<>();

		FieldSet graphFields = fields.extractPrefixed(this.asPrefix(GraphAccess._graph));
		Map<UUID, Graph> graphItemsMap = this.collectGraphs(graphFields, data);

		FieldSet userFields = fields.extractPrefixed(this.asPrefix(GraphAccess._user));
		Map<UUID, User> userItemsMap = this.collectUsers(userFields, data);

		List<GraphAccess> models = new ArrayList<>();

		for (GraphAccessEntity d : data) {
			GraphAccess m = new GraphAccess();
			if (fields.hasField(this.asIndexer(GraphAccess._id))) m.setId(d.getId());
			if (fields.hasField(this.asIndexer(GraphAccess._isActive))) m.setIsActive(d.getIsActive());
			if (fields.hasField(this.asIndexer(GraphAccess._createdAt))) m.setCreatedAt(d.getCreatedAt());
			if (fields.hasField(this.asIndexer(GraphAccess._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
			if (fields.hasField(this.asIndexer(GraphAccess._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
			if (!graphFields.isEmpty() && graphItemsMap != null && graphItemsMap.containsKey(d.getGraphId())) m.setGraph(graphItemsMap.get(d.getGraphId()));
			if (!userFields.isEmpty() && userItemsMap != null && userItemsMap.containsKey(d.getUserId())) m.setUser(userItemsMap.get(d.getUserId()));
			models.add(m);
		}
		this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
		return models;
	}

	private Map<UUID, User> collectUsers(FieldSet fields, List<GraphAccessEntity> data) throws MyApplicationException {
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

	private Map<UUID, Graph> collectGraphs(FieldSet fields, List<GraphAccessEntity> data) throws MyApplicationException {
		if (fields.isEmpty() || data.isEmpty()) return null;
		this.logger.debug("checking related - {}", Graph.class.getSimpleName());

		Map<UUID, Graph> itemMap;
		if (!fields.hasOtherField(this.asIndexer(Graph._id))) {
			itemMap = this.asEmpty(
					data.stream().map(x -> x.getGraphId()).distinct().collect(Collectors.toList()),
					x -> {
						Graph item = new Graph();
						item.setId(x);
						return item;
					},
					Graph::getId);
		} else {
			FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Graph._id);
			GraphQuery q = this.queryFactory.query(GraphQuery.class).authorize(this.authorize).ids(data.stream().map(x -> x.getGraphId()).distinct().collect(Collectors.toList()));
			itemMap = this.builderFactory.builder(GraphBuilder.class).authorize(this.authorize).asForeignKey(q, clone, Graph::getId);
		}
		if (!fields.hasField(Graph._id)) {
			itemMap.values().stream().filter(Objects::nonNull).peek(x -> x.setId(null)).collect(Collectors.toList());
		}

		return itemMap;
	}

}
