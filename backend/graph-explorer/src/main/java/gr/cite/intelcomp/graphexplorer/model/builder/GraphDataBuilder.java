package gr.cite.intelcomp.graphexplorer.model.builder;

import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.common.types.graphdata.GraphDataEntity;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.model.GraphData;
import gr.cite.intelcomp.graphexplorer.model.NodeData;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.DataLogEntry;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GraphDataBuilder extends BaseBuilder<GraphData, GraphDataEntity> {
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
	private final BuilderFactory builderFactory;

	@Autowired
	public GraphDataBuilder(
			ConventionService conventionService,
			BuilderFactory builderFactory) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(GraphDataBuilder.class)));
		this.builderFactory = builderFactory;
	}

	public GraphDataBuilder authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	@Override
	public List<GraphData> build(FieldSet fields, List<GraphDataEntity> datas) throws MyApplicationException {
		this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(datas).map(e -> e.size()).orElse(0), Optional.ofNullable(fields).map(e -> e.getFields()).map(e -> e.size()).orElse(0));
		this.logger.trace(new DataLogEntry("requested fields", fields));
		if (fields == null || fields.isEmpty()) return new ArrayList<>();

		List<GraphData> models = new ArrayList<>();
		FieldSet edgesFields = fields.extractPrefixed(this.asPrefix(GraphData._edges));
		FieldSet nodesFields = fields.extractPrefixed(this.asPrefix(GraphData._nodes));

		for (GraphDataEntity d : datas) {
			GraphData m = new GraphData();
			if (!edgesFields.isEmpty() && d.getEdges() != null) m.setEdges(this.builderFactory.builder(EdgeDataBuilder.class).authorize(this.authorize).build(edgesFields, d.getEdges()));
			if (!nodesFields.isEmpty() && d.getNodes() != null) m.setNodes(this.builderFactory.builder(NodeDataBuilder.class).authorize(this.authorize).build(nodesFields, d.getNodes()));
			if (fields.hasField(this.asIndexer(GraphData._size))) m.setSize(d.getSize());
			models.add(m);
		}
		this.logger.debug("build {} items", Optional.ofNullable(models).map(e -> e.size()).orElse(0));
		return models;
	}
}
