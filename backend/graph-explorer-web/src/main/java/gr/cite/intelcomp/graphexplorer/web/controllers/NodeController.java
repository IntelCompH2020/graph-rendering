package gr.cite.intelcomp.graphexplorer.web.controllers;

import gr.cite.intelcomp.graphexplorer.audit.AuditableAction;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.common.types.graphdata.NodeDataEntity;
import gr.cite.intelcomp.graphexplorer.data.NodeEntity;
import gr.cite.intelcomp.graphexplorer.event.EventBroker;
import gr.cite.intelcomp.graphexplorer.model.NodeData;
import gr.cite.intelcomp.graphexplorer.model.Node;
import gr.cite.intelcomp.graphexplorer.model.builder.NodeDataBuilder;
import gr.cite.intelcomp.graphexplorer.model.builder.NodeBuilder;
import gr.cite.intelcomp.graphexplorer.model.censorship.NodeDataCensor;
import gr.cite.intelcomp.graphexplorer.model.censorship.NodeCensor;
import gr.cite.intelcomp.graphexplorer.model.persist.NodeDataPersist;
import gr.cite.intelcomp.graphexplorer.model.persist.NodePersist;
import gr.cite.intelcomp.graphexplorer.query.NodeDataQuery;
import gr.cite.intelcomp.graphexplorer.query.NodeQuery;
import gr.cite.intelcomp.graphexplorer.query.lookup.NodeDataLookup;
import gr.cite.intelcomp.graphexplorer.query.lookup.NodeLookup;
import gr.cite.intelcomp.graphexplorer.service.graph.GraphService;
import gr.cite.intelcomp.graphexplorer.service.node.NodeService;
import gr.cite.intelcomp.graphexplorer.web.model.QueryResult;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import gr.cite.tools.validation.MyValidate;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping(path = "api/node")
public class NodeController {
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(NodeController.class));

	private final BuilderFactory builderFactory;
	private final AuditService auditService;
	private final NodeService nodeService;
	private final CensorFactory censorFactory;
	private final QueryFactory queryFactory;
	private final MessageSource messageSource;
	private final EventBroker eventBroker;
	private final GraphService graphService;
	
	private final ApplicationContext applicationContext;

	@Autowired
	public NodeController(
			BuilderFactory builderFactory,
			AuditService auditService,
			NodeService nodeService,
			CensorFactory censorFactory,
			QueryFactory queryFactory,
			MessageSource messageSource,
			EventBroker eventBroker,
			GraphService graphService, 
			ApplicationContext applicationContext
	) {
		this.builderFactory = builderFactory;
		this.auditService = auditService;
		this.nodeService = nodeService;
		this.censorFactory = censorFactory;
		this.queryFactory = queryFactory;
		this.messageSource = messageSource;
		this.eventBroker = eventBroker;
		this.graphService = graphService;
		this.applicationContext = applicationContext;
	}

	@PostMapping("query")
	public QueryResult<Node> Query(@RequestBody NodeLookup lookup) throws MyApplicationException, MyForbiddenException {
		logger.debug("querying {}", Node.class.getSimpleName());

		this.censorFactory.censor(NodeCensor.class).censor(lookup.getProject(), null);

		NodeQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated);
		List<NodeEntity> data = query.collectAs(lookup.getProject());
		List<Node> models = this.builderFactory.builder(NodeBuilder.class).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated).build(lookup.getProject(), data);
		long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

		this.auditService.track(AuditableAction.Node_Query, "lookup", lookup);
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

		return new QueryResult<>(models, count);
	}

	@GetMapping("{id}")
	public Node Get(@PathVariable("id") UUID id, FieldSet fieldSet, Locale locale) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
		logger.debug(new MapLogEntry("retrieving" + Node.class.getSimpleName()).And("id", id).And("fields", fieldSet));

		this.censorFactory.censor(NodeCensor.class).censor(fieldSet, null);

		NodeQuery query = this.queryFactory.query(NodeQuery.class).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated).ids(id);
		Node model = this.builderFactory.builder(NodeBuilder.class).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated).build(fieldSet, query.firstAs(fieldSet));
		if (model == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{id, Node.class.getSimpleName()}, LocaleContextHolder.getLocale()));

		this.auditService.track(AuditableAction.Node_Lookup, Map.ofEntries(
				new AbstractMap.SimpleEntry<String, Object>("id", id),
				new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
		));
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

		return model;
	}

	@PostMapping(value = { "persist", "persist/{id}" })
	@Transactional
	public Node Persist(@MyValidate @RequestBody NodePersist model, @PathVariable(name ="id", required = false) UUID id, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
		logger.debug(new MapLogEntry("persisting" + Node.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
		Node persisted = this.nodeService.persist(model, fieldSet, id);

		this.auditService.track(AuditableAction.Node_Persist, Map.ofEntries(
				new AbstractMap.SimpleEntry<String, Object>("model", model),
				new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
		));
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);
		return persisted;
	}

	@PostMapping("data/{nodeId}/persist")
	@Transactional
	public void persist(@PathVariable("nodeId") UUID nodeId, @MyValidate @RequestBody NodeDataPersist model) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, IOException {
		logger.debug(new MapLogEntry("persisting" + NodeDataPersist.class.getSimpleName()).And("model", model));

		this.graphService.persistNode(nodeId, model);

		this.auditService.track(AuditableAction.Node_Data_Persist, Map.ofEntries(
				new AbstractMap.SimpleEntry<String, Object>("model", model)
		));
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);
	}

	@PostMapping("data/{nodeId}/bulk-persist")
	@Transactional
	public void bulkPersist(@PathVariable("nodeId") UUID nodeId, @MyValidate @RequestBody List<NodeDataPersist> models) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, IOException {
		//logger.debug(new MapLogEntry("persisting" + NodeDataPersist.class.getSimpleName()).And("model", models));

		this.graphService.persistNodes(nodeId, models);

//		this.auditService.track(AuditableAction.Node_Data_Bulk_Persist, Map.ofEntries(
//				new AbstractMap.SimpleEntry<String, Object>("model", models)
//		));
	}

	@PostMapping("data/query")
	public QueryResult<NodeData> queryData(@RequestBody NodeDataLookup lookup) throws MyApplicationException, MyForbiddenException {
		logger.debug("querying {}", NodeData.class.getSimpleName());

		this.censorFactory.censor(NodeDataCensor.class).censor(lookup.getProject(), null);

		NodeDataQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated);
		List<NodeDataEntity> data = query.collectAs(lookup.getProject());
		List<NodeData> models = this.builderFactory.builder(NodeDataBuilder.class).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated).build(lookup.getProject(), data);
		long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

		this.auditService.track(AuditableAction.Node_Data_Query, "lookup", lookup);
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

		return new QueryResult<>(models, count);
	}

	@GetMapping("data/{nodeId}/{id}")
	public NodeData getData(@PathVariable("id") String id, @PathVariable("nodeId") UUID nodeId, FieldSet fieldSet, Locale locale) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
		logger.debug(new MapLogEntry("retrieving" + NodeData.class.getSimpleName()).And("id", id).And("fields", fieldSet).And("nodeId", nodeId));

		this.censorFactory.censor(NodeDataCensor.class).censor(fieldSet, null);

		NodeDataQuery query = this.queryFactory.query(NodeDataQuery.class).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated).nodeIds(nodeId).ids(id);
		NodeData model = this.builderFactory.builder(NodeDataBuilder.class).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated).build(fieldSet, query.firstAs(fieldSet));
		if (model == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{id, NodeData.class.getSimpleName()}, LocaleContextHolder.getLocale()));

		this.auditService.track(AuditableAction.Node_Data_Lookup, Map.ofEntries(
				new AbstractMap.SimpleEntry<String, Object>("id", id),
				new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
		));
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

		return model;
	}


	@DeleteMapping("{id}")
	@Transactional
	public void Delete(@PathVariable("id") UUID id) throws MyForbiddenException, InvalidApplicationException {
		logger.debug(new MapLogEntry("retrieving" + Node.class.getSimpleName()).And("id", id));

		this.nodeService.deleteAndSave(id);

		this.auditService.track(AuditableAction.Node_Delete, "id", id);
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);
	}
}
