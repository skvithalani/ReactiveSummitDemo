package com.example.probe_arm.assembly

import csw.framework.deploy.containercmd.ContainerCmd

object Main {
  def main(args: Array[String]): Unit = {
    ContainerCmd.start("ProbeArmAssembly_Container", args)
  }
}
