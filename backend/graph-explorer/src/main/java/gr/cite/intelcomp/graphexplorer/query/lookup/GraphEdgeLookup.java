package gr.cite.intelcomp.graphexplorer.query.lookup;

import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.query.GraphEdgeQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.List;
import java.util.UUID;

public class GraphEdgeLookup extends Lookup {

	private List<UUID> ids;
	private List<UUID> graphIds;
	private List<UUID> edgeIds;
	private List<IsActive> isActive;


	public GraphEdgeQuery enrich(QueryFactory queryFactory) {
		GraphEdgeQuery query = queryFactory.query(GraphEdgeQuery.class);
		if (this.ids != null) query.ids(this.ids);
		if (this.graphIds != null) query.graphIds(this.graphIds);
		if (this.edgeIds != null) query.edgeIds(this.edgeIds);
		if (this.isActive != null) query.isActive(this.isActive);

		this.enrichCommon(query);

		return query;
	}

}
