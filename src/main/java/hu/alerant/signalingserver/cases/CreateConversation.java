package hu.alerant.signalingserver.cases;

import static hu.alerant.signalingserver.api.WebRTCEvents.CONVERSATION_CREATED;

import org.apache.commons.lang3.StringUtils;
import hu.alerant.signalingserver.api.WebRTCEventBus;
import hu.alerant.signalingserver.domain.Conversation;
import hu.alerant.signalingserver.domain.InternalMessage;
import hu.alerant.signalingserver.domain.Member;
import hu.alerant.signalingserver.repository.Conversations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class CreateConversation {

	@Autowired
	@Qualifier("WebRTCEventBus")
	private WebRTCEventBus eventBus;

	@Autowired
	private Conversations conversations;

	public void execute(InternalMessage message) {
		Member creating = message.getFrom();
		if(message.getCustom() != null && message.getCustom().get("memberName") != null) {
			creating.setName(message.getCustom().get("memberName"));
		}

		Conversation conversation = createConversationUsing(message);

		conversation.join(creating);

        sendEventConversationCreatedFrom(message, conversation);
	}

    private void sendEventConversationCreatedFrom(InternalMessage message, Conversation conversation) {
        eventBus.post(CONVERSATION_CREATED.basedOn(message, conversation));
	}

	private Conversation createConversationUsing(InternalMessage message) {
		if (StringUtils.isNotBlank(message.getContent())) {
			return conversations.create(message.getContent());
		}
		return conversations.create();
	}

}
