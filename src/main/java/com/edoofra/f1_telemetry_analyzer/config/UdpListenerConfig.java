package com.edoofra.f1_telemetry_analyzer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.ip.udp.UnicastReceivingChannelAdapter;
import org.springframework.messaging.MessageChannel;

@Configuration
public class UdpListenerConfig {

    @Value("${f1.telemetry.udp.port:20777}")
    private int udpPort;

    @Value("${f1.telemetry.udp.buffer-size:2048}")
    private int bufferSize;

    @Bean
    public MessageChannel udpInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer udpMessageProducer() {
        UnicastReceivingChannelAdapter adapter = new UnicastReceivingChannelAdapter(udpPort);
        adapter.setOutputChannel(udpInputChannel());
        adapter.setSoTimeout(0); // No timeout for continuous listening
        adapter.setReceiveBufferSize(bufferSize);
        return adapter;
    }
}