package com.example
import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter.UntypedActorSystemOps

object Main {
  def main(args: Array[String]): Unit = {
    val actorSystem = ActorSystem("test")

//    actorSystem.spawn()
  }
}
