package gr.cite.intelcomp.graphexplorer.service.edge;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationContentResolver;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.authorization.EdgeRolesResource;
import gr.cite.intelcomp.graphexplorer.authorization.Permission;
import gr.cite.intelcomp.graphexplorer.common.JsonHandlingService;
import gr.cite.intelcomp.graphexplorer.common.enums.IsActive;
import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.FieldDefinitionEntity;
import gr.cite.intelcomp.graphexplorer.common.types.graphconfig.EdgeConfigEntity;
import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import gr.cite.intelcomp.graphexplorer.data.EdgeEntity;
import gr.cite.intelcomp.graphexplorer.errorcode.ErrorThesaurusProperties;
import gr.cite.intelcomp.graphexplorer.event.EdgeTouchedEvent;
import gr.cite.intelcomp.graphexplorer.event.EventBroker;
import gr.cite.intelcomp.graphexplorer.model.Edge;
import gr.cite.intelcomp.graphexplorer.model.builder.EdgeBuilder;
import gr.cite.intelcomp.graphexplorer.model.deleter.EdgeDeleter;
import gr.cite.intelcomp.graphexplorer.model.persist.EdgeConfigPersist;
import gr.cite.intelcomp.graphexplorer.model.persist.EdgePersist;
import gr.cite.intelcomp.graphexplorer.query.EdgeQuery;
import gr.cite.tools.data.builder.BuilderFactory;
import gr.cite.tools.data.deleter.DeleterFactory;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.exception.MyApplicationException;
import gr.cite.tools.exception.MyForbiddenException;
import gr.cite.tools.exception.MyNotFoundException;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import javax.management.InvalidApplicationException;
import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequestScope
public class EdgeServiceImpl implements EdgeService {
	private final EntityManager entityManager;
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(EdgeServiceImpl.class));
	private final AuthorizationService authorizationService;
	private final DeleterFactory deleterFactory;
	private final BuilderFactory builderFactory;
	private final ConventionService conventionService;
	private final ErrorThesaurusProperties errors;
	private final MessageSource messageSource;
	private final EventBroker eventBroker;
	private final QueryFactory queryFactory;
	private final JsonHandlingService jsonHandlingService;
	private final AuthorizationContentResolver authorizationContentResolver;
	public EdgeServiceImpl(EntityManager entityManager,
	                       AuthorizationService authorizationService,
	                       DeleterFactory deleterFactory,
	                       ConventionService conventionService,
	                       MessageSource messageSource,
	                       EventBroker eventBroker,
	                       JsonHandlingService jsonHandlingService,
	                       BuilderFactory builderFactory,
	                       ErrorThesaurusProperties errors,
	                       QueryFactory queryFactory, 
	                       AuthorizationContentResolver authorizationContentResolver) {
		this.entityManager = entityManager;
		this.authorizationService = authorizationService;
		this.deleterFactory = deleterFactory;
		this.conventionService = conventionService;
		this.messageSource = messageSource;
		this.eventBroker = eventBroker;
		this.jsonHandlingService = jsonHandlingService;
		this.builderFactory = builderFactory;
		this.errors = errors;
		this.queryFactory = queryFactory;
		this.authorizationContentResolver = authorizationContentResolver;
	}

	@Override
	public Edge persist(EdgePersist model, FieldSet fields) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
		return this.persist(model, fields, null);
	}

	@Override
	public Edge persist(EdgePersist model, FieldSet fields, UUID newItemId) throws MyForbiddenException, MyValidationException, MyApplicationException, MyNotFoundException, InvalidApplicationException {
		logger.debug(new MapLogEntry("persisting data Edge").And("model", model).And("fields", fields));

		Boolean isUpdate = this.conventionService.isValidGuid(model.getId());

		EdgeEntity data;
		if (isUpdate) {
			data = this.entityManager.find(EdgeEntity.class, model.getId());
			if (data == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{model.getId(), Edge.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		} else {
			data = new EdgeEntity();
			data.setId(newItemId == null ? UUID.randomUUID() : newItemId);
			data.setIsActive(IsActive.ACTIVE);
			data.setCreatedAt(Instant.now());
		}

		this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.edgeAffiliation(data.getId())), Permission.EditEdge);

		data.setCode(model.getCode());
		data.setName(model.getName());
		data.setDescription(model.getDescription());
		data.setUpdatedAt(Instant.now());
		data.setConfig(jsonHandlingService.toJsonSafe(this.mapToEdgeConfigEntity(model.getConfig())));
		if (isUpdate) this.entityManager.merge(data);
		else this.entityManager.persist(data);

		this.entityManager.flush();

		if (this.queryFactory.query(EdgeQuery.class).codes(data.getCode()).count() > 1) throw new MyApplicationException(this.errors.getEdgeAlreadyExists().getCode(), this.errors.getEdgeAlreadyExists().getMessage());

		this.eventBroker.emit(new EdgeTouchedEvent(data.getId()));
		return this.builderFactory.builder(EdgeBuilder.class).authorize(AuthorizationFlags.OwnerOrPermissionOrAffiliated).build(BaseFieldSet.build(fields, Edge._id), data);
	}

	private EdgeConfigEntity mapToEdgeConfigEntity(EdgeConfigPersist config) {
		if (config == null) return null;
		EdgeConfigEntity persistConfig = new EdgeConfigEntity();
		if (config.getFields() != null) {
			List<FieldDefinitionEntity> filterColumns = new ArrayList<>();
			config.getFields().forEach(x -> {
				FieldDefinitionEntity newConfig = new FieldDefinitionEntity();
				newConfig.setCode(x.getCode());
				newConfig.setType(x.getType());
				filterColumns.add(newConfig);
			});
			persistConfig.setFields(filterColumns);
		}

		return persistConfig;
	}


	public void deleteAndSave(UUID id) throws MyForbiddenException, InvalidApplicationException {
		logger.debug("deleting dataset: {}", id);
		EdgeEntity data = this.entityManager.find(EdgeEntity.class, id);
		if (data == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{id, Edge.class.getSimpleName()}, LocaleContextHolder.getLocale()));
		
		this.authorizationService.authorizeAtLeastOneForce(List.of(this.authorizationContentResolver.edgeAffiliation(data.getId())), Permission.DeleteEdge);

		this.deleterFactory.deleter(EdgeDeleter.class).deleteAndSaveByIds(List.of(id));
	}
}
