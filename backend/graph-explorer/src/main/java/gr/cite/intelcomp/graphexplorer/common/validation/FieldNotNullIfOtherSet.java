package gr.cite.intelcomp.graphexplorer.common.validation;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Constraint( validatedBy = { FieldNotNullIfOtherSetValidator.class } )
@Documented
@Target( { ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
public @interface FieldNotNullIfOtherSet {
	Class<?>[] groups() default {};

	String notNullField() default "id";
	String otherSetField() default "hash";
	String failOn() default "hash";

	String message() default "hash is required if id is set";

	Class<? extends Payload>[] payload() default {};
}
