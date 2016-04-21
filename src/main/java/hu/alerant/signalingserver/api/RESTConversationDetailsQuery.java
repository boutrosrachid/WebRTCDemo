package hu.alerant.signalingserver.api;

import java.util.HashMap;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import hu.alerant.signalingserver.cache.ConversationsCache;

@RestController
@RequestMapping(value = "/conversationdetails")
public class RESTConversationDetailsQuery {

	@Autowired
	private ConversationsCache cache;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public @ResponseBody HashMap<String, HashMap<String, List<String>>> listConversationsByNodes() {
		return cache.listConversationsByNodes();
    }

}