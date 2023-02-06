package gr.cite.intelcomp.graphexplorer.service.edge;

import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.EdgeConfigEntity;
import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.NodeConfigEntity;

import java.util.UUID;

public class EdgeConfigItem {
	private final UUID id;
	private final String code;
	private final EdgeConfigEntity configEntity;

	public EdgeConfigItem(UUID id, String code, EdgeConfigEntity configEntity) {
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

	public EdgeConfigEntity getConfigEntity() {
		return configEntity;
	}
}
