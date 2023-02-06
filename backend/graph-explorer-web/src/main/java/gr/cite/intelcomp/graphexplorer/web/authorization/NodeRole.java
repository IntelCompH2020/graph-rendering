package gr.cite.intelcomp.graphexplorer.web.authorization;

import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.Set;

public class NodeRole {
	private final Set<String> roles;

	@ConstructorBinding
	public NodeRole(Set<String> roles) {
		this.roles = roles;
	}

	public Set<String> getRoles() {
		return roles;
	}

}
