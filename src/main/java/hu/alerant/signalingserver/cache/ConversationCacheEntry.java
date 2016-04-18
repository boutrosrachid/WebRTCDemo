package hu.alerant.signalingserver.cache;

import java.io.Serializable;
import java.util.ArrayList;

public class ConversationCacheEntry implements Serializable {

	String conversationName;
	ArrayList<String> members;

	ConversationCacheEntry(String conversationName) {
		this.conversationName = conversationName;
		members = new ArrayList<String>();
	}

	public String toString() {
		String s = conversationName + " (";
		for(int i = 0; i < members.size(); i++) {
			s = s + members.get(i);
			if(i != members.size() -1) {
				s = s + ", ";
			}
		}
		s = s + ")";
		return s;
	}

}