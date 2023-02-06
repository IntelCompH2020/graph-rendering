package gr.cite.intelcomp.graphexplorer.query.lookup;

import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.query.NodeAccessQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.List;
import java.util.UUID;

public class NodeAccessLookup extends Lookup {

	private List<UUID> ids;
	private List<UUID> nodeIds;
	private List<UUID> userIds;
	private List<IsActive> isActive;


	public NodeAccessQuery enrich(QueryFactory queryFactory) {
		NodeAccessQuery query = queryFactory.query(NodeAccessQuery.class);
		if (this.ids != null) query.ids(this.ids);
		if (this.nodeIds != null) query.nodeIds(this.nodeIds);
		if (this.userIds != null) query.userIds(this.userIds);
		if (this.isActive != null) query.isActive(this.isActive);

		this.enrichCommon(query);

		return query;
	}

}
