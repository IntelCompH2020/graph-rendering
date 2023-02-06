package gr.cite.intelcomp.graphexplorer.common.validation;

import gr.cite.intelcomp.graphexplorer.errorcode.ErrorThesaurusProperties;
import gr.cite.tools.exception.MyValidationException;
import gr.cite.tools.validation.BaseValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.validation.Validator;
import java.util.List;
import java.util.Map;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ValidationServiceImpl extends BaseValidationService {

	private ErrorThesaurusProperties errors;

	@Autowired
	public ValidationServiceImpl(Validator validator, ErrorThesaurusProperties errors){
		super(validator);
		this.errors = errors;
	}

	@Override
	public <T> void validateForce(T item, Class<?>... groups) {
		List<Map.Entry<String, List<String>>> validationErrors = this.validate(item, groups);
		if (validationErrors != null && !validationErrors.isEmpty()) {
			throw new MyValidationException(this.errors.getModelValidation().getCode(),
					this.errors.getModelValidation().getMessage(),
					validationErrors);
		}
	}
}
