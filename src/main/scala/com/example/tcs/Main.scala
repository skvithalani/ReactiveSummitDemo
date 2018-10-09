package com.example.tcs
import csw.framework.deploy.containercmd.ContainerCmd

object Main {
  def main(args: Array[String]): Unit = {
    ContainerCmd.start("TCS_Container", args)
  }
}
