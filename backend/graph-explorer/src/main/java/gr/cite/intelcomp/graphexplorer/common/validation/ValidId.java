package gr.cite.intelcomp.graphexplorer.common.validation;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Constraint( validatedBy = { ValidIdValidator.class } )
@Documented
@Target( { ElementType.FIELD } )
@Retention( RetentionPolicy.RUNTIME )
public @interface ValidId {
	Class<?>[] groups() default {};

	String message() default "id set but not valid";

	Class<? extends Payload>[] payload() default {};
}
