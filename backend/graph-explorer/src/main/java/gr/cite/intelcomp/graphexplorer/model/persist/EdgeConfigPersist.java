package gr.cite.intelcomp.graphexplorer.model.persist;

import gr.cite.intelcomp.graphexplorer.common.validation.FieldNotNullIfOtherSet;

import javax.validation.Valid;
import java.util.List;

@FieldNotNullIfOtherSet(message = "{validation.hashempty}")
public class EdgeConfigPersist {
    @Valid
    private List<FieldDefinitionPersist> fields;
    public List<FieldDefinitionPersist> getFields() {
        return fields;
    }

    public void setFields(List<FieldDefinitionPersist> fields) {
        this.fields = fields;
    }
}
