package com.akka.test.supervision;

import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.japi.pf.DeciderBuilder;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import static akka.actor.SupervisorStrategy.*;

import java.util.concurrent.TimeUnit;

public class Supervisor extends UntypedActor {

	private static SupervisorStrategy strategy = new OneForOneStrategy(3, Duration.create("100 minute"),
			DeciderBuilder.match(ArithmeticException.class, e -> resume())
					.match(NullPointerException.class, e -> restart())
					.match(IllegalArgumentException.class, e -> restart()).matchAny(o -> escalate()).build());

	@Override
	public void preStart() {
		System.out.println("Creating child.");

		ActorRef child = getContext().actorOf(Props.create(ChildActor.class), "childActor");

		getContext().watch(child);
	}

	@Override
	public void onReceive(Object arg0) throws Exception {

		if (arg0 instanceof String) {

			if (arg0.equals("Start")) {
				System.out.println("Supervisor starting...");

				for (ActorRef child : getContext().getChildren()) {

					child.tell("Dance", getSelf());
				}
			} else if (arg0.equals("Give me Break")) {

				Runnable task = () -> {
					for (ActorRef child : getContext().getChildren()) {

						child.tell("Dance", getSelf());
					}
				};

				// Exhausted after 10 seconds.
				getContext().system().scheduler().scheduleOnce(new FiniteDuration(10, TimeUnit.SECONDS), task,
						getContext().system().dispatcher());

			}
		} else if (arg0 instanceof Terminated) {

			Terminated msg = ((Terminated) arg0);
			System.out.println("Terminated ." + msg.toString());
		}

	}

	@Override
	public SupervisorStrategy supervisorStrategy() {
		return strategy;
	}

}
