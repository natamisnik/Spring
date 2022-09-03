package org.springframework.beans.factory;

public interface InitializingBean {
    void afterPropertiesSet();//нужно предоставить бину возможность инициализировать самого себя
}
