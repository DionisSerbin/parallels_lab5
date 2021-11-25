package ru.bmstu.iu9;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;

import java.io.IOException;

public class AkkaApp {

    private static Flow<HttpRequest, HttpResponse, NotUsed> createFLow(Http http, ActorSystem system,
                                                                       ActorMaterializer materializer, ActorRef actor){

    }

    public static void main(String[] args) throws IOException {
        System.out.println("Server is starting");
        ActorSystem system = ActorSystem.create("routes");
        ActorRef actor = system.actorOf(Props.create(CacheActor.class));
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        final Flow<
                HttpRequest,
                HttpResponse,
                NotUsed
                > routeFlow = createFLow(http, system, materializer, actor);
    }



}
