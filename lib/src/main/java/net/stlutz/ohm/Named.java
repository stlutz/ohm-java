package net.stlutz.ohm;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface Named {
  String value() default Operation.defaultName;
}
