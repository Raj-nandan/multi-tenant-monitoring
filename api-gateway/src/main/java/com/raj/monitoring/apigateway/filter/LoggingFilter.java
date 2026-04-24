package com.raj.monitoring.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class LoggingFilter {

    @Bean
    public GlobalFilter customGlobalFilter() {
        return (exchange, chain) -> {
            log.info("Incoming Request: {} {}",
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI());

            return chain.filter(exchange)
                    .then(Mono.fromRunnable(() ->
                            log.info("Outgoing Response: {}",
                                    exchange.getResponse().getStatusCode())));
        };
    }
}