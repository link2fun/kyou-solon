package com.github.link2fun.framework.web.domain;

import cn.hutool.core.io.FileUtil;
import cn.hutool.system.oshi.OshiUtil;
import com.github.link2fun.framework.web.domain.server.*;
import com.github.link2fun.support.utils.Arith;
import com.github.link2fun.support.utils.ip.IpUtils;
import lombok.Getter;
import lombok.Setter;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * 服务器相关信息
 *
 * @author ruoyi
 */
@Setter
@Getter
public class Server implements Serializable {
  private static final int OSHI_WAIT_SECOND = 1000;

  /**
   * CPU相关信息
   */
  private Cpu cpu = new Cpu();

  /**
   * 內存相关信息
   */
  private Mem mem = new Mem();

  /**
   * JVM相关信息
   */
  private Jvm jvm = new Jvm();

  /**
   * 服务器相关信息
   */
  private Sys sys = new Sys();

  /**
   * 磁盘相关信息
   */
  private List<SysFile> sysFiles = new LinkedList<SysFile>();

  public void copyTo() throws Exception {
    SystemInfo si = new SystemInfo();
    HardwareAbstractionLayer hal = si.getHardware();

    setCpuInfo(hal.getProcessor());

    final GlobalMemory memory = OshiUtil.getMemory();
    setMemInfo(memory);

    setSysInfo();

    setJvmInfo();

    setSysFiles(si.getOperatingSystem());
  }

  /**
   * 设置CPU信息
   */
  private void setCpuInfo(CentralProcessor processor) {
    // CPU信息
    long[] prevTicks = processor.getSystemCpuLoadTicks();
    Util.sleep(OSHI_WAIT_SECOND);
    long[] ticks = processor.getSystemCpuLoadTicks();
    long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
    long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
    long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
    long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
    long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
    long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
    long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
    long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
    long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
    cpu.setCpuNum(processor.getLogicalProcessorCount());
    cpu.setTotal(totalCpu);
    cpu.setSys(cSys);
    cpu.setUsed(user);
    cpu.setWait(iowait);
    cpu.setFree(idle);
  }

  /**
   * 设置内存信息
   */
  private void setMemInfo(GlobalMemory memory) {
    mem.setTotal(memory.getTotal());
    mem.setUsed(memory.getTotal() - memory.getAvailable());
    mem.setFree(memory.getAvailable());
  }

  /**
   * 设置服务器信息
   */
  private void setSysInfo() {
    Properties props = System.getProperties();
    sys.setComputerName(IpUtils.getHostName());
    sys.setComputerIp(IpUtils.getHostIp());
    sys.setOsName(props.getProperty("os.name"));
    sys.setOsArch(props.getProperty("os.arch"));
    sys.setUserDir(props.getProperty("user.dir"));
  }

  /**
   * 设置Java虚拟机
   */
  private void setJvmInfo() throws UnknownHostException {
    Properties props = System.getProperties();
    jvm.setTotal(Runtime.getRuntime().totalMemory());
    jvm.setMax(Runtime.getRuntime().maxMemory());
    jvm.setFree(Runtime.getRuntime().freeMemory());
    jvm.setVersion(props.getProperty("java.version"));
    jvm.setHome(props.getProperty("java.home"));
  }

  /**
   * 设置磁盘信息
   */
  private void setSysFiles(OperatingSystem os) {
    FileSystem fileSystem = os.getFileSystem();
    List<OSFileStore> fsArray = fileSystem.getFileStores();
    for (OSFileStore fs : fsArray) {
      long free = fs.getUsableSpace();
      long total = fs.getTotalSpace();
      long used = total - free;
      SysFile sysFile = new SysFile();
      sysFile.setDirName(fs.getMount());
      sysFile.setSysTypeName(fs.getType());
      sysFile.setTypeName(fs.getName());
      sysFile.setTotal(FileUtil.readableFileSize(total));
      sysFile.setFree(FileUtil.readableFileSize(free));
      sysFile.setUsed(FileUtil.readableFileSize(used));
      sysFile.setUsage(Arith.mul(Arith.div(used, total, 4), 100));
      sysFiles.add(sysFile);
    }
  }


}
