package hu.alerant.signalingserver.api;

import hu.alerant.signalingserver.api.dto.WebRTCEvent;

public interface WebRTCHandler {

	void handleEvent(WebRTCEvent event);

}
