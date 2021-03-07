package com.cj.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

/**
 * OS util
 *
 * @author chenjie 2020-08-20
 */
public class OSUtil {
    private static Logger logger = LoggerFactory.getLogger(OSUtil.class);

    private static final SystemInfo SI = new SystemInfo();
    public static final String TWO_DECIMAL = "0.00";
    private static HardwareAbstractionLayer hal = SI.getHardware();
    private static OperatingSystem os = SI.getOperatingSystem();
    private static final int logicalProcessorCount= hal.getProcessor().getLogicalProcessorCount();
    private static final GlobalMemory memory = hal.getMemory();
    /**
     * get cpu usage
     *
     * @return
     */
    public static double cpuUsage() {
        SystemInfo si = new SystemInfo();
        CentralProcessor processor = hal.getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        double cpuUsage= processor.getSystemCpuLoadBetweenTicks(prevTicks)*100;
        DecimalFormat df = new DecimalFormat(TWO_DECIMAL);
        df.setRoundingMode(RoundingMode.HALF_UP);

        return Double.parseDouble(df.format(cpuUsage));
    }
    /**
     * get mem usage
     *
     * @return
     */
    public static double memUsage() {
        SystemInfo si = new SystemInfo();

        double memUsage= 100d * (memory.getTotal()-memory.getAvailable()) / memory.getTotal();
        DecimalFormat df = new DecimalFormat(TWO_DECIMAL);
        df.setRoundingMode(RoundingMode.HALF_UP);

        return Double.parseDouble(df.format(memUsage));
    }

    /**
     * get process cpu usage
     *
     * @return
     */
    public static double getProcessCpu(int pid) {
        OSProcess p = os.getProcess(pid);
        Util.sleep(1000);
        double cpuUsage = os.getProcess(pid).getProcessCpuLoadBetweenTicks(p);
        cpuUsage = cpuUsage * 100d / logicalProcessorCount;
        return cpuUsage;
    }
    /**
     * get process mem usage
     *
     * @return
     */
    public static double getProcessMem(int pid) {
        OSProcess p = os.getProcess(pid);
        double memUsage =100d * p.getResidentSetSize() / memory.getTotal();
        return memUsage;
    }
    /**
     * find process pid
     *
     * @return
     */
    public static int findProcessPid(String name) {
        List<OSProcess> processList = os.getProcesses();
        for (OSProcess item : processList){
            if(name.equals(item.getName())){
                return item.getProcessID();
            }
        }
        return -1;
    }

}
