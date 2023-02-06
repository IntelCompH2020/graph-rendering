package gr.cite.intelcomp.graphexplorer.model.persist;

import gr.cite.intelcomp.graphexplorer.common.validation.FieldNotNullIfOtherSet;
import gr.cite.intelcomp.graphexplorer.common.validation.ValidId;
import org.hibernate.id.GUIDGenerator;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@FieldNotNullIfOtherSet(message = "{validation.hashempty}")
public class GraphPersist {

    @ValidId(message = "{validation.invalidid}")
    private UUID id;

    @NotNull(message = "{validation.empty}")
    @NotEmpty(message = "{validation.empty}")
    @Size(max = 500, message = "{validation.largerthanmax}")
    private String name;

    private String description;

    private List<UUID> edgeIds;
    private List<UUID> nodeIds;

    private String hash;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public List<UUID> getEdgeIds() {
        return edgeIds;
    }

    public void setEdgeIds(List<UUID> edgeIds) {
        this.edgeIds = edgeIds;
    }

    public List<UUID> getNodeIds() {
        return nodeIds;
    }

    public void setNodeIds(List<UUID> nodeIds) {
        this.nodeIds = nodeIds;
    }
}
