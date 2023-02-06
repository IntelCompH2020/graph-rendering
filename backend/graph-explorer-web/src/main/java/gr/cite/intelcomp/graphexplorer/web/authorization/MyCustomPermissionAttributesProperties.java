package gr.cite.intelcomp.graphexplorer.web.authorization;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.HashMap;
import java.util.List;

@ConstructorBinding
@ConfigurationProperties(prefix = "permissions")
@ConditionalOnProperty(prefix = "permissions", name = "enabled", havingValue = "true")
public class MyCustomPermissionAttributesProperties {

	private final List<String> extendedClaims;
	private final HashMap<String, MyPermission> policies;

	@ConstructorBinding
	public MyCustomPermissionAttributesProperties(List<String> extendedClaims, HashMap<String, MyPermission> policies) {
		this.extendedClaims = extendedClaims;
		this.policies = policies;
	}

	public List<String> getExtendedClaims() {
		return extendedClaims;
	}

	public HashMap<String, MyPermission> getPolicies() {
		return policies;
	}

	public static class MyPermission {

		private final NodeRole node;
		private final EdgeRole edge;
		private final GraphRole graph;

		@ConstructorBinding
		public MyPermission(EdgeRole edge, NodeRole node, GraphRole graph) {
			this.node = node;
			this.edge = edge;
			this.graph = graph;
		}

		public NodeRole getNode() {
			return node;
		}

		public EdgeRole getEdge() {
			return edge;
		}

		public GraphRole getGraph() {
			return graph;
		}
	}

}
