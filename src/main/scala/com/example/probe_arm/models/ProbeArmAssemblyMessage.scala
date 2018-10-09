package com.example.probe_arm.models
import csw.messages.TMTSerializable
import csw.messages.params.states.CurrentState
import csw.services.command.scaladsl.CommandService

sealed trait ProbeArmAssemblyMessage extends TMTSerializable

object ProbeArmAssemblyMessage {
  case class DemandState(x: Int, y: Int)                       extends ProbeArmAssemblyMessage
  case class CurrentStateContainer(currentState: CurrentState) extends ProbeArmAssemblyMessage
  case class ProbeArmHcd(hcd: CommandService)                  extends ProbeArmAssemblyMessage
  case object ProbeArmHcdRemoved                               extends ProbeArmAssemblyMessage
}
