package io.muoncore.example;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import io.muoncore.protocol.rpc.Response;
import io.muoncore.protocol.rpc.client.RpcClient;

import java.util.HashMap;
import java.util.Map;


/**
 * Simple Muon example that acts as a client
 */
public class MuonClientExample {

    public static void main(String[] args) throws Exception {

        AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier("example-service-client").build();

        Muon muon = MuonBuilder.withConfig(config).build();
        RpcClient client = new RpcClient(muon);
        muon.getDiscovery().blockUntilReady();

        Map setting = new HashMap<>();
        setting.put("key", "hello");
        setting.put("value", 1234);

        Response data = client.request("request://example-service/", setting).get();

        System.out.println("Example Service says: " + data.getPayload(String.class));

        muon.shutdown();
    }
}
