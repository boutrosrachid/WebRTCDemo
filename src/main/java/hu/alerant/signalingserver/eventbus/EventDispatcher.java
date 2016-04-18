package hu.alerant.signalingserver.eventbus;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import hu.alerant.signalingserver.api.WebRTCEvents;
import hu.alerant.signalingserver.api.WebRTCHandler;
import hu.alerant.signalingserver.api.annotation.WebRTCEventListener;
import hu.alerant.signalingserver.api.dto.WebRTCEvent;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

import static org.springframework.core.annotation.AnnotationUtils.getValue;

@Component("WebRTCEventDispatcher")
@Scope("singleton")
@WebRTCEventListener
public class EventDispatcher {

	@Autowired
	private ApplicationContext context;

	@Subscribe
	@AllowConcurrentEvents
	public void handle(WebRTCEvent event) {
		getWebRTCEventListeners().stream()
				.filter(listener -> isWebRTCHandler(listener) && supportsCurrentEvent(listener, event))
				.forEach(listener -> {
					((WebRTCHandler) listener).handleEvent(event);
				});
	}

	private boolean isWebRTCHandler(Object listener) {
		return listener instanceof WebRTCHandler;
	}

	private boolean supportsCurrentEvent(Object listener, WebRTCEvent event) {
	    WebRTCEvents[] events = getSupportedEvents(listener);
		for (WebRTCEvents supportedEvent : events) {
			if (isSupporting(event, supportedEvent)) {
				return true;
			}
		}
		return false;
	}

	private boolean isSupporting(WebRTCEvent msg, WebRTCEvents supportedEvent) {
		return supportedEvent.equals(msg.type());
	}

	private WebRTCEvents[] getSupportedEvents(Object listener) {
	    try {
            if (AopUtils.isJdkDynamicProxy(listener)) {
                listener = ((Advised) listener).getTargetSource().getTarget();
            }
        } catch (Exception e) {
            return new WebRTCEvents[0];
        }
		return (WebRTCEvents[]) getValue(listener.getClass().getAnnotation(WebRTCEventListener.class));
	}

	private Collection<Object> getWebRTCEventListeners() {
		Map<String, Object> beans = context.getBeansWithAnnotation(WebRTCEventListener.class);
		beans.remove("WebRTCEventDispatcher");
		return beans.values();
	}
}
