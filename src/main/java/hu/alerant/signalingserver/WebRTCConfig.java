package hu.alerant.signalingserver;

import hu.alerant.signalingserver.api.WebRTCEventBus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean;

import lombok.extern.log4j.Log4j;

import java.util.concurrent.ScheduledExecutorService;

@Log4j
@Configuration
@ComponentScan(basePackageClasses = {WebRTCConfig.class})
public class WebRTCConfig {

    @Value("${webrtc.scheduler_size:10}")
    private int size;

    @Bean(name = "WebRTCEventBus")
    public WebRTCEventBus eventBus() {
        return new WebRTCEventBus();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        propertyPlaceholderConfigurer.setLocation(new ClassPathResource("webrtc.properties"));
        return propertyPlaceholderConfigurer;
    }

    @Bean(name = "WebRTCPingScheduler")
    public ScheduledExecutorService scheduler() {
        ScheduledExecutorFactoryBean factoryBean = new ScheduledExecutorFactoryBean();
        factoryBean.setThreadNamePrefix("WebRTCConfig");
        factoryBean.setPoolSize(size);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }
}
