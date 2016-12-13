package io.muoncore.example;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.requestresponse.Response;

import java.util.HashMap;
import java.util.Map;


/**
 * Simple Muon example that acts as a client
 */
public class MuonClientExample {

    public static void main(String[] args) throws Exception {

        AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier("example-service-client").build();

        Muon muon = MuonBuilder.withConfig(config).build();
        muon.getDiscovery().blockUntilReady();

        Map setting = new HashMap<>();
        setting.put("key", "hello");
        setting.put("value", 1234);

        Response data = muon.request("request://example-service/set", setting).get();

        System.out.println("Example Service says: " + data.getPayload(String.class));

        muon.shutdown();
    }
}
