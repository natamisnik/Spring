package org.springframework.beans.factory;

public interface BeanNameAware {  ///Все бины, что его реализуют — получает своё имя
    void setBeanName(String name);
}
