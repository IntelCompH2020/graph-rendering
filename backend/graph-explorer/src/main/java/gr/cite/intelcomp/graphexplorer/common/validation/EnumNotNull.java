package gr.cite.intelcomp.graphexplorer.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public  class EnumNotNull implements ConstraintValidator<ValidEnum,Object> {
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return value != null;
    }
}
