package gr.cite.intelcomp.graphexplorer.model.builder;

import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.common.JsonHandlingService;
import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.EdgeConfigEntity;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.data.EdgeEntity;
import gr.cite.intelcomp.graphexplorer.model.Edge;
import gr.cite.intelcomp.graphexplorer.model.EdgeAccess;
import gr.cite.intelcomp.graphexplorer.query.EdgeAccessQuery;
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
public class EdgeBuilder extends BaseBuilder<Edge, EdgeEntity> {

	private final QueryFactory queryFactory;
	private final BuilderFactory builderFactory;
	private final JsonHandlingService jsonHandlingService;
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	@Autowired
	public EdgeBuilder(
			ConventionService conventionService,
			QueryFactory queryFactory, BuilderFactory builderFactory, JsonHandlingService jsonHandlingService) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(EdgeBuilder.class)));
		this.queryFactory = queryFactory;
		this.builderFactory = builderFactory;
		this.jsonHandlingService = jsonHandlingService;
	}

	public EdgeBuilder authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	@Override
	public List<Edge> build(FieldSet fields, List<EdgeEntity> datas) throws MyApplicationException {
		this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(datas).map(List::size).orElse(0), Optional.ofNullable(fields).map(FieldSet::getFields).map(Set::size).orElse(0));
		this.logger.trace(new DataLogEntry("requested fields", fields));
		if (fields == null || datas == null || fields.isEmpty()) return new ArrayList<>();

		List<Edge> models = new ArrayList<>();

		FieldSet EdgeAccessesFields = fields.extractPrefixed(this.asPrefix(Edge._edgeAccesses));
		Map<UUID, List<EdgeAccess>> EdgeAccessesMap = this.collectEdgeAccesses(EdgeAccessesFields, datas);
		// TODO make it in bulk
		FieldSet accessRequestConfigFields = fields.extractPrefixed(this.asPrefix(Edge._config));

		for (EdgeEntity d : datas) {
			Edge m = new Edge();
			if (fields.hasField(this.asIndexer(Edge._id))) m.setId(d.getId());
			if (fields.hasField(this.asIndexer(Edge._code))) m.setCode(d.getCode());
			if (fields.hasField(this.asIndexer(Edge._name))) m.setName(d.getName());
			if (fields.hasField(this.asIndexer(Edge._description))) m.setDescription(d.getDescription());
			if (!accessRequestConfigFields.isEmpty() && d.getConfig() != null) {
				EdgeConfigEntity configEntity = this.jsonHandlingService.fromJsonSafe(EdgeConfigEntity.class, d.getConfig());
				if (configEntity != null) m.setConfig(this.builderFactory.builder(EdgeConfigBuilder.class).authorize(this.authorize).build(accessRequestConfigFields, configEntity));
			}
			if (fields.hasField(this.asIndexer(Edge._createdAt))) m.setCreatedAt(d.getCreatedAt());
			if (fields.hasField(this.asIndexer(Edge._updatedAt))) m.setUpdatedAt(d.getUpdatedAt());
			if (fields.hasField(this.asIndexer(Edge._isActive))) m.setIsActive(d.getIsActive());
			if (fields.hasField(this.asIndexer(Edge._hash))) m.setHash(this.hashValue(d.getUpdatedAt()));
			if (!EdgeAccessesFields.isEmpty() && EdgeAccessesMap != null && EdgeAccessesMap.containsKey(d.getId())) m.setEdgeAccesses(EdgeAccessesMap.get(d.getId()));
			models.add(m);
		}
		this.logger.debug("build {} items", Optional.of(models).map(List::size).orElse(0));
		return models;
	}

	private Map<UUID, List<EdgeAccess>> collectEdgeAccesses(FieldSet fields, List<EdgeEntity> datas) throws MyApplicationException {
		if (fields.isEmpty() || datas.isEmpty()) return null;
		this.logger.debug("checking related - {}", EdgeAccess.class.getSimpleName());

		Map<UUID, List<EdgeAccess>> itemMap = null;
		FieldSet clone = new BaseFieldSet(fields.getFields()).ensure(this.asIndexer(EdgeAccess._edge, Edge._id));
		EdgeAccessQuery query = this.queryFactory.query(EdgeAccessQuery.class).authorize(this.authorize).edgeIds(datas.stream().map(x -> x.getId()).distinct().collect(Collectors.toList()));
		itemMap = this.builderFactory.builder(EdgeAccessBuilder.class).authorize(this.authorize).authorize(this.authorize).asMasterKey(query, clone, x -> x.getEdge().getId());

		if (!fields.hasField(this.asIndexer(EdgeAccess._edge, Edge._id))) {
			itemMap.values().stream().flatMap(List::stream).filter(x -> x != null && x.getEdge() != null).map(x -> {
				x.getEdge().setId(null);
				return x;
			}).collect(Collectors.toList());
		}
		return itemMap;
	}

}
