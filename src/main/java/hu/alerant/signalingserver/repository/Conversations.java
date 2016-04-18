package hu.alerant.signalingserver.repository;

import com.google.common.collect.Maps;
import hu.alerant.signalingserver.domain.Conversation;
import hu.alerant.signalingserver.domain.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import hu.alerant.signalingserver.cache.ConversationsCache;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static hu.alerant.signalingserver.exception.Exceptions.CONVERSATION_NAME_OCCUPIED;
import static hu.alerant.signalingserver.exception.Exceptions.INVALID_CONVERSATION_NAME;

@Repository
public class Conversations {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private ConversationsCache cache;

	private Map<String, Conversation> conversations = Maps.newConcurrentMap();

	public Optional<Conversation> findBy(String id) {
		if (isEmpty(id)) {
			return Optional.empty();
		}
		return Optional.ofNullable(conversations.get(id));
	}

	public void remove(String id) {
		conversations.remove(id);
		cache.removeConversation(id);
	}

	public Conversation create() {
		return create(UUID.randomUUID().toString());
	}

	public Conversation create(String name) {
		validate(name);
		Conversation conversation = fetchConversationFromContext(name);
		registerInContext(name, conversation);
		cache.createConversation(name);
		return conversation;
	}

	private void registerInContext(String name, Conversation conversation) {
		conversations.put(name, conversation);
	}

	private Conversation fetchConversationFromContext(String name) {
		return context.getBean(Conversation.class, name);
	}

	private void validate(String name) {
		if (isEmpty(name)) {
			throw INVALID_CONVERSATION_NAME.exception();
		}
		if (conversations.containsKey(name)) {
			throw CONVERSATION_NAME_OCCUPIED.exception();
		}
	}

	public Collection<String> getAllIds() {
		return conversations.keySet();
	}

	public Optional<Conversation> getBy(Member member) {
		for (String conversationIds : conversations.keySet()) {
			Conversation conversation = conversations.get(conversationIds);
			if (conversation.has(member)) {
				return Optional.of(conversation);
			}
		}
		return Optional.empty();
	}
}
