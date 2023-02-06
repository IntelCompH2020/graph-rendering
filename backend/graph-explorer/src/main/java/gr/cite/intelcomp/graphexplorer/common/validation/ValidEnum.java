package gr.cite.intelcomp.graphexplorer.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;


@Constraint(validatedBy = EnumNotNull.class)
@Documented
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnum {
    String message() default "enum is required";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
