package com.example.probe_arm.hcd

import csw.framework.deploy.containercmd.ContainerCmd

object Main {
  def main(args: Array[String]): Unit = {
    ContainerCmd.start("ProbeArmHCD_Container", args)
  }
}
