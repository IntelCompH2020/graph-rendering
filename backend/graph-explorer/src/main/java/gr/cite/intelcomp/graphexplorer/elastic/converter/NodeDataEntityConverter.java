package gr.cite.intelcomp.graphexplorer.elastic.converter;

import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.FieldDefinitionEntity;
import gr.cite.intelcomp.graphexplorer.elastic.data.NodeDataEntity;
import gr.cite.intelcomp.graphexplorer.service.node.NodeConfigItem;
import gr.cite.intelcomp.graphexplorer.service.node.NodeConfigService;
import gr.cite.tools.exception.MyApplicationException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.util.LinkedHashMap;
import java.util.Map;

@WritingConverter
public class NodeDataEntityConverter implements Converter<NodeDataEntity, Map<String, Object>> {
	private final ApplicationContext applicationContext;

	public NodeDataEntityConverter(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public Map<String, Object> convert(NodeDataEntity source) {
		Map<String, Object> target = new LinkedHashMap<>();
		if (source.getId() != null) target.put(NodeDataEntity.Fields.id, source.getId());
		if (source.getName() != null) target.put(NodeDataEntity.Fields.name, source.getName());
		if (source.getLabel() != null) target.put(NodeDataEntity.Fields.label, source.getLabel());
		if (source.getX() != null) target.put(NodeDataEntity.Fields.x, source.getX());
		if (source.getY() != null) target.put(NodeDataEntity.Fields.y, source.getY());
		NodeConfigService nodeConfigService = this.applicationContext.getBean(NodeConfigService.class);
		NodeConfigItem item = nodeConfigService.getConfig(source.getNodeId());
		if (item != null && source.getProperties() != null && item.getConfigEntity() != null && item.getConfigEntity().getFields() != null) {
			for (Map.Entry<String, Object> prop : source.getProperties().entrySet()) {
				FieldDefinitionEntity fieldEntity = item.getConfigEntity().getFields().stream().filter(x-> x.getCode().equals(prop.getKey())).findFirst().orElse(null);

				if (prop.getKey() != null && fieldEntity != null) {
					switch (fieldEntity.getType()) {
						case String:
						case Integer:
						case Double:
						case Date: {
							target.put(nodeConfigService.ensurePropertyName(prop.getKey()), prop.getValue());
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
