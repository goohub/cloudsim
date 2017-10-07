package org.cloudbus.cloudsim.util;

import org.cloudbus.cloudsim.power.cluster.ClusterHost;
import org.cloudbus.cloudsim.power.cluster.PowerVmAllocationPolicyMigrationDynamicThreshold;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by root on 5/4/17.
 */
public class UtilizationMonitor {

    private static boolean init = false;

    private static Map<Integer, Double> loadMap;
    private static Map<Integer, Double> balanceMap;
    private static double evaluateUpThreshold;
    private static double evaluateDownThreshold;

    static {
        loadMap = new HashMap<Integer, Double>();
        balanceMap = new HashMap<Integer, Double>();
    }

    public static void getLoadstatistic(List<ClusterHost> hostList) {
        if (init) {
            return;
        }
        for (ClusterHost host : hostList) {
            loadMap.put(host.getId(), host.getUtilization());
            balanceMap.put(host.getId(), host.getBalance());
        }
        init = true;
    }

    public static void evaluateLoad(PowerVmAllocationPolicyMigrationDynamicThreshold vmAllocationPolicy) {
        double average = 0.0, deviation = 0.0;
        List<Double> statistic = new ArrayList<Double>(loadMap.size());
        for (Map.Entry<Integer, Double> loadEnt : loadMap.entrySet()) {
            double value = Double.parseDouble(String.format("%.4f", loadEnt.getValue()));
            average += value;
            statistic.add(value);
        }
        average /= statistic.size();
        deviation = computeDeviation(statistic, average);

        double w = 0.8, u = 0.7, v = 1.4;

        double l_cap = average + w * Math.abs(0.7 - average);
        evaluateUpThreshold = l_cap + u * deviation;
        evaluateDownThreshold = l_cap - v * deviation;
        vmAllocationPolicy.setUtilizationUpThreshold(evaluateUpThreshold);
        vmAllocationPolicy.setUtilizationDownThreshold(evaluateDownThreshold);
    }

    private static double computeDeviation(List<Double> statistic, Double average) {
        double tmp = 0.0;
        for (Double value : statistic) {
            tmp += Math.pow(value - average, 2);
        }
        return Math.sqrt(tmp / statistic.size());
    }

    public static double computeAverage(List<Double> statistic) {
        double sum = 0.0;
        for (Double item : statistic) {
            sum += item;
        }
        return sum / statistic.size();
    }

    public static void printLoadstatistic() throws Exception {
        StringBuffer host_bf = new StringBuffer(),
                load_bf = new StringBuffer();
        List<Double> statistic = new ArrayList<Double>(loadMap.size());
        for (Map.Entry<Integer, Double> loadEnt : loadMap.entrySet()) {
            double value = Double.parseDouble(String.format("%.4f", loadEnt.getValue()));
            statistic.add(value);
            host_bf.append(loadEnt.getKey()).append(",");
            load_bf.append(value).append(",");
        }
        host_bf.setLength(host_bf.length() - 1);
        load_bf.setLength(load_bf.length() - 1);

        Collections.sort(statistic);
        System.out.println("\naverage Utilization:" + computeAverage(statistic));
        System.out.println("medium Utilization:" + statistic.get(statistic.size() / 2));

        String base_path = UtilizationMonitor.class.getClassLoader().getResource(".").getPath();

        OutputStream host_out = new FileOutputStream(new File(base_path + "host.txt")),
                load_out = new FileOutputStream(new File(base_path + "utilization.txt"));
        host_out.write(host_bf.toString().getBytes());
        load_out.write(load_bf.toString().getBytes());
        load_out.close();
        host_out.close();
    }

    public static void printbalancetatistic() throws Exception {

        StringBuffer host_bf = new StringBuffer(),
                load_bf = new StringBuffer();
        List<Double> statistic = new ArrayList<Double>(loadMap.size());
        for (Map.Entry<Integer, Double> balanceEnt : balanceMap.entrySet()) {
            double value = Double.parseDouble(String.format("%.4f", balanceEnt.getValue()));
            statistic.add(value);
            host_bf.append(balanceEnt.getKey()).append(",");
            load_bf.append(value).append(",");
        }
        host_bf.setLength(host_bf.length() - 1);
        load_bf.setLength(load_bf.length() - 1);

        Collections.sort(statistic);
        System.out.println("\naverage balance:" + computeAverage(statistic));
        System.out.println("medium balance:" + statistic.get(statistic.size() / 2));

        String base_path = UtilizationMonitor.class.getClassLoader().getResource(".").getPath();

        OutputStream host_out = new FileOutputStream(new File(base_path + "host.txt")),
                load_out = new FileOutputStream(new File(base_path + "balance.txt"));
        host_out.write(host_bf.toString().getBytes());
        load_out.write(load_bf.toString().getBytes());
        load_out.close();
        host_out.close();
    }

    public static void printevaluation(){
        System.out.println("evaluateUpThreshold:" + evaluateUpThreshold);
        System.out.println("evaluateDownThreshold:" + evaluateDownThreshold);
    }

}
