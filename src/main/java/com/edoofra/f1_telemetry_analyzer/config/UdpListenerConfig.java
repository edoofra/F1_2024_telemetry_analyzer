package com.edoofra.f1_telemetry_analyzer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.ip.udp.UnicastReceivingChannelAdapter;
import org.springframework.messaging.MessageChannel;

@Configuration
public class UdpListenerConfig {

    @Value("${f1.telemetry.udp.port:20777}")
    private int udpPort;

    @Value("${f1.telemetry.udp.socket-buffer-size:8192}")
    private int socketBufferSize;
    
    @Value("${f1.telemetry.processing.async:true}")
    private boolean asyncProcessing;

    @Bean
    public MessageChannel udpInputChannel() {
        if (asyncProcessing) {
            return new QueueChannel(1000);
        }
        return new DirectChannel();
    }

    @Bean
    public MessageProducer udpMessageProducer() {
        UnicastReceivingChannelAdapter adapter = new UnicastReceivingChannelAdapter(udpPort);
        adapter.setOutputChannel(udpInputChannel());
        adapter.setSoTimeout(0); // No timeout for continuous listening
        adapter.setReceiveBufferSize(socketBufferSize);
        return adapter;
    }
}