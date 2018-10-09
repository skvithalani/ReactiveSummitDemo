package com.example.probe_arm.assembly
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import com.example.probe_arm.models.ProbeArmAssemblyMessage
import com.example.probe_arm.models.ProbeArmAssemblyMessage.{DemandState, ProbeArmHcd}
import csw.framework.CurrentStatePublisher
import csw.framework.scaladsl.ComponentHandlers
import csw.messages.TopLevelActorMessage
import csw.messages.commands.{CommandResponse, ControlCommand}
import csw.messages.framework.ComponentInfo
import csw.messages.location.{AkkaLocation, LocationRemoved, LocationUpdated, TrackingEvent}
import csw.messages.params.generics.KeyType.IntKey
import csw.messages.params.generics.{Key, KeyType}
import csw.services.alarm.api.scaladsl.AlarmService
import csw.services.command.CommandResponseManager
import csw.services.command.scaladsl.CommandService
import csw.services.event.api.scaladsl.EventService
import csw.services.location.scaladsl.LocationService
import csw.services.logging.scaladsl.LoggerFactory

import scala.concurrent.Future

class TLA(ctx: ActorContext[TopLevelActorMessage],
          componentInfo: ComponentInfo,
          commandResponseManager: CommandResponseManager,
          currentStatePublisher: CurrentStatePublisher,
          locationService: LocationService,
          eventService: EventService,
          alarmService: AlarmService,
          loggerFactory: LoggerFactory)
    extends ComponentHandlers(
      ctx,
      componentInfo,
      commandResponseManager,
      currentStatePublisher,
      locationService,
      eventService,
      alarmService,
      loggerFactory
    ) {

  var probeArmHcd: Option[CommandService]       = None
  var worker: ActorRef[ProbeArmAssemblyMessage] = _

  override def initialize(): Future[Unit] = {
    worker = ctx.spawnAnonymous(Worker.make(componentInfo))
    Future.unit
  }

  override def onLocationTrackingEvent(trackingEvent: TrackingEvent): Unit =
    trackingEvent match {
      case LocationUpdated(hcdLocation: AkkaLocation) ⇒
        val commandService = new CommandService(hcdLocation)(ctx.system)
        probeArmHcd = Some(commandService)
        worker ! ProbeArmHcd(commandService)
      case LocationRemoved(connection) ⇒ probeArmHcd = None
      case _                           ⇒
    }

  override def validateCommand(controlCommand: ControlCommand): CommandResponse = CommandResponse.Accepted(controlCommand.runId)

  override def onSubmit(controlCommand: ControlCommand): Unit = if (probeArmHcd.isDefined) {
    val demand = controlCommand.paramType(IntKey.make("demand"))
    worker ! DemandState(demand(0), demand(1))
  }

  override def onOneway(controlCommand: ControlCommand): Unit = ???
  override def onShutdown(): Future[Unit]                     = ???
  override def onGoOffline(): Unit                            = ???
  override def onGoOnline(): Unit                             = ???
}
