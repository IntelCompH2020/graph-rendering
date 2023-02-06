package gr.cite.intelcomp.graphexplorer.service.node;

import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.NodeConfigEntity;
import java.util.UUID;

public class NodeConfigItem {
	private final UUID id;
	private final String code;
	private final NodeConfigEntity configEntity;

	public NodeConfigItem(UUID id, String code, NodeConfigEntity configEntity) {
		this.id = id;
		this.code = code;
		this.configEntity = configEntity;
	}

	public UUID getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public NodeConfigEntity getConfigEntity() {
		return configEntity;
	}
}
