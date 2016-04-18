package hu.alerant.signalingserver.eventbus;

import hu.alerant.signalingserver.api.WebRTCEventBus;
import hu.alerant.signalingserver.api.annotation.WebRTCEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component("WebRTCEventBusSetup")
@Scope("singleton")
public class EventBusSetup {

	@Autowired
	@Qualifier("WebRTCEventBus")
	private WebRTCEventBus eventBus;

	@Autowired
	private ApplicationContext context;

	@PostConstruct
	public void setupHandlers() {
		context.getBeansWithAnnotation(WebRTCEventListener.class).values()
				.forEach(eventBus::register);
	}
}
