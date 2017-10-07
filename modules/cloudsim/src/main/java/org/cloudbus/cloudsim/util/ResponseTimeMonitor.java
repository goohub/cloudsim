package org.cloudbus.cloudsim.util;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.power.cluster.ClusterHost;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by root on 5/4/17.
 */
public class ResponseTimeMonitor {

    private static boolean init = false;

    private static List<Double> respTime;

    public static double computeAverage(List<Double> statistic){
        double sum = 0.0;
        for(Double item:statistic){
            sum+=item;
        }
        return sum / statistic.size();
    }

    public static void computeRespTime(List<Cloudlet> cloudlets) {
        respTime = new ArrayList<Double>(cloudlets.size());
        for(Cloudlet cloudlet:cloudlets){
            respTime.add(cloudlet.getFinishTime()-cloudlet.getExecStartTime());
        }
        Collections.sort(respTime);
        System.out.println("\naverage responseTime:" + computeAverage(respTime));
        System.out.println("\nmedium responseTime:" + respTime.get(respTime.size()/2));
    }
}
