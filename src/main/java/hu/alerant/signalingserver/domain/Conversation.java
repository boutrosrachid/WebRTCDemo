package hu.alerant.signalingserver.domain;


import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import hu.alerant.signalingserver.api.dto.WebRTCConversation;
import hu.alerant.signalingserver.cases.ExchangeSignalsBetweenMembers;
import hu.alerant.signalingserver.cases.JoinMember;
import hu.alerant.signalingserver.cases.LeftMember;
import hu.alerant.signalingserver.cache.ConversationsCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.Set;

@Log4j
@Getter
@Component
@Scope("prototype")
public class Conversation implements WebRTCConversation {

	@Autowired
	private ExchangeSignalsBetweenMembers exchange;

	@Autowired
	private JoinMember join;

	@Autowired
	private LeftMember left;

	@Autowired
	private ConversationsCache cache;

	private String id;

	private Set<Member> members = Sets.newConcurrentHashSet();

	@Autowired
	public Conversation(String id) {
		this.id = id;
	}

	public synchronized void join(Member sender) {
        informSenderThatHasBeenJoined(sender);

        informRestAndBeginSignalExchange(sender);

        members.add(sender);

        cache.addMemberToConversation(id, sender.getName());
    }

    private void informRestAndBeginSignalExchange(Member sender) {
        for (Member member : members) {
			join.sendMessageToOthers(sender, member);
			exchange.begin(member, sender);
		}
    }

    private void informSenderThatHasBeenJoined(Member sender) {
        if (isWithoutMember()) {
            join.sendMessageToFirstJoined(sender, id);
        } else {
            join.sendMessageToJoining(sender, id);
        }
    }

	public boolean isWithoutMember() {
		return members.size() == 0;
	}

	public boolean has(Member member) {
		return member != null && members.contains(member);
	}

	public synchronized void left(Member leaving) {
		members.remove(leaving);
		for (Member member : members) {
			left.executeFor(leaving, member);
		}
        cache.removeMemberFromConversation(id, leaving.getName());
	}

	public void execute(InternalMessage message) {
		exchange.execute(message);
	}

	public void destroy() {
		log.debug("cleanUp - Conversation");
		for (Member leaving : members) {
			for (Member member : members) {
				if(!leaving.getId().equals(member.getId())) {
					left.executeFor(leaving, member);
				}
			}
	        cache.removeMemberFromConversation(id, leaving.getName());
		}
	}

}

