package gr.cite.intelcomp.graphexplorer.model.builder;

import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.common.JsonHandlingService;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.data.NodeAccessEntity;
import gr.cite.intelcomp.graphexplorer.model.Node;
import gr.cite.intelcomp.graphexplorer.model.NodeAccess;
import gr.cite.intelcomp.graphexplorer.model.User;
import gr.cite.intelcomp.graphexplorer.query.NodeQuery;
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
public class NodeAccessBuilder extends BaseBuilder<NodeAccess, NodeAccessEntity> {

	private final QueryFactory queryFactory;
	private final BuilderFactory builderFactory;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	@Autowired
	public NodeAccessBuilder(
			ConventionService conventionService,
			QueryFactory queryFactory, BuilderFactory builderFactory) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(NodeAccessBuilder.class)));
		this.queryFactory = queryFactory;
		this.builderFactory = builderFactory;
	}

	public NodeAccessBuilder authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	@Override
	public List<NodeAccess> build(FieldSet fields, List<NodeAccessEntity> data) throws MyApplicationException {
		this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(data).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
		this.logger.trace(new DataLogEntry("requested fields", fields));
		if (fields == null || data == null || fields.isEmpty()) return new ArrayList<>();

		FieldSet NodeFields = fields.extractPrefixed(this.asPrefix(NodeAccess._node));
		Map<UUID, Node> NodeItemsMap = this.collectNodes(NodeFields, data);

		FieldSet userFields = fields.extractPrefixed(this.asPrefix(NodeAccess._user));
		Map<UUID, User> userItemsMap = this.collectUsers(userFields, data);

		List<NodeAccess> models = new ArrayList<>();

		for (NodeAccessEntity d : data) {
			NodeAccess m = new NodeAccess();
			if (fields.hasField(this.asIndexer(NodeAccess._id))) m.setId(d.getId());
			if (fields.hasField(this.asIndexer(NodeAccess._isActive))) m.setIsActive(d.getIsActive());
			if (fields.hasField(this.asIndexer(NodeAccess._createdAt))) m.setCreatedAt(d.getCreatedAt());
			if (fields.hasField(this.asIndexer(NodeAccess._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
			if (fields.hasField(this.asIndexer(NodeAccess._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
			if (!NodeFields.isEmpty() && NodeItemsMap != null && NodeItemsMap.containsKey(d.getNodeId())) m.setNode(NodeItemsMap.get(d.getNodeId()));
			if (!userFields.isEmpty() && userItemsMap != null && userItemsMap.containsKey(d.getUserId())) m.setUser(userItemsMap.get(d.getUserId()));
			models.add(m);
		}
		this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
		return models;
	}

	private Map<UUID, User> collectUsers(FieldSet fields, List<NodeAccessEntity> data) throws MyApplicationException {
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

	private Map<UUID, Node> collectNodes(FieldSet fields, List<NodeAccessEntity> data) throws MyApplicationException {
		if (fields.isEmpty() || data.isEmpty()) return null;
		this.logger.debug("checking related - {}", Node.class.getSimpleName());

		Map<UUID, Node> itemMap;
		if (!fields.hasOtherField(this.asIndexer(Node._id))) {
			itemMap = this.asEmpty(
					data.stream().map(x -> x.getNodeId()).distinct().collect(Collectors.toList()),
					x -> {
						Node item = new Node();
						item.setId(x);
						return item;
					},
					Node::getId);
		} else {
			FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(Node._id);
			NodeQuery q = this.queryFactory.query(NodeQuery.class).authorize(this.authorize).ids(data.stream().map(x -> x.getNodeId()).distinct().collect(Collectors.toList()));
			itemMap = this.builderFactory.builder(NodeBuilder.class).authorize(this.authorize).asForeignKey(q, clone, Node::getId);
		}
		if (!fields.hasField(Node._id)) {
			itemMap.values().stream().filter(Objects::nonNull).peek(x -> x.setId(null)).collect(Collectors.toList());
		}

		return itemMap;
	}

}
