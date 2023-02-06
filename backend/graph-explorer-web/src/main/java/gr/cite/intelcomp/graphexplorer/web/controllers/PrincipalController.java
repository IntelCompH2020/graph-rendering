package gr.cite.intelcomp.graphexplorer.web.controllers;

import gr.cite.commons.web.oidc.principal.CurrentPrincipalResolver;
import gr.cite.commons.web.oidc.principal.MyPrincipal;
import gr.cite.intelcomp.graphexplorer.audit.AuditableAction;
import gr.cite.intelcomp.graphexplorer.service.graph.GraphService;
import gr.cite.intelcomp.graphexplorer.web.model.Account;
import gr.cite.intelcomp.graphexplorer.web.model.AccountBuilder;
import gr.cite.tools.auditing.AuditService;
import gr.cite.tools.fieldset.BaseFieldSet;
import gr.cite.tools.fieldset.FieldSet;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/principal", produces = MediaType.APPLICATION_JSON_VALUE)
public class PrincipalController {
	private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(PrincipalController.class));
	private final AuditService auditService;

	private final CurrentPrincipalResolver currentPrincipalResolver;
	private final AccountBuilder accountBuilder;
	private final GraphService graphService;

	@Autowired
	public PrincipalController(
			CurrentPrincipalResolver currentPrincipalResolver,
			AccountBuilder accountBuilder,
			AuditService auditService, GraphService graphService) {
		this.currentPrincipalResolver = currentPrincipalResolver;
		this.accountBuilder = accountBuilder;
		this.auditService = auditService;
		this.graphService = graphService;
	}

	@GetMapping("me")
	public Account me(FieldSet fieldSet) {
		logger.debug("me");


		if (fieldSet == null || fieldSet.isEmpty()) {
			fieldSet = new BaseFieldSet(
					Account._isAuthenticated,
					BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._subject),
					BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._userId),
					BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._name),
					BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._scope),
					BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._client),
					BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._issuedAt),
					BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._notBefore),
					BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._authenticatedAt),
					BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._expiresAt),
					BaseFieldSet.asIndexer(Account._principal, Account.PrincipalInfo._more),
					Account._permissions);
		}

		MyPrincipal principal = this.currentPrincipalResolver.currentPrincipal();

		Account me = this.accountBuilder.build(fieldSet, principal);

		this.auditService.track(AuditableAction.Principal_Lookup);
		//auditService.trackIdentity(AuditableAction.IdentityTracking_Action);

		return me;

	}

}
