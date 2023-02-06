package gr.cite.intelcomp.graphexplorer.web.controllers;

import gr.cite.intelcomp.graphexplorer.audit.AuditableAction;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.common.types.graphdata.EdgeDataEntity;
import gr.cite.intelcomp.graphexplorer.data.EdgeEntity;
import gr.cite.intelcomp.graphexplorer.event.EventBroker;
import gr.cite.intelcomp.graphexplorer.model.Edge;
import gr.cite.intelcomp.graphexplorer.model.EdgeData;
import gr.cite.intelcomp.graphexplorer.model.builder.EdgeBuilder;
import gr.cite.intelcomp.graphexplorer.model.builder.EdgeDataBuilder;
import gr.cite.intelcomp.graphexplorer.model.censorship.EdgeCensor;
import gr.cite.intelcomp.graphexplorer.model.censorship.EdgeDataCensor;
import gr.cite.intelcomp.graphexplorer.model.persist.EdgePersist;
import gr.cite.intelcomp.graphexplorer.model.persist.EdgeDataPersist;
import gr.cite.intelcomp.graphexplorer.query.EdgeDataQuery;
import gr.cite.intelcomp.graphexplorer.query.EdgeQuery;
import gr.cite.intelcomp.graphexplorer.query.lookup.EdgeDataLookup;
import gr.cite.intelcomp.graphexplorer.query.lookup.EdgeLookup;
import gr.cite.intelcomp.graphexplorer.service.edge.EdgeService;
import gr.cite.intelcomp.graphexplorer.service.graph.GraphService;
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
@RequestMapping(path = "api/edge")
public class EdgeController {
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(EdgeController.class));

	private final BuilderFactory builderFactory;
	private final AuditService auditService;
	private final EdgeService edgeService;
	private final CensorFactory censorFactory;
	private final QueryFactory queryFactory;
	private final MessageSource messageSource;
	private final EventBroker eventBroker;
	private final GraphService graphService;
	
	private final ApplicationContext applicationContext;

	@Autowired
	public EdgeController(
			BuilderFactory builderFactory,
			AuditService auditService,
			EdgeService edgeService,
			CensorFactory censorFactory,
			QueryFactory queryFactory,
			MessageSource messageSource,
			EventBroker eventBroker,
			GraphService graphService, 
			ApplicationContext applicationContext
	) {
		this.builderFactory = builderFactory;
		this.auditService = auditService;
		this.edgeService = edgeService;
		this.censorFactory = censorFactory;
		this.queryFactory = queryFactory;
		this.messageSource = messageSource;
		this.eventBroker = eventBroker;
		this.graphService = graphService;
		this.applicationContext = applicationContext;
	}

	@PostMapping("query")
	public QueryResult<Edge> Query(@RequestBody EdgeLookup lookup) throws MyApplicationException, MyForbiddenException {
		logger.debug("querying {}", Edge.class.getSimpleName());

		this.censorFactory.censor(EdgeCensor.class).censor(lookup.getProject(), null);

		EdgeQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated);
		List<EdgeEntity> data = query.collectAs(lookup.getProject());
		List<Edge> models = this.builderFactory.builder(EdgeBuilder.class).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated).build(lookup.getProject(), data);
		long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

		this.auditService.track(AuditableAction.Edge_Query, "lookup", lookup);
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

		return new QueryResult<>(models, count);
	}

	@GetMapping("{id}")
	public Edge Get(@PathVariable("id") UUID id, FieldSet fieldSet, Locale locale) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
		logger.debug(new MapLogEntry("retrieving" + Edge.class.getSimpleName()).And("id", id).And("fields", fieldSet));

		this.censorFactory.censor(EdgeCensor.class).censor(fieldSet, null);

		EdgeQuery query = this.queryFactory.query(EdgeQuery.class).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated).ids(id);
		Edge model = this.builderFactory.builder(EdgeBuilder.class).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated).build(fieldSet, query.firstAs(fieldSet));
		if (model == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{id, Edge.class.getSimpleName()}, LocaleContextHolder.getLocale()));

		this.auditService.track(AuditableAction.Edge_Lookup, Map.ofEntries(
				new AbstractMap.SimpleEntry<String, Object>("id", id),
				new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
		));
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

		return model;
	}

	@PostMapping(value = { "persist", "persist/{id}" })
	@Transactional
	public Edge Persist(@MyValidate @RequestBody EdgePersist model, @PathVariable(name ="id", required = false) UUID id,FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
		logger.debug(new MapLogEntry("persisting" + Edge.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
		Edge persisted = this.edgeService.persist(model, fieldSet, id);

		this.auditService.track(AuditableAction.Edge_Persist, Map.ofEntries(
				new AbstractMap.SimpleEntry<String, Object>("model", model),
				new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
		));
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);
		return persisted;
	}

	@PostMapping("data/{nodeId}/{edgeId}/persist")
	@Transactional
	public void persist(@PathVariable("nodeId") UUID nodeId, @PathVariable("edgeId") UUID edgeId,  @MyValidate @RequestBody EdgeDataPersist model) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, IOException {
		logger.debug(new MapLogEntry("persisting" + EdgeDataPersist.class.getSimpleName()).And("model", model).And("edgeId", edgeId).And("nodeId", nodeId));

		this.graphService.persistEdge(nodeId, edgeId, model);

		this.auditService.track(AuditableAction.Edge_Data_Persist, Map.ofEntries(
				new AbstractMap.SimpleEntry<String, Object>("model", model)
		));
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);
	}

	@PostMapping("data/{nodeId}/{edgeId}/bulk-persist")
	@Transactional
	public void bulkPersist(@PathVariable("nodeId") UUID nodeId, @PathVariable("edgeId") UUID edgeId, @MyValidate @RequestBody List<EdgeDataPersist> models) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException, IOException {
//		logger.debug(new MapLogEntry("persisting" + EdgeDataPersist.class.getSimpleName()).And("model", models).And("edgeId", edgeId));

		this.graphService.persistEdges(nodeId, edgeId,  models);

//		this.auditService.track(AuditableAction.Edge_Data_Bulk_Persist, Map.ofEntries(
//				new AbstractMap.SimpleEntry<String, Object>("model", models)
//		));
	}

	@PostMapping("data/query")
	public QueryResult<EdgeData> queryData(@RequestBody EdgeDataLookup lookup) throws MyApplicationException, MyForbiddenException {
		logger.debug("querying {}", EdgeData.class.getSimpleName());

		this.censorFactory.censor(EdgeDataCensor.class).censor(lookup.getProject(), null);

		EdgeDataQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated);
		List<EdgeDataEntity> data = query.collectAs(lookup.getProject());
		List<EdgeData> models = this.builderFactory.builder(EdgeDataBuilder.class).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated).build(lookup.getProject(), data);
		long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

		this.auditService.track(AuditableAction.Edge_Data_Query, "lookup", lookup);
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

		return new QueryResult<>(models, count);
	}

	@DeleteMapping("{id}")
	@Transactional
	public void Delete(@PathVariable("id") UUID id) throws MyForbiddenException, InvalidApplicationException {
		logger.debug(new MapLogEntry("retrieving" + Edge.class.getSimpleName()).And("id", id));

		this.edgeService.deleteAndSave(id);

		this.auditService.track(AuditableAction.Edge_Delete, "id", id);
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);
	}
}
