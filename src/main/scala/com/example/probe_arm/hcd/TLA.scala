package com.example.probe_arm.hcd
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.ActorContext
import com.example.probe_arm.models.ProbeArmHcdMessage
import com.example.probe_arm.models.ProbeArmHcdMessage.Move
import csw.framework.CurrentStatePublisher
import csw.framework.scaladsl.ComponentHandlers
import csw.messages.TopLevelActorMessage
import csw.messages.commands.{CommandResponse, ControlCommand}
import csw.messages.framework.ComponentInfo
import csw.messages.location.TrackingEvent
import csw.messages.params.generics.KeyType.IntKey
import csw.services.alarm.api.scaladsl.AlarmService
import csw.services.command.CommandResponseManager
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
  var worker: ActorRef[ProbeArmHcdMessage] = _

  override def initialize(): Future[Unit] = {
    worker = ctx.spawnAnonymous(Worker.make(currentStatePublisher, componentInfo))
    Future.unit
  }

  override def onLocationTrackingEvent(trackingEvent: TrackingEvent): Unit = ???

  override def validateCommand(controlCommand: ControlCommand): CommandResponse = CommandResponse.Accepted(controlCommand.runId)

  override def onSubmit(controlCommand: ControlCommand): Unit = {
    val moveParam = controlCommand.paramType(IntKey.make("move"))
    worker ! Move(moveParam(0), moveParam(1))
  }

  override def onOneway(controlCommand: ControlCommand): Unit = ???

  override def onShutdown(): Future[Unit] = ???
  override def onGoOffline(): Unit        = ???
  override def onGoOnline(): Unit         = ???
}
