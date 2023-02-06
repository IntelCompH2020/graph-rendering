package gr.cite.intelcomp.graphexplorer.service.gremlin.common;

import gr.cite.intelcomp.graphexplorer.service.gremlin.configuration.GremlinProperties;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Tokens;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.driver.ser.GryoMessageSerializerV3d0;
import org.apache.tinkerpop.gremlin.driver.ser.Serializers;
import org.apache.tinkerpop.gremlin.orientdb.io.OrientIoRegistry;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.io.gryo.GryoMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import javax.annotation.PostConstruct;

import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;

@Service
@RequestScope
@ConditionalOnProperty(prefix = "gremlin", name = "enabled", havingValue = "true")
public class GremlinFactory implements AutoCloseable {
	private Cluster gremlinCluster;
	private GraphTraversalSource graphTraversalSource;

	private final GremlinProperties gremlinConfig;

	public GremlinFactory(@NonNull GremlinProperties gremlinConfig) {
		if (!gremlinConfig.isEnabled()) {
			this.gremlinConfig = gremlinConfig;
			return;
		}
		final int port = gremlinConfig.getPort();
		if (port <= 0 || port > 65535) {
			gremlinConfig.setPort(Constants.DEFAULT_ENDPOINT_PORT);
		}

		final int maxContentLength = gremlinConfig.getMaxContentLength();
		if (maxContentLength <= 0) {
			gremlinConfig.setMaxContentLength(Constants.DEFAULT_MAX_CONTENT_LENGTH);
		}

		this.gremlinConfig = gremlinConfig;
	}

	private Cluster createGremlinCluster()  {
		if (!gremlinConfig.isEnabled()) throw  new UnsupportedOperationException("GremlinFactory not enabled");
		final Cluster cluster;
		GryoMessageSerializerV3d0 serializer = new GryoMessageSerializerV3d0(
				GryoMapper.build().addRegistry(OrientIoRegistry.instance()));
		try {
			cluster = Cluster.build(this.gremlinConfig.getEndpoint())
					//.serializer(Serializers.valueOf(this.gremlinConfig.getSerializer()).simpleInstance())
					.serializer(serializer)
					.credentials(this.gremlinConfig.getUsername(), this.gremlinConfig.getPassword())
					.enableSsl(this.gremlinConfig.isSslEnabled())
					.maxContentLength(this.gremlinConfig.getMaxContentLength())
					.port(this.gremlinConfig.getPort())
					.maxContentLength(this.gremlinConfig.getMaxContentLength())
					.create();
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Invalid configuration of Gremlin", e);
		}

		return cluster;
	}

	public Client getGremlinClient() {

		if (!gremlinConfig.isEnabled()) throw  new UnsupportedOperationException("GremlinFactory not enabled");
		if (this.gremlinCluster == null) {
			this.gremlinCluster = this.createGremlinCluster();
		}

		return this.gremlinCluster.connect();
	}

	public GraphTraversalSource getGraphTraversalSource() {

		if (!gremlinConfig.isEnabled()) throw  new UnsupportedOperationException("GremlinFactory not enabled");
		if (this.gremlinCluster == null) {
			this.gremlinCluster = this.createGremlinCluster();
		}

		if (this.graphTraversalSource == null) {
			this.graphTraversalSource =  traversal().withRemote(DriverRemoteConnection.using(this.gremlinCluster))
					.with(Tokens.ARGS_EVAL_TIMEOUT, this.gremlinConfig.getEvaluationTimeout());
		}

		return this.graphTraversalSource;
	}
	
	public boolean supportsTransactions(){
		if (!gremlinConfig.isEnabled()) throw  new UnsupportedOperationException("GremlinFactory not enabled");
		return this.getGraphTraversalSource().getGraph().features().graph().supportsTransactions();
	}

	public Transaction openTransaction(GraphTraversalSource g){
		if (!gremlinConfig.isEnabled()) throw  new UnsupportedOperationException("GremlinFactory not enabled");
		Transaction tx = null;
		if (this.supportsTransactions()) {
			tx = g.tx();
			tx.open();
		}
		return tx;
	}

	public void commitTransaction(Transaction tx){
		if (!gremlinConfig.isEnabled()) throw  new UnsupportedOperationException("GremlinFactory not enabled");
		if (this.supportsTransactions() && tx != null) tx.commit();
	}

	public void rollbackTransaction(Transaction tx){
		if (!gremlinConfig.isEnabled()) throw  new UnsupportedOperationException("GremlinFactory not enabled");
		if (this.supportsTransactions() && tx != null) tx.rollback();
	}

	@Override
	public void close() throws Exception {
		if (!gremlinConfig.isEnabled()) return;
		if (this.gremlinCluster != null && !this.gremlinCluster.isClosed()) this.gremlinCluster.close();
		if (this.graphTraversalSource != null) this.graphTraversalSource.close();
		this.gremlinCluster = null;
		this.graphTraversalSource = null;
	}
}
