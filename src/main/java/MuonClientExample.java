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
import io.muoncore.protocol.requestresponse.Response;
import io.muoncore.transport.MuonTransport;
import io.muoncore.transport.ServiceCache;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;


/**
 * Simple Muon example that acts as a client
 */
public class MuonClientExample {

    public static void main(String[] args) throws Exception {

        Properties props = new Properties();
        props.load(MuonClientExample.class.getResourceAsStream("application.properties"));

        Muon muon = createMuon(props);
        muon.getDiscovery().blockUntilReady();

        Thread.sleep(4000);

        Response<String> data = muon.request("request://ExampleService/hello", String.class).get();

        System.out.println("Example Service says: " + data.getPayload());

        muon.shutdown();
    }

    private static Muon createMuon(Properties props) throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, IOException {

        String serviceName = props.getProperty("service.name") + "-client";
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
