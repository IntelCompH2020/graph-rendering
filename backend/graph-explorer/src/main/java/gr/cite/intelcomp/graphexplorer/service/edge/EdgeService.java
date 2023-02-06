package gr.cite.intelcomp.graphexplorer.service.edge;

import gr.cite.intelcomp.graphexplorer.model.Edge;
import gr.cite.intelcomp.graphexplorer.model.persist.EdgePersist;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.FieldSet;

import javax.management.InvalidApplicationException;
import java.util.UUID;

public interface EdgeService {
	Edge persist(EdgePersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException;

	Edge persist(EdgePersist model, FieldSet fields, UUID newItemId) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException;

	void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException;
}
