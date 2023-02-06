package gr.cite.intelcomp.graphexplorer.authorization;

import java.util.List;
import java.util.UUID;

public interface AuthorizationContentResolver {
	NodeRolesResource nodeAffiliation(UUID nodeId);

	List<UUID> affiliatedNodes(String... permissions);
	EdgeRolesResource edgeAffiliation(UUID edgeId);

	List<UUID> affiliatedEdges(String... permissions);
	GraphRolesResource graphAffiliation(UUID graphId);

	List<UUID> affiliatedGraphs(String... permissions);
}
