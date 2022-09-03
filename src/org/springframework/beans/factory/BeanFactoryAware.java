package org.springframework.beans.factory;

public interface BeanFactoryAware {
    void getBeanName();
    //позволит бинам получать ссылку на фабрику бинов
}
