package ru.bmstu.iu9;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;

import java.util.HashMap;

public class CacheActor extends AbstractActor {
    private final HashMap<String, Integer> storage = new HashMap<>();

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.
                create().
                match(
                        Message.class,
                        m -> {
                            getSender().tell(
                                    storage.getOrDefault(m.getUrl(), -1),
                                    ActorRef.noSender()
                            );
                        }
                ).
                match(
                        StorageMessage.class,
                        m -> {
                            storage.putIfAbsent(
                                    m.getUrl(),
                                    m.getTime()
                            );
                        }
                ).
                build();
    }
}
