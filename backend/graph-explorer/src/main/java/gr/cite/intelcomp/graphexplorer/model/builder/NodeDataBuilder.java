package gr.cite.intelcomp.graphexplorer.model.builder;

import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.common.types.graphdata.NodeDataEntity;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.model.NodeData;
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
public class NodeDataBuilder extends BaseBuilder<NodeData, NodeDataEntity> {
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);

	@Autowired
	public NodeDataBuilder(
			ConventionService conventionService
	) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(NodeDataBuilder.class)));
	}

	public NodeDataBuilder authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	@Override
	public List<NodeData> build(FieldSet fields, List<NodeDataEntity> datas) throws MyApplicationException {
		this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(datas).map(e -> e.size()).orElse(0), Optional.ofNullable(fields).map(e -> e.getFields()).map(e -> e.size()).orElse(0));
		this.logger.trace(new DataLogEntry("requested fields", fields));
		if (fields == null || fields.isEmpty()) return new ArrayList<>();

		List<NodeData> models = new ArrayList<>();
		for (NodeDataEntity d : datas) {
			NodeData m = new NodeData();
			if (fields.hasField(this.asIndexer(NodeData._id))) m.setId(d.getId());
			if (fields.hasField(this.asIndexer(NodeData._x))) m.setX(d.getX());
			if (fields.hasField(this.asIndexer(NodeData._label))) m.setLabel(d.getLabel());
			if (fields.hasField(this.asIndexer(NodeData._y))) m.setY(d.getY());
			if (fields.hasField(this.asIndexer(NodeData._name))) m.setName(d.getName());
			if (d.getProperties() != null && d.getProperties().keySet() != null) {
				Map<String, Object> properties = new HashMap<>();
				for (String prop : d.getProperties().keySet()) {
					properties.put(prop, d.getProperties().get(prop));
				}
				if (properties.size() > 0) m.setProperties(properties);
			}
			models.add(m);
		}
		this.logger.debug("build {} items", Optional.ofNullable(models).map(e -> e.size()).orElse(0));
		return models;
	}
}
