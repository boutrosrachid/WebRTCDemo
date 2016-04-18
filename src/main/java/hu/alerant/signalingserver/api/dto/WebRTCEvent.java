package hu.alerant.signalingserver.api.dto;

import org.joda.time.DateTime;
import hu.alerant.signalingserver.api.WebRTCEvents;
import hu.alerant.signalingserver.exception.SignalingException;

import java.util.Map;
import java.util.Optional;

public interface WebRTCEvent {

	WebRTCEvents type();

    DateTime published();

    Optional<WebRTCMember> from();

    Optional<WebRTCMember> to();

    Optional<WebRTCConversation> conversation();

    Optional<SignalingException> exception();

    Map<String, String> custom();

    Optional<String> reason();

}
