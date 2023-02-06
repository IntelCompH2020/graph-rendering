package gr.cite.intelcomp.graphexplorer.web.controllers;

import gr.cite.intelcomp.graphexplorer.audit.AuditableAction;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.common.types.graphdata.GraphDataEntity;
import gr.cite.intelcomp.graphexplorer.common.types.graphdata.NodeDataEntity;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.data.EdgeEntity;
import gr.cite.intelcomp.graphexplorer.data.GraphEntity;
import gr.cite.intelcomp.graphexplorer.model.*;
import gr.cite.intelcomp.graphexplorer.model.builder.EdgeBuilder;
import gr.cite.intelcomp.graphexplorer.model.builder.GraphBuilder;
import gr.cite.intelcomp.graphexplorer.model.builder.GraphDataBuilder;
import gr.cite.intelcomp.graphexplorer.model.censorship.EdgeCensor;
import gr.cite.intelcomp.graphexplorer.model.censorship.GraphCensor;
import gr.cite.intelcomp.graphexplorer.model.censorship.NodeDataCensor;
import gr.cite.intelcomp.graphexplorer.model.persist.EdgePersist;
import gr.cite.intelcomp.graphexplorer.model.persist.GraphPersist;
import gr.cite.intelcomp.graphexplorer.query.EdgeQuery;
import gr.cite.intelcomp.graphexplorer.query.GraphQuery;
import gr.cite.intelcomp.graphexplorer.query.NodeDataQuery;
import gr.cite.intelcomp.graphexplorer.query.lookup.EdgeLookup;
import gr.cite.intelcomp.graphexplorer.query.lookup.GraphDataLookup;
import gr.cite.intelcomp.graphexplorer.query.lookup.GraphLookup;
import gr.cite.intelcomp.graphexplorer.service.graph.GraphService;
import gr.cite.intelcomp.graphexplorer.web.model.QueryResult;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.censor.CensorFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.fieldset.BaseFieldSet;
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
import java.util.*;

@RestController
@RequestMapping(path = "api/graph")
public class GraphController {
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(GraphController.class));

	private final BuilderFactory builderFactory;
	private final AuditService auditService;
	private final GraphService graphService;
	private final CensorFactory censorFactory;
	private final QueryFactory queryFactory;
	private final MessageSource messageSource;

	private final ApplicationContext applicationContext;
	private final ConventionService conventionService;

	@Autowired
	public GraphController(
			BuilderFactory builderFactory,
			AuditService auditService,
			GraphService graphService, 
			CensorFactory censorFactory,
			QueryFactory queryFactory,
			MessageSource messageSource,
			ApplicationContext applicationContext,
			ConventionService conventionService) {
		this.builderFactory = builderFactory;
		this.auditService = auditService;
		this.graphService = graphService;
		this.censorFactory = censorFactory;
		this.queryFactory = queryFactory;
		this.messageSource = messageSource;
		this.applicationContext = applicationContext;
		this.conventionService = conventionService;
	}

	@PostMapping("query")
	public QueryResult<Graph> Query(@RequestBody GraphLookup lookup) throws MyApplicationException, MyForbiddenException {
		logger.debug("querying {}", Graph.class.getSimpleName());

		this.censorFactory.censor(GraphCensor.class).censor(lookup.getProject(), null);

		GraphQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated);
		List<GraphEntity> data = query.collectAs(lookup.getProject());
		List<Graph> models = this.builderFactory.builder(GraphBuilder.class).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated).build(lookup.getProject(), data);
		long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

		this.auditService.track(AuditableAction.Graph_Query, "lookup", lookup);
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

		return new QueryResult<>(models, count);
	}

	@GetMapping("{id}")
	public Graph Get(@PathVariable("id") UUID id, FieldSet fieldSet, Locale locale) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
		logger.debug(new MapLogEntry("retrieving" + Graph.class.getSimpleName()).And("id", id).And("fields", fieldSet));

		this.censorFactory.censor(GraphCensor.class).censor(fieldSet, null);

		GraphQuery query = this.queryFactory.query(GraphQuery.class).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated).ids(id);
		Graph model = this.builderFactory.builder(GraphBuilder.class).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated).build(fieldSet, query.firstAs(fieldSet));
		if (model == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{id, Graph.class.getSimpleName()}, LocaleContextHolder.getLocale()));

		this.auditService.track(AuditableAction.Graph_Lookup, Map.ofEntries(
				new AbstractMap.SimpleEntry<String, Object>("id", id),
				new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
		));
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

		return model;
	}

	@PostMapping("persist")
	@Transactional
	public Graph Persist(@MyValidate @RequestBody GraphPersist model, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
		logger.debug(new MapLogEntry("persisting" + Graph.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));
		Graph persisted = this.graphService.persist(model, fieldSet);

		this.auditService.track(AuditableAction.Graph_Persist, Map.ofEntries(
				new AbstractMap.SimpleEntry<String, Object>("model", model),
				new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
		));
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);
		return persisted;
	}

	@DeleteMapping("{id}")
	@Transactional
	public void Delete(@PathVariable("id") UUID id) throws MyForbiddenException, InvalidApplicationException {
		logger.debug(new MapLogEntry("delete" + Graph.class.getSimpleName()).And("id", id));

		this.graphService.deleteAndSave(id);

		this.auditService.track(AuditableAction.Graph_Delete, "id", id);
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);
	}

	@PostMapping("data/query")
	public GraphData queryData(@RequestBody GraphDataLookup lookup) throws MyApplicationException, MyForbiddenException {
		logger.debug("querying {}", GraphData.class.getSimpleName());

		this.censorFactory.censor(NodeDataCensor.class).censor(lookup.getProject(), null);

		GraphData models = this.graphService.getGraphData(lookup);

		this.auditService.track(AuditableAction.Graph_QueryData, "lookup", lookup);
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

		return models;
	}

	@PostMapping("data/get-info")
	@Transactional
	public GraphInfo getGraphInfo(@RequestBody GraphInfoLookup lookup, Locale locale) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
		logger.debug(new MapLogEntry("retrieving graph info").And("lookup", lookup));

		GraphInfo models = this.graphService.getGraphInfo(lookup);

		this.auditService.track(AuditableAction.Graph_GraphInfo, Map.ofEntries(
				new AbstractMap.SimpleEntry<String, Object>("lookup", lookup)
		));
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

		return models;
	}

	@PostMapping("recalculate-node-size/{id}")
	@Transactional
	public void recalculateNodeSize(@PathVariable("id") UUID id) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
		logger.debug(new MapLogEntry("retrieving graph info").And("id", id));

		this.graphService.recalculateNodeSize(id);

		this.auditService.track(AuditableAction.Graph_RecalculateNodeSize, Map.ofEntries(
				new AbstractMap.SimpleEntry<String, Object>("id", id)
		));
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

	}
}
