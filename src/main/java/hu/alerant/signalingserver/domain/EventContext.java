package hu.alerant.signalingserver.domain;

import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import hu.alerant.signalingserver.api.WebRTCEvents;
import hu.alerant.signalingserver.api.dto.WebRTCConversation;
import hu.alerant.signalingserver.api.dto.WebRTCEvent;
import hu.alerant.signalingserver.api.dto.WebRTCMember;
import hu.alerant.signalingserver.exception.SignalingException;

import java.util.Map;
import java.util.Optional;

import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;

public class EventContext implements WebRTCEvent {

    private final WebRTCEvents type;
    private final DateTime published = DateTime.now();
    private final Map<String, String> custom = Maps.newHashMap();
    private final Optional<WebRTCMember> from;
    private final Optional<WebRTCMember> to;
    private final Optional<WebRTCConversation> conversation;
    private final Optional<SignalingException> exception;
    private final String reason;

    private EventContext(WebRTCEvents type, WebRTCMember from, WebRTCMember to, WebRTCConversation conversation, SignalingException exception, String reason) {
        this.type = type;
        this.from = ofNullable(from);
        this.to = ofNullable(to);
        this.conversation = ofNullable(conversation);
        this.exception = ofNullable(exception);
        this.reason = reason;
    }

    @Override
    public WebRTCEvents type() {
        return type;
    }

    @Override
    public DateTime published() {
        return published;
    }

    @Override
    public Optional<WebRTCMember> from() {
        return from;
    }

    @Override
    public Optional<WebRTCMember> to() {
        return to;
    }

    @Override
    public Optional<WebRTCConversation> conversation() {
        return conversation;
    }

    @Override
    public Optional<SignalingException> exception() {
        return exception;
    }

    @Override
    public Map<String, String> custom() {
        return unmodifiableMap(custom);
    }

    @Override
    public Optional<String> reason() {
        return Optional.ofNullable(reason);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) <- %s -> (%s)", type, from, conversation, to);
    }

    public static EventContextBuilder builder() {
        return new EventContextBuilder();
    }

    public static class EventContextBuilder {
        private Map<String, String> custom;
        private WebRTCEvents type;
        private WebRTCMember from;
        private WebRTCMember to;
        private WebRTCConversation conversation;
        private SignalingException exception;
        private String reason;

        public EventContextBuilder reason(String reason){
            this.reason = reason;
            return this;
        }

        public EventContextBuilder type(WebRTCEvents type) {
            this.type = type;
            return this;
        }

        public EventContextBuilder custom(Map<String, String> custom) {
            this.custom = custom;
            return this;
        }

        public EventContextBuilder from(WebRTCMember from) {
            this.from = from;
            return this;
        }

        public EventContextBuilder to(WebRTCMember to){
            this.to = to;
            return this;
        }

        public EventContextBuilder conversation(WebRTCConversation conversation) {
            this.conversation = conversation;
            return this;
        }

        public EventContextBuilder exception(SignalingException exception) {
            this.exception = exception;
            return this;
        }

        public WebRTCEvent build() {
            if (type == null) {
                throw new IllegalArgumentException("Type is required");
            }
            EventContext eventContext = new EventContext(type, from, to, conversation, exception, reason);
            if (custom != null) {
                eventContext.custom.putAll(custom);
            }
            return eventContext;
        }
    }
}
