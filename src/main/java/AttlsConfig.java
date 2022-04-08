package com.mycompany;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Configuration
public class AttlsConfig {

    @Bean
    public <T> TomcatConnectorCustomizer attlsOnEmbededTomcat() {
        return connector -> {
            try {
                ProtocolHandler protocolHandler = connector.getProtocolHandler();

                Field handlerField = AbstractProtocol.class.getDeclaredField("handler");
                handlerField.setAccessible(true);
                AbstractEndpoint.Handler<T> handler = (AbstractEndpoint.Handler<T>) handlerField.get(protocolHandler);

                handler = new AttlsHandler<>(handler);

                Method methodHandler = AbstractProtocol.class.getDeclaredMethod("getEndpoint");
                methodHandler.setAccessible(true);
                AbstractEndpoint<T, ?> endpoint = (AbstractEndpoint<T, ?>) methodHandler.invoke(protocolHandler);
                endpoint.setHandler(handler);

                handlerField.set(protocolHandler, handler);
            } catch (Exception e) {
                throw new IllegalStateException("Cannot prepare AT-TLS handler", e);
            }
        };
    }

}