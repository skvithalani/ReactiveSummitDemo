package com.example.probe_arm.hcd
import akka.actor.typed.{ActorContext, Behavior}
import akka.actor.typed.scaladsl.{Behaviors, MutableBehavior, TimerScheduler}
import com.example.probe_arm.models.ProbeArmHcdMessage
import com.example.probe_arm.models.ProbeArmHcdMessage.{Move, Tick}
import csw.framework.CurrentStatePublisher
import csw.messages.framework.ComponentInfo
import csw.messages.params.generics.KeyType.IntKey
import csw.messages.params.states.{CurrentState, StateName}

import scala.concurrent.duration.DurationDouble

class Worker(ctx: ActorContext[ProbeArmHcdMessage],
             timer: TimerScheduler[ProbeArmHcdMessage],
             currentStatePublisher: CurrentStatePublisher,
             componentInfo: ComponentInfo)
    extends MutableBehavior[ProbeArmHcdMessage] {

  timer.startPeriodicTimer("publish", Tick, 1000.millis)

  var moveX    = 0
  var moveY    = 0
  var currentX = 0
  var currentY = 0

  override def onMessage(msg: ProbeArmHcdMessage): Behavior[ProbeArmHcdMessage] = {
    msg match {
      case Tick ⇒
        currentX = moveX
        currentY = moveY
        val currentPositionParam = IntKey.make("currentPosition").set(currentX, currentY)
        currentStatePublisher.publish(CurrentState(componentInfo.prefix, StateName("currentPosition")).add(currentPositionParam))
      case Move(x, y) ⇒
        moveX = x
        moveY = y
    }
    this
  }
}

object Worker {
  def make(currentStatePublisher: CurrentStatePublisher, componentInfo: ComponentInfo): Behavior[ProbeArmHcdMessage] =
    Behaviors.setup { ctx ⇒
      Behaviors.withTimers(new Worker(ctx, _, currentStatePublisher, componentInfo))
    }
}
