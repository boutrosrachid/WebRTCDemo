package hu.alerant.signalingserver.domain;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import hu.alerant.signalingserver.cases.connection.ConnectionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class RTCConnections {
	private static Table<Member, Member, ConnectionContext> connections = HashBasedTable.create();

    @Autowired
    @Qualifier("WebRTCPingScheduler")
    private ScheduledExecutorService scheduler;

    @Value("${webrtc.max_connection_setup_time:30}")
    private int maxConnectionSetupTime;

    @PostConstruct
    void cleanOldConnections(){
        scheduler.scheduleWithFixedDelay(() -> {
            List<ConnectionContext> oldConnections = connections.values().stream()
                    .filter(context -> !context.isCurrent())
                    .collect(Collectors.toList());
            oldConnections.forEach(c -> connections.remove(c.getMaster(), c.getSlave()));

        }, maxConnectionSetupTime, maxConnectionSetupTime, TimeUnit.SECONDS);
    }

	public void put(Member from, Member to, ConnectionContext ctx) {
		connections.put(from, to, ctx);
		connections.put(to, from, ctx);
	}

	public ConnectionContext get(Member from, Member to) {
		return connections.get(from, to);
	}

}
