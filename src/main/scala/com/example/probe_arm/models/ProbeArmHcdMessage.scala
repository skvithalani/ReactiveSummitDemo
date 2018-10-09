package com.example.probe_arm.models

sealed trait ProbeArmHcdMessage

object ProbeArmHcdMessage {
  case class Move(x: Int, y: Int) extends ProbeArmHcdMessage
  case object Tick                extends ProbeArmHcdMessage
}
