package net.stlutz.ohm;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

@Retention(RUNTIME)
@Target(METHOD)
@Repeatable(Actions.class)
public @interface Action {
	String value() default "";
}
