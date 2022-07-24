package com.eka.connect.creditrisk.validator;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { NotNullCustomValidator.class })
@Documented
public @interface NotNullDependsOn {


	 String message() default "This field is mandatory";
	 
	String[] fieldToValidate() default {};
	String[] fieldMessages() default {};
	String dependsOn() default "";
	
	String[] dependsOnPropertyValueAcceptableList()default {};
	
	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};
}

/**
 * Defines several {@link NotNull} annotations on the same element.
 *
 * @see javax.validation.constraints.NotNull
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@interface List {

	NotNullDependsOn[] value();
}

class NotNullCustomValidator implements
		ConstraintValidator<NotNullDependsOn, Object> {

	static final Logger LOGGER = LoggerFactory
			.getLogger(NotNullCustomValidator.class);
	  private String[] fieldToValidate;
	  private String[] validationMessages;
	  private String[]dependsOnPropertyValueAcceptableList;
	  String dependsOn;

	  	
	    public void initialize(final NotNullDependsOn constraintAnnotation) {
	        this.dependsOn = constraintAnnotation.dependsOn();
	        this.fieldToValidate = constraintAnnotation.fieldToValidate();
	        this.validationMessages = constraintAnnotation.fieldMessages();
	        this.dependsOnPropertyValueAcceptableList = constraintAnnotation.dependsOnPropertyValueAcceptableList();
	    }
	
	@Override
	public boolean isValid(Object value,
			ConstraintValidatorContext context) {
		
		boolean isValid = true;
		context.disableDefaultConstraintViolation();
		try {
			if (StringUtils.isEmpty(dependsOn) || fieldToValidate == null || fieldToValidate.length == 0

			|| validationMessages == null || validationMessages.length == 0 || validationMessages.length!= fieldToValidate.length){
				return true;
			}
			PropertyDescriptor pd = null;
			pd = new PropertyDescriptor(dependsOn, value.getClass());
			String dependsOnPropertyValue = (String)pd.getReadMethod().invoke(value);
			if (dependsOnPropertyValue != null) {
				for (int i = 0; i < fieldToValidate.length; i++) {
					boolean isMandatory = true;
					pd = new PropertyDescriptor(fieldToValidate[i],
							value.getClass());
					String actualValue = (String) pd.getReadMethod().invoke(
							value);

					if (dependsOnPropertyValueAcceptableList != null
							&& dependsOnPropertyValueAcceptableList.length > 0) {
						isMandatory = false;
						for (int j = 0; j < dependsOnPropertyValueAcceptableList.length; j++) {

							if (dependsOnPropertyValue
									.equalsIgnoreCase(dependsOnPropertyValueAcceptableList[j])) {
								isMandatory = true;
								break;
							}
						}
					} 
					if (isMandatory && (StringUtils.isEmpty(actualValue) || actualValue.trim().length()==0)){
						context.buildConstraintViolationWithTemplate(
								validationMessages[i]).addConstraintViolation();
						isValid = isValid &&  false;
					}
				}
			}
		}
			catch (IllegalArgumentException |InvocationTargetException e) {
			LOGGER.error("IllegalArgumentException  or  InvocationTargetException ..", e);
		}
			catch(IntrospectionException e){
				LOGGER.error("IntrospectionException..", e);	
			
		} catch (IllegalAccessException  e) {
			LOGGER.error("IllegalAccessException..", e);
			
		}   
		return isValid;
		
	}
}
