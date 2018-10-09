package com.example.tcs
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors, MutableBehavior, TimerScheduler}
import akka.util.Timeout
import com.example.tcs.models.TcsAssemblyMessage
import com.example.tcs.models.TcsAssemblyMessage.{ProbeArmAssembly, ProbeArmAssemblyRemoved, Tick}
import csw.messages.commands.{CommandName, Setup}
import csw.messages.framework.ComponentInfo
import csw.messages.params.generics.KeyType.IntKey
import csw.services.command.scaladsl.CommandService

import scala.concurrent.duration.DurationDouble

class Worker(ctx: ActorContext[TcsAssemblyMessage], componentInfo: ComponentInfo, timer: TimerScheduler[TcsAssemblyMessage])
    extends MutableBehavior[TcsAssemblyMessage] {

  implicit val timeout: Timeout = Timeout(5.seconds)

  var x                                        = 0
  var y                                        = 0
  var probeArmAssembly: Option[CommandService] = None

  override def onMessage(msg: TcsAssemblyMessage): Behavior[TcsAssemblyMessage] = {
    msg match {
      case Tick ⇒
        x = x + 10
        y = y + 25
        val demandParam = IntKey.make("demand").set(x, y)
        val setup       = Setup(componentInfo.prefix, CommandName("DemandState"), None).add(demandParam)
        probeArmAssembly.foreach(_.submit(setup))
      case ProbeArmAssembly(assembly) ⇒
        probeArmAssembly = Some(assembly)
        timer.startPeriodicTimer("key", Tick, 1000.millis)
      case ProbeArmAssemblyRemoved ⇒ timer.cancel("key")
    }
    this
  }
}

object Worker {
  def make(componentInfo: ComponentInfo): Behavior[TcsAssemblyMessage] =
    Behaviors.setup(ctx ⇒ {
      Behaviors.withTimers(new Worker(ctx, componentInfo, _))
    })
}
