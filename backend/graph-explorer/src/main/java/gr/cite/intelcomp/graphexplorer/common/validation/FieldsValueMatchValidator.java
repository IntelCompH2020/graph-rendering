package gr.cite.intelcomp.graphexplorer.common.validation;

import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FieldsValueMatchValidator implements ConstraintValidator<FieldsValueMatch, Object> {

	private String field;
	private String fieldMatch;

	@Override
	public void initialize(FieldsValueMatch constraintAnnotation) {
		this.field = constraintAnnotation.field();
		this.fieldMatch = constraintAnnotation.fieldMatch();
	}

	@Override
	public boolean isValid(Object entity, ConstraintValidatorContext context) {

		Object fieldValue = new BeanWrapperImpl(entity).getPropertyValue(field);
		Object fieldMatchValue = new BeanWrapperImpl(entity).getPropertyValue(fieldMatch);

		if (fieldValue != null) {
			return fieldValue.equals(fieldMatchValue);
		} else {
			return fieldMatchValue == null;
		}
	}
}
