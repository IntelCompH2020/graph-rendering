package gr.cite.intelcomp.graphexplorer.service.elasticnode;

import gr.cite.tools.exception.MyForbiddenException;

import javax.management.InvalidApplicationException;
import java.io.IOException;
import java.util.UUID;

public interface ElasticNodeService {

	String calculateIndexName(String code, boolean ensureUnique) throws IOException;

	String ensureIndex(UUID nodeId) throws IOException;

	String getIndexName(UUID nodeId) throws IOException;

	void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException, IOException;
}
