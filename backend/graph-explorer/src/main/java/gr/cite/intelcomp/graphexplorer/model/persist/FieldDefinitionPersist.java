package gr.cite.intelcomp.graphexplorer.model.persist;

import gr.cite.intelcomp.graphexplorer.common.enums.GraphFieldType;
import gr.cite.intelcomp.graphexplorer.common.validation.FieldNotNullIfOtherSet;
import gr.cite.intelcomp.graphexplorer.common.validation.ValidEnum;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@FieldNotNullIfOtherSet(message = "{validation.hashempty}")
public class FieldDefinitionPersist {
    @NotNull(message = "{validation.empty}")
    @NotEmpty(message = "{validation.empty}")
    @Size(max = 200, message = "{validation.largerthanmax}")
    private String code;

    @ValidEnum(message = "enum is null")
    private GraphFieldType type;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public GraphFieldType getType() {
        return type;
    }

    public void setType(GraphFieldType type) {
        this.type = type;
    }
}
