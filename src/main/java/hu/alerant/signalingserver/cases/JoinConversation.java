package hu.alerant.signalingserver.cases;

import hu.alerant.signalingserver.api.WebRTCEventBus;
import hu.alerant.signalingserver.domain.Conversation;
import hu.alerant.signalingserver.domain.InternalMessage;
import hu.alerant.signalingserver.repository.Conversations;
import hu.alerant.signalingserver.domain.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static hu.alerant.signalingserver.api.WebRTCEvents.MEMBER_JOINED;
import static hu.alerant.signalingserver.exception.Exceptions.CONVERSATION_NOT_FOUND;

@Component
public class JoinConversation {

	@Autowired
	@Qualifier("WebRTCEventBus")
	private WebRTCEventBus eventBus;

	@Autowired
	private Conversations conversations;

	public void execute(InternalMessage message) {
		Conversation conversation = findConversationToJoin(message);
		Member joining = message.getFrom();

		if(message.getCustom() != null && message.getCustom().get("memberName") != null) {
			joining.setName(message.getCustom().get("memberName"));
		}

		conversation.join(joining);

        sendEventMemberJoinedFrom(message, conversation);
	}

    private void sendEventMemberJoinedFrom(InternalMessage message, Conversation conversation) {
        eventBus.post(MEMBER_JOINED.basedOn(message, conversation));
	}

	private Conversation findConversationToJoin(InternalMessage message) {
		return conversations.findBy(message.getContent()).orElseThrow(CONVERSATION_NOT_FOUND::exception);
	}

}
