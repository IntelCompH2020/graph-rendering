package gr.cite.intelcomp.graphexplorer.model.builder;

import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.EdgeConfigEntity;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.model.EdgeConfig;
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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EdgeConfigBuilder extends BaseBuilder<EdgeConfig, EdgeConfigEntity> {
	private final BuilderFactory builderFactory;
	@Autowired
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
	public EdgeConfigBuilder(
			ConventionService conventionService,
			BuilderFactory builderFactory) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(EdgeConfigBuilder.class)));
		this.builderFactory = builderFactory;
	}

	public EdgeConfigBuilder authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	@Override
	public List<EdgeConfig> build(FieldSet fields, List<EdgeConfigEntity> datas) throws MyApplicationException {
		this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(datas).map(e -> e.size()).orElse(0), Optional.ofNullable(fields).map(e -> e.getFields()).map(e -> e.size()).orElse(0));
		this.logger.trace(new DataLogEntry("requested fields", fields));
		if (fields == null || fields.isEmpty()) return new ArrayList<>();
		FieldSet fieldsFields = fields.extractPrefixed(this.asPrefix(EdgeConfig._fields));

		List<EdgeConfig> models = new ArrayList<>();
		for (EdgeConfigEntity d : datas) {
			EdgeConfig m = new EdgeConfig();
			if (!fieldsFields.isEmpty() && d.getFields() != null) m.setFields(this.builderFactory.builder(FieldDefinitionBuilder.class).authorize(this.authorize).build(fieldsFields, d.getFields()));
			models.add(m);
		}
		this.logger.debug("build {} items", Optional.ofNullable(models).map(e -> e.size()).orElse(0));
		return models;
	}
}
