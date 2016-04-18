package hu.alerant.signalingserver.cases;

import hu.alerant.signalingserver.domain.Member;
import hu.alerant.signalingserver.domain.PingTask;
import hu.alerant.signalingserver.repository.Members;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Component
public class RegisterMember {

    @Value("${webrtc.ping_period:3}")
	private int period;

	@Autowired
	private Members members;

	@Autowired
	@Qualifier("WebRTCPingScheduler")
	private ScheduledExecutorService scheduler;

	public void incoming(Session session) {
		members.register(Member.create()//
				.session(session)//
				.ping(ping(session))//
				.build());
	}

	private ScheduledFuture<?> ping(Session session) {
		return scheduler.scheduleAtFixedRate(new PingTask(session), period, period, TimeUnit.SECONDS);
	}

}
