package hu.alerant.signalingserver.api;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import hu.alerant.signalingserver.cache.ConversationsCache;

@RestController
@RequestMapping(value = "/conversations")
public class RESTConversationQuery {

	@Autowired
	private ConversationsCache cache;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public @ResponseBody Map<String, String> listConversations() {
		return cache.listConversations();
    }
}