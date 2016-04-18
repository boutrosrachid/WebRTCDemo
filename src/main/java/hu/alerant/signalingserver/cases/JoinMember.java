package hu.alerant.signalingserver.cases;

import hu.alerant.signalingserver.domain.InternalMessage;
import hu.alerant.signalingserver.domain.Member;
import hu.alerant.signalingserver.domain.Signal;
import org.springframework.stereotype.Component;

@Component
public class JoinMember {

	public void sendMessageToJoining(Member sender, String id) {
		InternalMessage.create()//
				.to(sender)//
				.content(id)//
				.signal(Signal.JOINED)//
				.build()//
				.post();
	}

	public void sendMessageToOthers(Member sender, Member member) {
		InternalMessage.create()//
				.from(sender)//
				.to(member)//
				.signal(Signal.JOINED)//
				.build()//
				.post();
	}

	public void sendMessageToFirstJoined(Member sender, String id) {
		InternalMessage.create()//
				.to(sender)//
				.signal(Signal.CREATED)//
				.content(id)//
				.build()//
				.post();
	}

}
