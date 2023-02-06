package gr.cite.intelcomp.graphexplorer.service.gremlin.query;


import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.service.gremlin.common.GremlinFactory;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public abstract class GremlinVertexQueryBase<D> extends GremlinQueryBase<D, Vertex, Vertex> {
	public GremlinVertexQueryBase(GremlinFactory gremlinFactory, ConventionService conventionService) {
		super(gremlinFactory, conventionService);
	}

	@Override
	protected GraphTraversal<Vertex, Vertex> getGraphTraversal(GremlinFactory gremlinFactory) {
		return gremlinFactory.getGraphTraversalSource().V();
	}
}
