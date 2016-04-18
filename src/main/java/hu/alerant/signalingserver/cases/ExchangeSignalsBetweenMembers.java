package hu.alerant.signalingserver.cases;

import hu.alerant.signalingserver.cases.connection.ConnectionContext;
import hu.alerant.signalingserver.domain.InternalMessage;
import hu.alerant.signalingserver.domain.Member;
import hu.alerant.signalingserver.domain.RTCConnections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ExchangeSignalsBetweenMembers {

	@Autowired
	private RTCConnections connections;

	@Autowired
	private ApplicationContext context;

	public synchronized void begin(Member from, Member to) {
		connections.put(from, to, context.getBean(ConnectionContext.class, from, to));
		connections.get(from, to).begin();
	}

	public synchronized void execute(InternalMessage message) {
		connections.get(message.getFrom(), message.getTo()).process(message);
	}
}
