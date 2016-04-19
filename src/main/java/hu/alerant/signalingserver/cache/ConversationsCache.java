package hu.alerant.signalingserver.cache;

import java.util.HashMap;
import java.util.List;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component("applicationCache")
@Scope("singleton")
public class ConversationsCache {

	private CacheContainer container;

	private Cache<String, ConversationCacheEntry> cache;

	@PostConstruct
	public void initCache() throws Exception {
		container = (CacheContainer)new javax.naming.InitialContext().lookup("java:jboss/infinispan/container/applicationCache");
		this.cache = container.getCache();
	}

	public HashMap<String, List<String>> listConversations() {
		HashMap<String, List<String>> map = new HashMap();
		for(ConversationCacheEntry c : this.cache.values()) {
			map.put(c.conversationName, c.members);
		}
		return map;
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
