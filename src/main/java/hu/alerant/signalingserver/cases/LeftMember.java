package hu.alerant.signalingserver.cases;

import hu.alerant.signalingserver.domain.InternalMessage;
import hu.alerant.signalingserver.domain.Member;
import hu.alerant.signalingserver.domain.Signal;
import org.springframework.stereotype.Component;

@Component
public class LeftMember {

	public void executeFor(Member leaving, Member recipien) {
		InternalMessage.create()//
				.from(leaving)//
				.to(recipien)//
				.signal(Signal.LEFT)//
				.build()//
				.post();
	}

}
