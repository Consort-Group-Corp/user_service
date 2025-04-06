package uz.consortgroup.user_service.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class WebClientConfig {
    @Value("${one-id.base-url}")
    private String baseUrl;

    @Value("${one-id.timeout:5000}")
    private int timeout;

    @Bean
    public WebClient webClientWithTimeout() {
        TcpClient tcpClient = TcpClient
                .create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
                .doOnConnected(connection -> {
                    connection.addHandlerLast(new ReadTimeoutHandler(timeout, TimeUnit.MILLISECONDS));
                    connection.addHandlerLast(new WriteTimeoutHandler(timeout, TimeUnit.MILLISECONDS));
                });

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .filter((request, next) -> {
                    log.info("Request: {} {}", request.method(), request.url());
                    return next.exchange(request)
                            .doOnNext(response -> log.info("Response status: {}", response.statusCode()));
                })
                .build();
    }
}
