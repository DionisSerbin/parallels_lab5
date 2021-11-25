package ru.bmstu.iu9;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.io.IOException;

public class AkkaApp {

    public static void main(String[] args) throws IOException {
        System.out.println("Server is starting");
        ActorSystem system = ActorSystem.create("routes");
        ActorRef actorRef = system.actorOf(Props.create())
    }

}
