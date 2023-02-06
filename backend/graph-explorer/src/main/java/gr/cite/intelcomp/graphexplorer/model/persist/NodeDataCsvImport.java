package gr.cite.intelcomp.graphexplorer.model.persist;

import gr.cite.intelcomp.graphexplorer.common.validation.ValidId;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.UUID;


public class NodeDataCsvImport {
	@ValidId(message = "{validation.invalidid}")
	@NotNull(message = "{validation.empty}")
	private UUID nodeId;

	@NotNull(message = "{validation.empty}")
	@NotEmpty(message = "{validation.empty}")
	private String filePath;

	@NotNull(message = "{validation.empty}")
	private Boolean hasHeaderColumn;
	private Character separator;
	private Character quoteChar;
	private Character escape;

	@NotNull(message = "{validation.empty}")
	private Integer idIndex;
	@NotNull(message = "{validation.empty}")
	private Integer xIndex;
	@NotNull(message = "{validation.empty}")
	private Integer yIndex;
	@NotNull(message = "{validation.empty}")
	private Integer nameIndex;

	public UUID getNodeId() {
		return nodeId;
	}

	public void setNodeId(UUID nodeId) {
		this.nodeId = nodeId;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Boolean getHasHeaderColumn() {
		return hasHeaderColumn;
	}

	public void setHasHeaderColumn(Boolean hasHeaderColumn) {
		this.hasHeaderColumn = hasHeaderColumn;
	}

	public Character getSeparator() {
		return separator;
	}

	public void setSeparator(Character separator) {
		this.separator = separator;
	}

	public Character getQuoteChar() {
		return quoteChar;
	}

	public void setQuoteChar(Character quoteChar) {
		this.quoteChar = quoteChar;
	}

	public Character getEscape() {
		return escape;
	}

	public void setEscape(Character escape) {
		this.escape = escape;
	}

	public Integer getIdIndex() {
		return idIndex;
	}

	public void setIdIndex(Integer idIndex) {
		this.idIndex = idIndex;
	}

	public Integer getxIndex() {
		return xIndex;
	}

	public void setxIndex(Integer xIndex) {
		this.xIndex = xIndex;
	}

	public Integer getyIndex() {
		return yIndex;
	}

	public void setyIndex(Integer yIndex) {
		this.yIndex = yIndex;
	}

	public Integer getNameIndex() {
		return nameIndex;
	}

	public void setNameIndex(Integer nameIndex) {
		this.nameIndex = nameIndex;
	}
}




