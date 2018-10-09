package com.example.probe_arm.assembly
import akka.actor.typed.scaladsl.{Behaviors, MutableBehavior}
import akka.actor.typed.{ActorContext, Behavior}
import akka.util.Timeout
import com.example.probe_arm.models.ProbeArmAssemblyMessage
import com.example.probe_arm.models.ProbeArmAssemblyMessage.{CurrentStateContainer, DemandState, ProbeArmHcd, ProbeArmHcdRemoved}
import csw.messages.commands.{CommandName, Setup}
import csw.messages.framework.ComponentInfo
import csw.messages.params.generics.KeyType.IntKey
import csw.messages.params.states.{CurrentState, StateName}
import csw.services.command.scaladsl.CommandService

import scala.concurrent.duration.DurationDouble

class Worker(ctx: ActorContext[ProbeArmAssemblyMessage], componentInfo: ComponentInfo)
    extends MutableBehavior[ProbeArmAssemblyMessage] {

  var demandState: Option[DemandState]    = None
  var currentState: Option[CurrentState]  = None
  var probeArmHcd: Option[CommandService] = None

  override def onMessage(msg: ProbeArmAssemblyMessage): Behavior[ProbeArmAssemblyMessage] = {
    msg match {
      case demand: DemandState ⇒ demandState = Some(demand)
      case CurrentStateContainer(currentValue) ⇒
        currentState = Some(currentValue)
        for {
          demand      ← demandState
          probeArmHcd ← probeArmHcd
          currentParam = currentValue(IntKey.make("currentPosition"))
          x: Int       = demand.x - currentParam(0)
          y: Int       = demand.y - currentParam(1)
          param        = IntKey.make("move").set(x, y)
          setup        = Setup(componentInfo.prefix, CommandName("move"), None).add(param)
          _            = probeArmHcd.oneway(setup)(Timeout(5.seconds))
        } yield ()

      case ProbeArmHcd(hcd) ⇒
        probeArmHcd = Some(hcd)
        hcd.subscribeOnlyCurrentState(Set(StateName("currentPosition")), x ⇒ CurrentStateContainer(x))
      case ProbeArmHcdRemoved ⇒ probeArmHcd = None
    }
    this
  }
}

object Worker {
  def make(componentInfo: ComponentInfo): Behavior[ProbeArmAssemblyMessage] = Behaviors.setup(new Worker(_, componentInfo))
}
