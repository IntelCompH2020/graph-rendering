package gr.cite.intelcomp.graphexplorer.model.builder;

import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.FieldDefinitionEntity;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.model.FieldDefinition;
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
public class FieldDefinitionBuilder extends BaseBuilder<FieldDefinition, FieldDefinitionEntity> {
	private EnumSet<AuthorizationFlags> authorize = EnumSet.of(AuthorizationFlags.None);
	@Autowired
	public FieldDefinitionBuilder(
			ConventionService conventionService
	) {
		super(conventionService, new LoggerService(LoggerFactory.getLogger(FieldDefinitionBuilder.class)));
	}

	public FieldDefinitionBuilder authorize(EnumSet<AuthorizationFlags> values) {
		this.authorize = values;
		return this;
	}

	@Override
	public List<FieldDefinition> build(FieldSet fields, List<FieldDefinitionEntity> datas) throws MyApplicationException {
		this.logger.debug("building for {} items requesting {} fields", Optional.ofNullable(datas).map(e -> e.size()).orElse(0), Optional.ofNullable(fields).map(e -> e.getFields()).map(e -> e.size()).orElse(0));
		this.logger.trace(new DataLogEntry("requested fields", fields));
		if (fields == null || fields.isEmpty()) return new ArrayList<>();

		List<FieldDefinition> models = new ArrayList<>();
		for (FieldDefinitionEntity d : datas) {
			FieldDefinition m = new FieldDefinition();
			if (fields.hasField(this.asIndexer(FieldDefinition._code))) m.setCode(d.getCode());
			if (fields.hasField(this.asIndexer(FieldDefinition._type))) m.setType(d.getType());
			models.add(m);
		}
		this.logger.debug("build {} items", Optional.ofNullable(models).map(e -> e.size()).orElse(0));
		return models;
	}
}
