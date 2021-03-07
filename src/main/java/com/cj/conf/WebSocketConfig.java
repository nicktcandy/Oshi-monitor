package com.cj.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
/**
 * @author chenjie 2020-04-11 20:56:31
 */
@Configuration
public class WebSocketConfig {
    /**
     * ServerEndpointExporter
     *
     * use @ServerEndpoint to declare websocket endpoint
     *
     * @return
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}