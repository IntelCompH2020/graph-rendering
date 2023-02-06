package gr.cite.intelcomp.graphexplorer.query.lookup;

import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.query.GraphAccessQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.List;
import java.util.UUID;

public class GraphAccessLookup extends Lookup {

	private List<UUID> ids;
	private List<UUID> graphIds;
	private List<UUID> userIds;
	private List<IsActive> isActive;


	public GraphAccessQuery enrich(QueryFactory queryFactory) {
		GraphAccessQuery query = queryFactory.query(GraphAccessQuery.class);
		if (this.ids != null) query.ids(this.ids);
		if (this.graphIds != null) query.graphIds(this.graphIds);
		if (this.userIds != null) query.userIds(this.userIds);
		if (this.isActive != null) query.isActive(this.isActive);

		this.enrichCommon(query);

		return query;
	}

}
