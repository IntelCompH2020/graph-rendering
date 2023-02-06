package gr.cite.intelcomp.graphexplorer.service.edge;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface EdgeConfigService {
	EdgeConfigItem getConfig(UUID edgeId);

	String ensurePropertyName(@NotNull String prop);
}
