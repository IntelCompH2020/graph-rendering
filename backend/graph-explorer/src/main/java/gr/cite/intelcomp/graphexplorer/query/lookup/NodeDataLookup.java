package gr.cite.intelcomp.graphexplorer.query.lookup;

import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.query.NodeDataQuery;
import gr.cite.intelcomp.graphexplorer.query.NodeQuery;
import gr.cite.intelcomp.graphexplorer.service.gremlin.query.types.DoubleCompare;
import gr.cite.tools.data.query.Lookup;
import gr.cite.tools.data.query.QueryFactory;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class NodeDataLookup extends GremlinLookup {

    private String like;
    private List<String> ids;
    private List<String> excludedIds;
    
    private List<UUID> edgeIds;
    
    private List<UUID> nodeIds;

    private List<DoubleCompare> x;

    private List<DoubleCompare> y;

    public String getLike() {
        return like;
    }

    public void setLike(String like) {
        this.like = like;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public List<String> getExcludedIds() {
        return excludedIds;
    }

    public void setExcludedIds(List<String> excludeIds) {
        this.excludedIds = excludeIds;
    }

    public List<UUID> getEdgeIds() {
        return edgeIds;
    }

    public void setEdgeIds(List<UUID> edgeIds) {
        this.edgeIds = edgeIds;
    }

    public List<UUID> getNodeIds() {
        return nodeIds;
    }

    public void setNodeIds(List<UUID> nodeIds) {
        this.nodeIds = nodeIds;
    }

    public List<DoubleCompare> getX() {
        return x;
    }

    public void setX(List<DoubleCompare> x) {
        this.x = x;
    }

    public List<DoubleCompare> getY() {
        return y;
    }

    public void setY(List<DoubleCompare> y) {
        this.y = y;
    }

    public NodeDataQuery enrich(QueryFactory queryFactory) {
        NodeDataQuery query = queryFactory.query(NodeDataQuery.class);
        if (this.like != null) query.like(this.like);
        if (this.ids != null) query.ids(this.ids);
        if (this.excludedIds != null) query.excludedIds(this.excludedIds);
        if (this.x != null) query.x(this.x);
        if (this.y != null) query.y(this.y);
        if (this.nodeIds != null) query.nodeIds(this.nodeIds);
        if (this.edgeIds != null) query.edgeIds(this.edgeIds);

        this.enrichCommon(query);

        return query;
    }

}
