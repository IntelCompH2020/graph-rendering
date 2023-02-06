package gr.cite.intelcomp.graphexplorer.model.persist;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

public class EdgeDataPersist {
    public EdgeDataPersist() {
        properties = new HashMap<>();
    }
    @NotNull(message = "{validation.empty}")
    @NotEmpty(message = "{validation.empty}")
    private String sourceId;

    @NotNull(message = "{validation.empty}")
    @NotEmpty(message = "{validation.empty}")
    private String targetId;

    @NotNull(message = "{validation.empty}")
    private Double weight;

    private Map<String, Object> properties;

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    @JsonAnySetter
    public void add(String property, Object value) {
        properties.put(property, value);
    }
}
