package com.akka.test.supervision;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class Main {

	public static void main(String[] args) {

		final ActorSystem system = ActorSystem.create("supervision");

		ActorRef supervisior = system.actorOf(Props.create(Supervisor.class), "supervisor");

		supervisior.tell("Start", ActorRef.noSender());

	}

}
