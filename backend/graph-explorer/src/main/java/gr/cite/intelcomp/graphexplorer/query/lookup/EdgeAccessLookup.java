package gr.cite.intelcomp.graphexplorer.query.lookup;

import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.query.EdgeAccessQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.List;
import java.util.UUID;

public class EdgeAccessLookup extends Lookup {

	private List<UUID> ids;
	private List<UUID> edgeIds;
	private List<UUID> userIds;
	private List<IsActive> isActive;


	public EdgeAccessQuery enrich(QueryFactory queryFactory) {
		EdgeAccessQuery query = queryFactory.query(EdgeAccessQuery.class);
		if (this.ids != null) query.ids(this.ids);
		if (this.edgeIds != null) query.edgeIds(this.edgeIds);
		if (this.userIds != null) query.userIds(this.userIds);
		if (this.isActive != null) query.isActive(this.isActive);

		this.enrichCommon(query);

		return query;
	}

}
