package gr.cite.intelcomp.graphexplorer.elastic.converter;

import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.FieldDefinitionEntity;
import gr.cite.intelcomp.graphexplorer.elastic.data.EdgeDataEntity;
import gr.cite.intelcomp.graphexplorer.service.edge.EdgeConfigItem;
import gr.cite.intelcomp.graphexplorer.service.edge.EdgeConfigService;
import gr.cite.tools.exception.MyApplicationException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.util.LinkedHashMap;
import java.util.Map;

@WritingConverter
public class EdgeDataEntityConverter implements Converter<EdgeDataEntity, Map<String, Object>> {
	private final ApplicationContext applicationContext;

	public EdgeDataEntityConverter(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public Map<String, Object> convert(EdgeDataEntity source) {
		Map<String, Object> target = new LinkedHashMap<>();
		if (source.getId() != null) target.put(EdgeDataEntity.Fields.id, source.getId());
		if (source.getLabel() != null) target.put(EdgeDataEntity.Fields.label, source.getLabel());
		if (source.getSourceId() != null) target.put(EdgeDataEntity.Fields.sourceId, source.getSourceId());
		if (source.getTargetId() != null) target.put(EdgeDataEntity.Fields.targetId, source.getTargetId());
		if (source.getWeight() != null) target.put(EdgeDataEntity.Fields.weight, source.getWeight());
		EdgeConfigService edgeConfigService = this.applicationContext.getBean(EdgeConfigService.class);
		EdgeConfigItem item = edgeConfigService.getConfig(source.getEdgeId());
		if (item != null && source.getProperties() != null && item.getConfigEntity() != null && item.getConfigEntity().getFields() != null) {
			for (Map.Entry<String, Object> prop : source.getProperties().entrySet()) {
				FieldDefinitionEntity fieldEntity = item.getConfigEntity().getFields().stream().filter(x-> x.getCode().equals(prop.getKey())).findFirst().orElse(null);

				if (prop.getKey() != null && fieldEntity != null) {
					switch (fieldEntity.getType()) {
						case String:
						case Integer:
						case Double:
						case Date: {
							target.put(edgeConfigService.ensurePropertyName(prop.getKey()), prop.getValue());
							break;
						}
						default:
							throw new MyApplicationException("invalid type " + fieldEntity.getType());
					}
				}
			}
		}
		return target;
	}
}
