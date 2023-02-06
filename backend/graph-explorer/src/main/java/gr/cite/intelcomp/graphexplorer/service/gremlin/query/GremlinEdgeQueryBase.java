package gr.cite.intelcomp.graphexplorer.service.gremlin.query;


import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.service.gremlin.common.GremlinFactory;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Edge;

public abstract class GremlinEdgeQueryBase<D> extends GremlinQueryBase<D, Edge, Edge> {
	public GremlinEdgeQueryBase(GremlinFactory gremlinFactory, ConventionService conventionService) {
		super(gremlinFactory, conventionService);
	}

	@Override
	protected GraphTraversal<Edge, Edge> getGraphTraversal(GremlinFactory gremlinFactory) {
		return gremlinFactory.getGraphTraversalSource().E();
	}
}
