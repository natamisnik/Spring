package org.springframework.context;

public interface ApplicationListener <E>{ //позволит бинам слушать наши события(типизация по классу события)
    void onApplicationEvent(E event);

}
