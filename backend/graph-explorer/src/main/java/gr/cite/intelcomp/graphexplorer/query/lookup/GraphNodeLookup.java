package gr.cite.intelcomp.graphexplorer.query.lookup;

import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.query.GraphNodeQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.List;
import java.util.UUID;

public class GraphNodeLookup extends Lookup {

	private List<UUID> ids;
	private List<UUID> graphIds;
	private List<UUID> nodeIds;
	private List<IsActive> isActive;


	public GraphNodeQuery enrich(QueryFactory queryFactory) {
		GraphNodeQuery query = queryFactory.query(GraphNodeQuery.class);
		if (this.ids != null) query.ids(this.ids);
		if (this.graphIds != null) query.graphIds(this.graphIds);
		if (this.nodeIds != null) query.nodeIds(this.nodeIds);
		if (this.isActive != null) query.isActive(this.isActive);

		this.enrichCommon(query);

		return query;
	}

}
