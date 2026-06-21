package com.pawmatch.config;

import com.pawmatch.security.JwtTokenUtil;
import com.pawmatch.security.PawMatchPrincipal;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtTokenUtil jwtTokenUtil;

    public WebSocketConfig(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单消息代理，客户端订阅 /queue 和 /topic 前缀的目的地
        registry.enableSimpleBroker("/queue", "/topic");
        // 客户端发送消息的前缀
        registry.setApplicationDestinationPrefixes("/app");
        // 用户私有频道前缀，/user 会被解析为当前认证用户
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // 从 STOMP CONNECT 帧的登录头中获取 token
                    // 前端通过 connectHeaders: { login: token } 传递
                    String token = accessor.getLogin();
                    if (token != null && jwtTokenUtil.validateToken(token)) {
                        Long userId = jwtTokenUtil.getUserId(token);
                        Integer userType = jwtTokenUtil.getUserType(token);
                        Principal principal = new PawMatchPrincipal(userId, userType);
                        accessor.setUser(principal);
                    }
                }
                return message;
            }
        });
    }
}
