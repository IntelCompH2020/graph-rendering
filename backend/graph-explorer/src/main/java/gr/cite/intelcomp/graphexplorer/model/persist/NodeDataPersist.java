package gr.cite.intelcomp.graphexplorer.model.persist;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

public class NodeDataPersist {
    public NodeDataPersist() {
        properties = new HashMap<>();
    }

    @NotNull(message = "{validation.empty}")
    @NotEmpty(message = "{validation.empty}")
    private String id;
    
    private String name;

    @NotNull(message = "{validation.empty}")
    private Double x;

    @NotNull(message = "{validation.empty}")
    private Double y;

    private Map<String, Object> properties;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    @JsonAnySetter
    public void add(String property, Object value) {
        properties.put(property, value);
    }
}
