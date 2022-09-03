package com.nabrami;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.InvocationTargetException;

public class Main {
    public static void main(String[] args) throws ReflectiveOperationException {

        new Main();

    }
    public Main(){
        try {
            testBeanFactory();
            testContext();
        }catch (ReflectiveOperationException e){
            e.printStackTrace();
        }
    }
    void testBeanFactory() throws ReflectiveOperationException{
        BeanFactory beanFactory = new BeanFactory();
        beanFactory.addPostProcessor(new CustomPostProcessor());

        beanFactory.instantiate("com.nabrami");////сканировал наш пакет и находил в нем классы, которые аннотированы @Component+@Service и добавляет
        beanFactory.populateProperties(); //найти поля с аннотацями зависимостей и инжектировать зависимости(сформируем название сеттера и вызовем его)
        beanFactory.injectBeanNames();//проверяем, реализует ли бин наш интерфейс, и вызываем сеттер
        beanFactory.initializeBeans();//инициализировать самого себя

        ProductService productService = (ProductService) beanFactory.getBean("productService");
        System.out.println(productService);//ProductService@612

        PromotionsService promotionsService = productService.getPromotionsService();
        System.out.println(promotionsService);
        System.out.println("Bean name = " + promotionsService.getBeanName());

        System.out.println(promotionsService.getClass());

        beanFactory.close();
    }
    void testContext() throws ReflectiveOperationException{
        ApplicationContext applicationContext = new ApplicationContext("com.nabrami");
        applicationContext.close();
    }



}
