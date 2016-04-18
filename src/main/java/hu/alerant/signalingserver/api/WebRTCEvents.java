package hu.alerant.signalingserver.api;

import hu.alerant.signalingserver.api.dto.WebRTCEvent;
import hu.alerant.signalingserver.domain.Conversation;
import hu.alerant.signalingserver.domain.EventContext;
import hu.alerant.signalingserver.domain.InternalMessage;
import hu.alerant.signalingserver.exception.Exceptions;

import javax.websocket.Session;

public enum WebRTCEvents {
    SESSION_OPENED,
    SESSION_CLOSED,
    CONVERSATION_CREATED,
    CONVERSATION_DESTROYED,
    UNEXPECTED_SITUATION,
    MEMBER_JOINED,
    MEMBER_LEFT,
    MEDIA_LOCAL_STREAM_REQUESTED,
    MEDIA_LOCAL_STREAM_CREATED,
    MEDIA_STREAMING,
    ;

    public WebRTCEvent basedOn(InternalMessage message, Conversation conversation) {
        return EventContext.builder()
                .from(message.getFrom())
                .to(message.getTo())
                .custom(message.getCustom())
                .conversation(conversation)
                .type(this)
                .build();
    }

    public WebRTCEvent occurFor(Session session, String reason) {
        return EventContext.builder()
                .from(session::getId)
                .type(this)
                .reason(reason)
                .build();
    }

    public WebRTCEvent occurFor(Session session) {
        return EventContext.builder()
                .type(this)
                .from(session::getId)
                .exception(Exceptions.UNKNOWN_ERROR.exception())
                .build();
    }
}
