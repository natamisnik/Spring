package org.springframework.beans.factory;

public interface DisposableBean {//который содержит метод void destroy(). Все бины, исполняющие данный интерфейс, будут иметь возможность сами себя уничтожить (освободить ресурсы, например).
    void destroy();
}
