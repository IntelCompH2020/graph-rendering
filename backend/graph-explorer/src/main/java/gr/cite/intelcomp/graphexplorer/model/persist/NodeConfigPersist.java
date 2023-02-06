package gr.cite.intelcomp.graphexplorer.model.persist;

import gr.cite.intelcomp.graphexplorer.common.validation.FieldNotNullIfOtherSet;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@FieldNotNullIfOtherSet(message = "{validation.hashempty}")
public class NodeConfigPersist {
    @Valid
    private List<FieldDefinitionPersist> fields;
    
    private List<String> clusterFields;

    @NotNull(message = "{validation.empty}")
    @NotEmpty(message = "{validation.empty}")
    private String defaultOrderField;

    public List<FieldDefinitionPersist> getFields() {
        return fields;
    }

    public void setFields(List<FieldDefinitionPersist> fields) {
        this.fields = fields;
    }

    public List<String> getClusterFields() {
        return clusterFields;
    }

    public void setClusterFields(List<String> clusterFields) {
        this.clusterFields = clusterFields;
    }

    public String getDefaultOrderField() {
        return defaultOrderField;
    }

    public void setDefaultOrderField(String defaultOrderField) {
        this.defaultOrderField = defaultOrderField;
    }
}
