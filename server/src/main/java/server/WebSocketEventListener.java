package server;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashSet;
import java.util.Set;

@Component
public class WebSocketEventListener {
    public Set<String> sessionIds = new HashSet<>();

    /**
     * Add connected websocket connection to the list.
     *
     * @param event Connection event
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        String id = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
        sessionIds.add(id);
    }

    /**
     * Remove websocket connection from the list.
     *
     * @param event Disconnect event
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String id = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
        sessionIds.remove(id);
    }

}