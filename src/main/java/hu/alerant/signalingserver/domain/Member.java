package hu.alerant.signalingserver.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import hu.alerant.signalingserver.api.dto.WebRTCMember;

import javax.websocket.Session;
import java.util.concurrent.ScheduledFuture;

import static lombok.AccessLevel.PRIVATE;

@Data
@Builder(builderMethodName = "create")
public class Member implements WebRTCMember{

	private String id;
	private String name;
	private Session session;

	@Getter(PRIVATE)
	private ScheduledFuture<?> ping;

	private Member(String id, String name, Session session, ScheduledFuture<?> ping) {
		this.id = session.getId();
		this.session = session;
		this.ping = ping;
		this.name = this.id;
	}

	public void markLeft() {
		ping.cancel(true);
	}
	@Override
	public String toString() {
        return String.format("%s", id);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Member)) {
			return false;
		}
		Member m = (Member) o;
		return new EqualsBuilder()//
				.append(m.id, id)//
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()//
				.append(id)//
				.build();
	}

}
