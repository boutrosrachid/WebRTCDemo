package hu.alerant.signalingserver.api.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import hu.alerant.signalingserver.api.WebRTCEvents;

@Retention(RUNTIME)
@Target({ TYPE })
public @interface WebRTCEventListener {

	WebRTCEvents[] value() default {};
}
