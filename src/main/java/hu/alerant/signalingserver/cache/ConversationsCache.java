package hu.alerant.signalingserver.cache;

import java.util.HashMap;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component("ConversationsCache")
@Scope("singleton")
public class ConversationsCache {

	private CacheContainer container;

	private Cache<String, ConversationCacheEntry> cache;

	@PostConstruct
	public void initCache() throws Exception {
		container = (CacheContainer)new javax.naming.InitialContext().lookup("java:jboss/infinispan/container/conversationsCache");
		this.cache = container.getCache();
	}

	public HashMap<String, String> listConversations() {
		HashMap<String, String> list = new HashMap<String, String>();
		for(ConversationCacheEntry c : this.cache.values()) {
			list.put(c.conversationName, c.toString());
		}
		return list;
	}

	public void createConversation(String name) {
		ConversationCacheEntry c = new ConversationCacheEntry(name);
		this.cache.put(name, c);
	}

	public void removeConversation(String name) {
		this.cache.remove(name);
	}

	public void addMemberToConversation(String name, String member) {
		ConversationCacheEntry c = (ConversationCacheEntry)this.cache.get(name);
		if(c != null) {
			c.members.add(member);
			this.cache.put(name, c);
		}
	}

	public void removeMemberFromConversation(String name, String member) {
		ConversationCacheEntry c = (ConversationCacheEntry)this.cache.get(name);
		if(c != null) {
			c.members.remove(member);
			this.cache.put(name, c);
		}
	}

}
