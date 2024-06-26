package gr.cite.intelcomp.graphexplorer.errorcode;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "error-thesaurus")
public class ErrorThesaurusProperties {

	private ErrorDescription systemError;

	public ErrorDescription getSystemError() {
		return systemError;
	}

	public void setSystemError(ErrorDescription systemError) {
		this.systemError = systemError;
	}

	private ErrorDescription forbidden;

	public ErrorDescription getForbidden() {
		return forbidden;
	}

	public void setForbidden(ErrorDescription forbidden) {
		this.forbidden = forbidden;
	}

	private ErrorDescription hashConflict;

	public ErrorDescription getHashConflict() {
		return hashConflict;
	}

	public void setHashConflict(ErrorDescription hashConflict) {
		this.hashConflict = hashConflict;
	}


	private ErrorDescription modelValidation;

	public ErrorDescription getModelValidation() {
		return modelValidation;
	}

	public void setModelValidation(ErrorDescription modelValidation) {
		this.modelValidation = modelValidation;
	}
	

	private ErrorDescription nodeAlreadyExists;

	public ErrorDescription getNodeAlreadyExists() {
		return nodeAlreadyExists;
	}

	public void setNodeAlreadyExists(ErrorDescription nodeAlreadyExists) {
		this.nodeAlreadyExists = nodeAlreadyExists;
	}


	private ErrorDescription edgeAlreadyExists;

	public ErrorDescription getEdgeAlreadyExists() {
		return edgeAlreadyExists;
	}

	public void setEdgeAlreadyExists(ErrorDescription edgeAlreadyExists) {
		this.edgeAlreadyExists = edgeAlreadyExists;
	}
}
