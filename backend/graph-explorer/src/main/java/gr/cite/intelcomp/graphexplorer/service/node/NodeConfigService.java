package gr.cite.intelcomp.graphexplorer.service.node;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface NodeConfigService {
	NodeConfigItem getConfig(UUID nodeId);

	String ensurePropertyName(@NotNull String prop);
}
