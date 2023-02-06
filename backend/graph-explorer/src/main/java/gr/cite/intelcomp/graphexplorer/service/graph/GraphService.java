package gr.cite.intelcomp.graphexplorer.service.graph;

import gr.cite.intelcomp.graphexplorer.model.*;
import gr.cite.intelcomp.graphexplorer.model.persist.EdgeDataPersist;
import gr.cite.intelcomp.graphexplorer.model.persist.EdgePersist;
import gr.cite.intelcomp.graphexplorer.model.persist.GraphPersist;
import gr.cite.intelcomp.graphexplorer.model.persist.NodeDataPersist;
import gr.cite.intelcomp.graphexplorer.query.lookup.GraphDataLookup;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface GraphService {
	Graph persist(GraphPersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException;

	void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;
	GraphData getGraphData(GraphDataLookup lookup);
	GraphInfo getGraphInfo(GraphInfoLookup lookup);

	void recalculateNodeSize(UUID nodeId);

	void persistNode(UUID nodeId, NodeDataPersist model) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, IOException;

	void persistNodes(UUID nodeId, List<NodeDataPersist> models) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, IOException;

	void persistEdge(UUID nodeId, UUID edgeId, EdgeDataPersist model) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, IOException;

	void persistEdges(UUID nodeId, UUID edgeId, List<EdgeDataPersist> models) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException, IOException;
}
