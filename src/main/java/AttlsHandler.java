package com.mycompany;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.apache.coyote.AbstractProtocol;

import java.lang.reflect.Field;
import java.nio.channels.SocketChannel;

@RequiredArgsConstructor
class AttlsHandler<T> implements AbstractEndpoint.Handler<T> {

    @Delegate(excludes = Proccess.class)
    private final AbstractEndpoint.Handler<T> original;

    private Field fdField;

    /**
     * Methods take file description from all known sockets
     *
     * @param socket implementation of socket in Tomcat
     * @return file descriptor value
     */
    private int getFd(SocketChannel socket) {
        if (fdField == null) {
            try {
                fdField = socket.getClass().getDeclaredField("fdVal");
                fdField.setAccessible(true);
            } catch (IllegalArgumentException | IllegalStateException | NoSuchFieldException e) {
                throw new IllegalStateException("Cannot find file descriptor (socket ID)", e);
            }
        }
        try {
            return fdField.getInt(socket);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot obtain file descriptor (socket ID)", e);
        }
    }

    /**
     * This method handles processing of each request. At first create AT-TLS context, the process and on the end
     * dispose the context.
     *
     * @param socketWrapperBase describing socket (see Tomcat implementation)
     * @param status            status of socket (see Tomcat implementation)
     * @return new status of socket (see Tomcat implementation)
     */
    public SocketState process(SocketWrapperBase socketWrapperBase, SocketEvent status) {
        NioChannel nioChannel = (NioChannel) socketWrapperBase.getSocket();
        Attls.init(getFd(nioChannel.getIOChannel()));
        try {
            return original.process(socketWrapperBase, status);
        } finally {
            Attls.dispose();
        }
    }

    interface Proccess {

        <T> SocketState process(SocketWrapperBase<T> socket, SocketEvent status);

    }

}
