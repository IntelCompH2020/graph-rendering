package gr.cite.intelcomp.graphexplorer.common.validation;

import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class FieldNotNullIfOtherSetValidator implements ConstraintValidator<FieldNotNullIfOtherSet, Object> {

	private String notNullField;
	private String otherSetField;

	@Override
	public void initialize(FieldNotNullIfOtherSet constraintAnnotation) {
		this.notNullField = constraintAnnotation.notNullField();
		this.otherSetField = constraintAnnotation.otherSetField();
	}

	@Override
	public boolean isValid(Object entity, ConstraintValidatorContext context) {
		Object notNullValue = new BeanWrapperImpl(entity)
				.getPropertyValue(this.notNullField);
		Object otherSetValue = new BeanWrapperImpl(entity)
				.getPropertyValue(this.otherSetField);

		boolean hashIsString = Objects.equals(new BeanWrapperImpl(entity)
				.getPropertyType(this.otherSetField), String.class);

		boolean hashValueEmpty = otherSetValue == null || (hashIsString && ((String)otherSetValue).isBlank());

		if (notNullValue != null && hashValueEmpty) return false;
		return true;
	}
}
