package org.springframework.beans.factory.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PreDestroy {//предназначена для методов, которые должны быть вызваны при закрытии контейнера
}
