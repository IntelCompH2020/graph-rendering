package gr.cite.intelcomp.graphexplorer.service.elasticedge;

import gr.cite.tools.exception.MyForbiddenException;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.util.UUID;

public interface ElasticEdgeService {

	String calculateIndexName(String code, boolean ensureUnique) throws IOException;

	String ensureIndex(UUID edgeId) throws IOException;

	String getIndexName(UUID edgeId) throws IOException;

	void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException, IOException;
}
