package ru.bmstu.iu9;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.Query;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import javafx.util.Pair;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import akka.pattern.Patterns;

public class AkkaApp {

    private static final String LOCAL_HOST = "localhost";
    private static final int PORT = 8080;
    private static final String TEST_URL = "testUrl";
    private static final String COUNT = "count";
    private static final int MAP_ASYNC = 1;
    private static final int TIME_OUT = 5;

    private static Flow<HttpRequest, HttpResponse, NotUsed> createFLow(Http http, ActorSystem system,
                                                                       ActorMaterializer materializer, ActorRef actor){
        return Flow.of(HttpRequest.class).
                map(
                        (req) -> {
                            Query query = req.getUri().query();
                            String url = query.get(TEST_URL).get();
                            int count = Integer.parseInt(
                                    query.get(COUNT).get()
                            );
                            System.out.println(url + " " + count);
                            return new Pair<String, Integer>(url, count);
                        }
                ).
                mapAsync(MAP_ASYNC,
                        req -> {
                            CompletionStage<Object> completionStage = Patterns.ask(
                                    actor,
                                    new Message(req.getKey()),
                                    Duration.ofSeconds(TIME_OUT)
                            );
                            return completionStage.thenCompose(
                                    res -> {
                                        if((Integer) res >= 0) {
                                            return CompletableFuture.
                                                    completedFuture(new Pair<>(
                                                            req.getKey(),
                                                            (Integer) res
                                                    ));
                                        }
                                        Flow<
                                                Pair<
                                                        String,
                                                        Integer
                                                        >,
                                                Integer,
                                                NotUsed> flow = Flow.
                                                    <Pair<
                                                            String,
                                                            Integer>>
                                                            create().
                                                mapConcat(
                                                        pair -> new ArrayList<>(
                                                                Collections.
                                                                        nCopies(
                                                                                pair.getValue(),
                                                                                pair.getKey()
                                                                        ))
                                                ).mapAsync(req.getValue())
                                    }
                            )
                        }
                )
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Server is going to start");
        ActorSystem system = ActorSystem.create("routes");
        ActorRef actor = system.actorOf(Props.create(CacheActor.class));
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        final Flow<
                HttpRequest,
                HttpResponse,
                NotUsed
                > routeFlow = createFLow(http, system, materializer, actor);
        final CompletionStage<ServerBinding> bind = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost(LOCAL_HOST, PORT),
                materializer
        );
        System.out.println("Server is starting at http://" + LOCAL_HOST + ":" + PORT);
        System.in.read();
        bind.
                thenCompose(ServerBinding::unbind).
                thenAccept(unbound -> system.terminate());
    }

}
