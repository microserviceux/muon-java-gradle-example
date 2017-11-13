package io.muoncore.example;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.rpc.server.RpcServer;

import java.util.Collections;
import java.util.Map;

import static io.muoncore.protocol.rpc.server.HandlerPredicates.path;


/**
 * Simple Muon server example with RPC and streaming endpoints.
 */
public class MuonServerExample {

    static Muon muon;

    public static void main(String[] args) throws Exception {

        muon = muonServer();

        Thread.sleep(2000);

        System.out.println("Use Ctrl-C to exit");
    }

    private static Muon muonServer() throws Exception {
        AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier("example-service")
//                .addWriter(conf -> {
//                    conf.getProperties().put("muon.discovery.factories", "io.muoncore.discovery.amqp.AmqpDiscoveryFactory");
//                    conf.getProperties().put("amqp.discovery.url", "amqp://muon:microservices@localhost");
//
//                    conf.getProperties().put("muon.transport.factories", "io.muoncore.transport.amqp.AmqpMuonTransportFactory");
//                    conf.getProperties().put("amqp.transport.url", "amqp://muon:microservices@localhost");
//
//                })
                .build();

        Muon muon = MuonBuilder.withConfig(config).build();

        RpcServer rpc = new RpcServer(muon);

        rpc.handleRequest(path("/"))
                .addResponseType(Map.class)
                .handler(request -> {
                    request.ok(Collections.singletonMap("message", "hello world"));
                })
                .build();

        return muon;
    }
}
