package gr.cite.intelcomp.graphexplorer.common.validation;

import gr.cite.intelcomp.graphexplorer.convention.ConventionService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.UUID;

public class ValidIdValidator  implements ConstraintValidator<ValidId, Object> {

	@Autowired
	private ConventionService conventionService;

	@Override
	public void initialize(ValidId constraintAnnotation) { }

	@Override
	public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
		if(o == null) return true;
		else if(o instanceof UUID){
			UUID uuidId = (UUID)o;
			return this.conventionService.isValidGuid(uuidId);
		}
		else if(o instanceof Integer){
			Integer intId = (Integer)o;
			return this.conventionService.isValidId(intId);
		}
		else{
			String stringId = o.toString();
			UUID uuidId = null;
			try {
				uuidId = UUID.fromString(stringId);
			}catch (Exception ex){
				return false;
			}
			return this.conventionService.isValidGuid(uuidId);
		}
	}
}
