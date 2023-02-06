package gr.cite.intelcomp.graphexplorer.web.controllers;

import gr.cite.intelcomp.graphexplorer.audit.AuditableAction;
import gr.cite.intelcomp.graphexplorer.authorization.AuthorizationFlags;
import gr.cite.intelcomp.graphexplorer.data.UserEntity;
import gr.cite.intelcomp.graphexplorer.model.User;
import gr.cite.intelcomp.graphexplorer.model.builder.UserBuilder;
import gr.cite.intelcomp.graphexplorer.model.censorship.UserCensor;
import gr.cite.intelcomp.graphexplorer.model.persist.UserAccessPersist;
import gr.cite.intelcomp.graphexplorer.model.persist.UserPersist;
import gr.cite.intelcomp.graphexplorer.query.UserQuery;
import gr.cite.intelcomp.graphexplorer.query.lookup.UserLookup;
import gr.cite.intelcomp.graphexplorer.service.user.UserService;
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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.management.InvalidApplicationException;
import javax.transaction.Transactional;
import java.util.*;

@RestController
@RequestMapping(path = "api/user")
public class UserController {
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(UserController.class));

	private final BuilderFactory builderFactory;
	private final AuditService auditService;
	private final UserService userService;
	private final CensorFactory censorFactory;
	private final QueryFactory queryFactory;
	private final MessageSource messageSource;


	@Autowired
	public UserController(
			BuilderFactory builderFactory,
			AuditService auditService,
			UserService userService,
			CensorFactory censorFactory,
			QueryFactory queryFactory,
			MessageSource messageSource

	) {
		this.builderFactory = builderFactory;
		this.auditService = auditService;
		this.userService = userService;
		this.censorFactory = censorFactory;
		this.queryFactory = queryFactory;
		this.messageSource = messageSource;
	}

	@PostMapping("query")
	public QueryResult<User> Query(@RequestBody UserLookup lookup) throws MyApplicationException, MyForbiddenException {
		logger.debug("querying {}", User.class.getSimpleName());
		this.censorFactory.censor(UserCensor.class).censor(lookup.getProject(), null);
		UserQuery query = lookup.enrich(this.queryFactory).authorize(AuthorizationFlags.OwnerOrPermission);
		List<UserEntity> datas = query.collectAs(lookup.getProject());
		List<User> models = this.builderFactory.builder(UserBuilder.class).authorize(AuthorizationFlags.OwnerOrPermission).build(lookup.getProject(), datas);
		long count = (lookup.getMetadata() != null && lookup.getMetadata().getCountAll()) ? query.count() : models.size();

		this.auditService.track(AuditableAction.User_Query, "lookup", lookup);
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

		return new QueryResult(models, count);
	}

	@GetMapping("{id}")
	@Transactional
	public User Get(@PathVariable("id") UUID id, FieldSet fieldSet, Locale locale) throws MyApplicationException, MyForbiddenException, MyNotFoundException {
		logger.debug(new MapLogEntry("retrieving" + User.class.getSimpleName()).And("id", id).And("fields", fieldSet));

		this.censorFactory.censor(UserCensor.class).censor(fieldSet, id);

		UserQuery query = this.queryFactory.query(UserQuery.class).authorize(AuthorizationFlags.OwnerOrPermission).ids(id);
		User model = this.builderFactory.builder(UserBuilder.class).authorize(AuthorizationFlags.OwnerOrPermission).build(fieldSet, query.firstAs(fieldSet));
		if (model == null) throw new MyNotFoundException(messageSource.getMessage("General_ItemNotFound", new Object[]{id, User.class.getSimpleName()}, LocaleContextHolder.getLocale()));

		this.auditService.track(AuditableAction.User_Lookup, Map.ofEntries(
				new AbstractMap.SimpleEntry<String, Object>("id", id),
				new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
		));
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

		return model;
	}

	@PostMapping("persist")
	@Transactional
	public User Persist(@MyValidate @RequestBody UserPersist model, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
		logger.debug(new MapLogEntry("persisting" + User.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));

		User persisted = this.userService.persist(model, fieldSet);

		this.auditService.track(AuditableAction.User_Persist, Map.ofEntries(
				new AbstractMap.SimpleEntry<String, Object>("model", model),
				new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
		));
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);
		return persisted;
	}

	@PostMapping("access/persist")
	@Transactional
	public User Persist(@MyValidate @RequestBody UserAccessPersist model, FieldSet fieldSet) throws MyApplicationException, MyForbiddenException, MyNotFoundException, InvalidApplicationException {
		logger.debug(new MapLogEntry("persisting" + User.class.getSimpleName()).And("model", model).And("fieldSet", fieldSet));

		User persisted = this.userService.persist(model, fieldSet);

		this.auditService.track(AuditableAction.User_PersistAccess, Map.ofEntries(
				new AbstractMap.SimpleEntry<String, Object>("model", model),
				new AbstractMap.SimpleEntry<String, Object>("fields", fieldSet)
		));
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);
		return persisted;
	}

	@DeleteMapping("{id}")
	@Transactional
	public void Delete(@PathVariable("id") UUID id) throws MyForbiddenException, InvalidApplicationException {
		logger.debug(new MapLogEntry("retrieving" + User.class.getSimpleName()).And("id", id));

		this.userService.deleteAndSave(id);

		this.auditService.track(AuditableAction.User_Delete, "id", id);
		//this.auditService.trackIdentity(AuditableAction.IdentityTracking_Action);
	}


}
