package org.springframework.beans.factory.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {//помечать этой аннотацией те поля, которые являются зависимостями.
}
