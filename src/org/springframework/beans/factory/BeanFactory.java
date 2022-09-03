package org.springframework.beans.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.PreDestroy;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.stereotype.Component;
import org.springframework.beans.factory.stereotype.Service;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanFactory {
    private Map<String, Object> singletons = new HashMap();

    public Object getBean(String beanName) {
        return singletons.get(beanName);
    }

    private List<BeanPostProcessor> postProcessors = new ArrayList<>();

    public void addPostProcessor(BeanPostProcessor postProcessor) {
        postProcessors.add(postProcessor);
    }

    public Map<String, Object> getSingletons() {
        return singletons;
    }

    public void instantiate(String basePackage) {   //сканировал наш пакет и находил в нем классы, которые аннотированы @Component+@Service и добавляет их в мап
        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader(); //получить ClassLoader. Он отвечает за загрузку классов

            String path = basePackage.replace('.', '/'); //преобразовать пакетный путь в путь к папке
            Enumeration<URL> resources = classLoader.getResources(path);//получить что-то типа List<URL> (пути в вашей файловой системе, по которым можно искать class-файлы)

            while (resources.hasMoreElements()) { //Преобразуем URL в файл, а затем получаем его имя
                URL resource = resources.nextElement();
                File file = new File(resource.toURI());

                for (File classFile : file.listFiles()) {
                    String fileName = classFile.getName();//ProductService.class
                    System.out.println(fileName);
                    if (fileName.endsWith(".class")) {
                        String className = fileName.substring(0, fileName.lastIndexOf(".")); //Дальше, нам нужно получить название файла без расширения.

                        Class classObject = Class.forName(basePackage + "." + className); //Далее, мы можем по полному имени класса получить объект класса

                        if (classObject.isAnnotationPresent(Component.class) || classObject.isAnnotationPresent(Service.class)) { //выделить среди них те, что имеют аннотацию @Component и Service
                            System.out.println("Component: " + classObject);

                            Object instance = classObject.newInstance();//=new CustomClass() Теперь нам нужно создать наш бин. Надо сделать что-то вроде new ProductService(), но для каждого бина у нас свой класс. Рефлексия в Java предоставляет нам универсальное решение (вызывается конструктор по-умолчанию):
                            String beanName = className.substring(0, 1).toLowerCase() + className.substring(1); //нужно поместить этот бин в Map<String, Object> singletons. Для этого нужно выбрать имя бина (его id). В Java мы называем переменные подобно классам (только первая буква в нижнем регистре)
                            singletons.put(beanName, instance);
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    public void populateProperties() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {     //находить эти аннотации и инжектировать зависимости по ним
        System.out.println("==populateProperties==");
        for (Object object : singletons.values()) {
            for (Field field : object.getClass().getDeclaredFields()) {   //по всем нашим бинам в мапе singletons, и для каждого бина пройтись по всем его полям
                if (field.isAnnotationPresent(Autowired.class)) {
                    for (Object dependency : singletons.values()) {
                        if (dependency.getClass().equals(field.getType())) {//ещё разок пройтись по всем бинам и посмотреть их тип
                            String setterName = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);//setPromotionsService
                            System.out.println("Setter name = " + setterName);
                            Method setter = object.getClass().getMethod(setterName, dependency.getClass());
                            setter.invoke(object, dependency);  //То, что мы реализовали — это инъекция по типу, вкладываем зависимость через сеттер

                        }

                    }
                }
            }

        }
    }

    public void injectBeanNames() {  //проверяем, реализует ли бин наш интерфейс, и вызываем сеттер
        for (String name : singletons.keySet()) {
            Object bean = singletons.get(name);

            if (bean instanceof BeanNameAware) {
                ((BeanNameAware) bean).setBeanName(name);
            }
        }
    }

    public void initializeBeans(){
        for (String name : singletons.keySet()) {
        Object bean = singletons.get(name);

        for (BeanPostProcessor postProcessor : postProcessors) {
            postProcessor.postProcessBeforeInitialization(bean, name);
        }

        if (bean instanceof InitializingBean) {
            ((InitializingBean) bean).afterPropertiesSet();     //инициализировать самого себя
        }

        for (BeanPostProcessor postProcessor : postProcessors) {
            postProcessor.postProcessAfterInitialization(bean, name);
        }
    }
    }



    public void close() {
        for (Object bean : singletons.values()) {
            for (Method method : bean.getClass().getMethods()) {
                if (method.isAnnotationPresent(PreDestroy.class)) {
                    try {
                        method.invoke(bean);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (bean instanceof DisposableBean) {
                ((DisposableBean) bean).destroy();
            }
        }
    }
}
