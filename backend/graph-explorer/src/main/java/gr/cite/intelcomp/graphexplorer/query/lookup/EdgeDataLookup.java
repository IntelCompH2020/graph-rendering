package gr.cite.intelcomp.graphexplorer.query.lookup;

import gr.cite.intelcomp.graphexplorer.query.EdgeDataQuery;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

public class EdgeDataLookup extends GremlinLookup {

    private NodeDataLookup nodeSubQuery;

    public NodeDataLookup getNodeSubQuery() {
        return nodeSubQuery;
    }

    public void setNodeSubQuery(NodeDataLookup nodeSubQuery) {
        this.nodeSubQuery = nodeSubQuery;
    }

    public EdgeDataQuery enrich(QueryFactory queryFactory) {
        EdgeDataQuery query = queryFactory.query(EdgeDataQuery.class);
        if (this.nodeSubQuery != null) query.nodeSubQuery(this.nodeSubQuery.enrich(queryFactory));

        this.enrichCommon(query);

        return query;
    }

}
