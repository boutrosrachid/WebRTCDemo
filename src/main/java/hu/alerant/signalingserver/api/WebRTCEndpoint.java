package hu.alerant.signalingserver.api;

import java.util.Set;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import lombok.extern.log4j.Log4j;

import hu.alerant.signalingserver.domain.Message;
import hu.alerant.signalingserver.domain.Server;
import hu.alerant.signalingserver.codec.MessageDecoder;
import hu.alerant.signalingserver.codec.MessageEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

@Log4j
@Component
@ServerEndpoint(value = "/signaling",//
decoders = MessageDecoder.class,//
encoders = MessageEncoder.class)
public class WebRTCEndpoint {

	private Server server;

	private static Set<WebRTCEndpoint> endpoints = Sets.newConcurrentHashSet();

	public WebRTCEndpoint() {
		endpoints.add(this);
		log.info("Created " + this);
		endpoints.stream().filter(e -> e.server != null).findFirst().ifPresent(s -> this.setServer(s.server));
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		log.info("Opening: " + session.getId());
		server.register(session);
	}

	@OnMessage
	public void onMessage(Message message, Session session) {
		log.info("Handling message from: " + session.getId());
		server.handle(message, session);
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		log.info("Closing: " + session.getId() + " with reason: " + reason.getReasonPhrase());
		server.unregister(session, reason);
	}

	@OnError
	public void onError(Session session, Throwable exception) {
		log.info("Occured exception for session: " + session.getId());
		log.error(exception);
		server.handleError(session, exception);
	}

	@Autowired
	public void setServer(Server server) {
		log.info("Setted server: " + server + " to " + this);
		this.server = server;
	}
}
