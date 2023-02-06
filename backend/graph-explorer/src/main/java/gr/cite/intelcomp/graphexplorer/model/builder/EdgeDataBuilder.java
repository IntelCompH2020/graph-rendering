package gr.cite.intelcomp.graphexplorer.model.builder;

import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.common.types.graphdata.EdgeDataEntity;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.model.EdgeData;
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
public class EdgeDataBuilder extends BaseBuilder<EdgeData, EdgeDataEntity> {
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
	@Autowired
	public EdgeDataBuilder(
			ConventionService conventionService
	) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(EdgeDataBuilder.class)));
	}

	public EdgeDataBuilder authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	@Override
	public List<EdgeData> build(FieldSet fields, List<EdgeDataEntity> datas) throws MyApplicationException {
		this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(datas).map(e -> e.size()).orElse(0), Optional.ofNullable(fields).map(e -> e.getFields()).map(e -> e.size()).orElse(0));
		this.logger.trace(new DataLogEntry("requested fields", fields));
		if (fields == null || fields.isEmpty()) return new ArrayList<>();

		List<EdgeData> models = new ArrayList<>();
		for (EdgeDataEntity d : datas) {
			EdgeData m = new EdgeData();
			if (fields.hasField(this.asIndexer(EdgeData._id))) m.setId(d.getId());
			if (fields.hasField(this.asIndexer(EdgeData._label))) m.setLabel(d.getLabel());
			if (fields.hasField(this.asIndexer(EdgeData._targetId))) m.setTargetId(d.getTargetId());
			if (fields.hasField(this.asIndexer(EdgeData._sourceId))) m.setSourceId(d.getSourceId());
			if (fields.hasField(this.asIndexer(EdgeData._weight))) m.setWeight(d.getWeight());
			if (d.getProperties() != null) {
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
