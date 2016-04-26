package hu.alerant.signalingserver.api;

import com.google.common.eventbus.EventBus;
import lombok.extern.log4j.Log4j;
import hu.alerant.signalingserver.api.dto.WebRTCEvent;
import hu.alerant.signalingserver.repository.Conversations;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.DisposableBean;

@Log4j
@Service("WebRTCEventBus")
@Scope("singleton")
public class WebRTCEventBus implements DisposableBean {

	private EventBus eventBus;

	@Autowired
	private Conversations conversations;

	public WebRTCEventBus() {
		this.eventBus = new EventBus();
	}

	public void post(WebRTCEvent event) {
        log.info("POSTED EVENT: " + event);
		eventBus.post(event);
	}

	@Deprecated
	public void post(Object o) {
		eventBus.post(o);
	}

	public void register(Object listeners) {
        log.info("REGISTERED LISTENER: " + listeners);
		eventBus.register(listeners);
	}

	public void destroy() throws Exception {
		conversations.destroy();
	}

}
