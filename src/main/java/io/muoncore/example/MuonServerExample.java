package io.muoncore.example;

import io.muoncore.Discovery;
import io.muoncore.Muon;
import io.muoncore.SingleTransportMuon;
import io.muoncore.codec.Codecs;
import io.muoncore.codec.json.JsonOnlyCodecs;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.extension.amqp.*;
import io.muoncore.extension.amqp.discovery.AmqpDiscovery;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09ClientAmqpConnection;
import io.muoncore.extension.amqp.rabbitmq09.RabbitMq09QueueListenerFactory;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.ServiceCache;
import reactor.rx.broadcast.Broadcaster;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all;


/**
 * Simple Muon server example with RPC and streaming endpoints.
 */
public class MuonServerExample {


    public static void main(String[] args) throws Exception {

        Properties props = new Properties();
        props.load(MuonServerExample.class.getResourceAsStream("/application.properties"));

        muonServer(props);

        Thread.sleep(2000);

        System.out.println("Use Ctrl-C to exit");
    }

    private static Muon muonServer(Properties props) throws Exception {

        Muon muon = createMuon(props);

        muon.handleRequest(all(), Map.class, request -> {
            System.out.println("A request has been made ");
            request.ok("The answer is yes");
        });

//        Broadcaster<String> stream = Broadcaster.create();
//        muon.publishSource("mySource", HOT, stream);
//
//        publishDataEvery5Seconds(stream);

        return muon;
    }

    private static void publishDataEvery5Seconds(Broadcaster<String> broadcaster) {

        new Thread(() -> {

            while(true) {
                try {
                    Thread.sleep(5000);

                    broadcaster.accept("Hello World");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    private static Muon createMuon(Properties props) throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, IOException {

        String serviceName = props.getProperty("service.name");
        String amqpUrl = props.getProperty("amqp.url");

        Discovery discovery = createDiscovery(amqpUrl);
        MuonTransport transport = createTransport(serviceName, amqpUrl);

        AutoConfiguration config = new AutoConfiguration();
        config.setServiceName(serviceName);
        config.setAesEncryptionKey("abcde12345678906");

        return new SingleTransportMuon(config, discovery, transport);
    }

    private static MuonTransport createTransport(String serviceName, String amqpUrl) throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, IOException {
        AmqpConnection connection = new RabbitMq09ClientAmqpConnection(amqpUrl);
        QueueListenerFactory queueFactory = new RabbitMq09QueueListenerFactory(connection.getChannel());

        ServiceQueue serviceQueue = new DefaultServiceQueue(serviceName, connection);
        AmqpChannelFactory channelFactory = new DefaultAmqpChannelFactory(serviceName, queueFactory, connection);

        return new AMQPMuonTransport(amqpUrl, serviceQueue, channelFactory);
    }

    private static Discovery createDiscovery(String amqpUrl) throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, IOException {
        AmqpConnection connection = new RabbitMq09ClientAmqpConnection(amqpUrl);
        QueueListenerFactory queueFactory = new RabbitMq09QueueListenerFactory(connection.getChannel());

        Codecs codecs = new JsonOnlyCodecs();

        AmqpDiscovery discovery = new AmqpDiscovery(queueFactory, connection, new ServiceCache(), codecs);
        discovery.start();
        return discovery;
    }
}
