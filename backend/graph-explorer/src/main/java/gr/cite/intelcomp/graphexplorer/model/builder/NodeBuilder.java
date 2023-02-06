package gr.cite.intelcomp.graphexplorer.model.builder;

import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.common.JsonHandlingService;
import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.NodeConfigEntity;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.data.NodeEntity;
import gr.cite.intelcomp.graphexplorer.model.Node;
import gr.cite.intelcomp.graphexplorer.model.NodeAccess;
import gr.cite.intelcomp.graphexplorer.query.NodeAccessQuery;
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
public class NodeBuilder extends BaseBuilder<Node, NodeEntity> {

	private final QueryFactory queryFactory;
	private final BuilderFactory builderFactory;
	private final JsonHandlingService jsonHandlingService;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	@Autowired
	public NodeBuilder(
			ConventionService conventionService,
			QueryFactory queryFactory, BuilderFactory builderFactory, JsonHandlingService jsonHandlingService) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(NodeBuilder.class)));
		this.queryFactory = queryFactory;
		this.builderFactory = builderFactory;
		this.jsonHandlingService = jsonHandlingService;
	}

	public NodeBuilder authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	@Override
	public List<Node> build(FieldSet fields, List<NodeEntity> datas) throws MyApplicationException {
		this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(datas).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
		this.logger.trace(new DataLogEntry("requested fields", fields));
		if (fields == null || datas == null || fields.isEmpty()) return new ArrayList<>();

		List<Node> models = new ArrayList<>();

		FieldSet NodeAccessesFields = fields.extractPrefixed(this.asPrefix(Node._nodeAccesses));
		Map<UUID, List<NodeAccess>> NodeAccessesMap = this.collectNodeAccesses(NodeAccessesFields, datas);
		// TODO make it in bulk
		FieldSet accessRequestConfigFields = fields.extractPrefixed(this.asPrefix(Node._config));

		for (NodeEntity d : datas) {
			Node m = new Node();
			if (fields.hasField(this.asIndexer(Node._id))) m.setId(d.getId());
			if (fields.hasField(this.asIndexer(Node._code))) m.setCode(d.getCode());
			if (fields.hasField(this.asIndexer(Node._name))) m.setName(d.getName());
			if (fields.hasField(this.asIndexer(Node._description))) m.setDescription(d.getDescription());
			if (!accessRequestConfigFields.isEmpty() && d.getConfig() != null) {
				NodeConfigEntity configEntity = this.jsonHandlingService.fromJsonSafe(NodeConfigEntity.class, d.getConfig());
				if (configEntity != null) m.setConfig(this.builderFactory.builder(NodeConfigBuilder.class).authorize(this.authorize).build(accessRequestConfigFields, configEntity));
			}
			if (fields.hasField(this.asIndexer(Node._createdAt))) m.setCreatedAt(d.getCreatedAt());
			if (fields.hasField(this.asIndexer(Node._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
			if (fields.hasField(this.asIndexer(Node._isActive))) m.setIsActive(d.getIsActive());
			if (fields.hasField(this.asIndexer(Node._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
			if (!NodeAccessesFields.isEmpty() && NodeAccessesMap != null && NodeAccessesMap.containsKey(d.getId())) m.setNodeAccesses(NodeAccessesMap.get(d.getId()));
			models.add(m);
		}
		this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
		return models;
	}

	private Map<UUID, List<NodeAccess>> collectNodeAccesses(FieldSet fields, List<NodeEntity> datas) throws MyApplicationException {
		if (fields.isEmpty() || datas.isEmpty()) return null;
		this.logger.debug("checking related - {}", NodeAccess.class.getSimpleName());

		Map<UUID, List<NodeAccess>> itemMap = null;
		FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(NodeAccess._node, Node._id));
		NodeAccessQuery query = this.queryFactory.query(NodeAccessQuery.class).authorize(this.authorize).nodeIds(datas.stream().map(x -> x.getId()).distinct().collect(Collectors.toList()));
		itemMap = this.builderFactory.builder(NodeAccessBuilder.class).authorize(this.authorize).authorize(this.authorize).asMasterKey(query, clone, x -> x.getNode().getId());

		if (!fields.hasField(this.asIndexer(NodeAccess._node, Node._id))) {
			itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getNode() != null).map(x -> {
				x.getNode().setId(null);
				return x;
			}).collect(Collectors.toList());
		}
		return itemMap;
	}

}
