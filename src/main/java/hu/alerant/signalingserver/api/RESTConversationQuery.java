package hu.alerant.signalingserver.api;

import java.util.Map;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import hu.alerant.signalingserver.cache.ConversationsCache;
import hu.alerant.signalingserver.repository.Conversations;

@RestController
@RequestMapping(value = "/conversations")
public class RESTConversationQuery {

	@Autowired
	private ConversationsCache cache;

	@Autowired
	private Conversations conversations;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public @ResponseBody Map<String, List<String>> listConversations() {
		return cache.listConversations();
    }

    @RequestMapping(value = "/shutdown", method = RequestMethod.GET)
    public void shutdown() {
		conversations.destroy();
	}

}