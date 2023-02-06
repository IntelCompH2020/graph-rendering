package gr.cite.intelcomp.graphexplorer.model.censorship;

import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.tools.data.censor.Censor;
import gr.cite.tools.fieldset.FieldSet;

public class BaseCensor implements Censor {

	protected final ConventionService conventionService;

	public BaseCensor(ConventionService conventionService){
		this.conventionService = conventionService;
	}

	protected Boolean isEmpty(FieldSet fields) {
		return fields == null || fields.isEmpty();
	}

	protected String asIndexerPrefix(String part){
		return this.conventionService.asPrefix(part);
	}
}
