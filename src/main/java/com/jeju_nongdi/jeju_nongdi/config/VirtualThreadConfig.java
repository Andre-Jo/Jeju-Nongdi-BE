package com.jeju_nongdi.jeju_nongdi.config;

import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class VirtualThreadConfig {

    /**
     * Virtual Thread 기반 기본 Executor
     */
    @Bean(name = "virtualThreadExecutor")
    public Executor virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Spring의 기본 TaskExecutor를 Virtual Thread로 교체
     */
    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public AsyncTaskExecutor applicationTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

    /**
     * Tomcat의 요청 Virtual Thread로처리함
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatVirtualThreadCustomizer() {
        return factory -> factory.addProtocolHandlerCustomizers(
                (TomcatProtocolHandlerCustomizer<?>) protocolHandler ->
                        protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor())
        );
    }

    /**
     * @Async 메서드를 위한 Virtual Thread Executor
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
