package io.muoncore.example;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.requestresponse.Response;


/**
 * Simple Muon example that acts as a client
 */
public class MuonClientExample {

    public static void main(String[] args) throws Exception {

        AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier("example-service-client").build();

        Muon muon = MuonBuilder.withConfig(config).build();
        muon.getDiscovery().blockUntilReady();

        Response<String> data = muon.request("request://example-service/hello", String.class).get();

        System.out.println("Example Service says: " + data.getPayload());

        muon.shutdown();
    }
}
