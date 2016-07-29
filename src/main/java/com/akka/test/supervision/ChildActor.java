package com.akka.test.supervision;

import java.util.concurrent.TimeUnit;

import akka.actor.UntypedActor;
import scala.Option;
import scala.concurrent.duration.FiniteDuration;

public class ChildActor extends UntypedActor {

	@Override
	public void preStart() throws Exception {
		System.out.println("preStart");
	};

	@Override
	public void postStop() throws Exception {
		System.out.println("postStop");
	};

	@Override
	public void preRestart(Throwable reason, Option<Object> message) throws Exception {
		System.out.println("preRestart resonn=" + reason.getMessage());
	}

	@Override
	public void postRestart(Throwable th) throws Exception {
		System.out.println("postRestart. error=" + th.getMessage());

		getContext().parent().tell("Ready", getSelf());
	};

	@Override
	public void onReceive(Object arg0) throws Exception {

		if (arg0 instanceof String) {

			if (arg0.equals("Dance")) {

				System.out.println("Dancing...");

				Runnable task = () -> {
					getSelf().tell("Exhausted", getSelf());
				};

				// Exhausted after 10 seconds.
				getContext().system().scheduler().scheduleOnce(new FiniteDuration(10, TimeUnit.SECONDS), task,
						getContext().system().dispatcher());

			} else if (arg0.equals("Exhausted")) {
				System.out.println("Exhausted");
				getContext().parent().tell("Give me Break", getSelf());
				throw new IllegalArgumentException("Exhausted");
			}
		}
	}
}
