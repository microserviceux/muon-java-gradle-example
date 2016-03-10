package io.muoncore.example;

import io.muoncore.Muon;
import io.muoncore.MuonBuilder;
import io.muoncore.config.AutoConfiguration;
import io.muoncore.config.MuonConfigBuilder;
import reactor.rx.broadcast.Broadcaster;

import java.util.HashMap;
import java.util.Map;

import static io.muoncore.protocol.requestresponse.server.HandlerPredicates.all;


/**
 * Simple Muon server example with RPC and streaming endpoints.
 */
public class MuonServerExample {


    public static void main(String[] args) throws Exception {

        muonServer();

        Thread.sleep(2000);

        System.out.println("Use Ctrl-C to exit");
    }

    private static Muon muonServer() throws Exception {
        AutoConfiguration config = MuonConfigBuilder.withServiceIdentifier("example-service").build();

        Muon muon = MuonBuilder.withConfig(config).build();

        muon.handleRequest(all(), Map.class, request -> {
            System.out.println("A request has been made ");
            Map data = new HashMap();
            data.put("Hello", "world " + System.currentTimeMillis());
            request.ok(data);
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
}
