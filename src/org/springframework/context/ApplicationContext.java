package org.springframework.context;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.event.ContextClosedEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ApplicationContext {  // контекст-реализыция контейнера
    private BeanFactory beanFactory = new BeanFactory();

    public ApplicationContext(String basePackage) throws ReflectiveOperationException {  //этапы инициализации
        System.out.println("******Context is under construction******");

        beanFactory.instantiate(basePackage);
        beanFactory.populateProperties();
        beanFactory.injectBeanNames();
        beanFactory.initializeBeans();
    }

    public void close() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {      //пройтись по всем нашим бинам, и выяснить, какие из них являются слушателями событий
        beanFactory.close();
        for (Object bean : beanFactory.getSingletons().values()) {
            if (bean instanceof ApplicationListener) {
                for (Type type : bean.getClass().getGenericInterfaces()) { //получить список из интерфейсов, которые данный бин реализует, и отфильтровать среди них те, которые имеют параметры
                    if (type instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) type;
                        Type firstParameter = parameterizedType.getActualTypeArguments()[0];  //извлекаем тип первого параметра, и убеждаемся, что он — наш класс события. Если это так, мы получаем наш метод через рефлексию и вызываем его:
                        if (firstParameter.equals(ContextClosedEvent.class)) {
                            Method method = bean.getClass().getMethod("onApplicationEvent", ContextClosedEvent.class);
                            method.invoke(bean, new ContextClosedEvent());

                        }
                    }

                }
            }
        }
    }
}
