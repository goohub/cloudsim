package org.cloudbus.cloudsim.power.cluster;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.alogrithm.cluster.Cluster;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyMigrationAbstract;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicy;

import java.util.List;

/**
 * Created by root on 9/3/17.
 */
public class PowerVmAllocationPolicyMigrationDynamicThreshold extends PowerVmAllocationPolicyMigrationAbstract {

    private double utilizationDownThreshold = 0.6;

    private double utilizationUpThreshold = 0.9;

    @Override
    protected boolean isHostOverUtilized(PowerHost host) {
//        addHistoryEntry(host, getUtilizationDownThreshold());
        addHistoryEntry(host, getUtilizationUpThreshold());
        double totalRequestedMips = 0;
        for (Vm vm : host.getVmList()) {
            totalRequestedMips += vm.getCurrentRequestedTotalMips();
        }
        double utilization = totalRequestedMips / host.getTotalMips();
        return utilization > getUtilizationUpThreshold();
    }

    private int doWeight(ClusterVm cv) {
        int i = 0;
        int index = -1;
        double weight = 0.0;
        double weightOpt = Double.MIN_VALUE;
        for (Host host : getHostList()) {
            if (!host.isSuitableForVm(cv)) continue;
            if (cv.getDomain() == "L") {
               /* weight = host.getAvailableMips() * cv.getMips() * cv.getNumberOfPes() +
                        host.getRamProvisioner().getAvailableRam() * cv.getRam() +
                        host.getBwProvisioner().getAvailableBw() * cv.getBw();*/
                double space_mips = (double) (host.getAvailableMips()) / host.getTotalMips();
                double space_ram = (double) host.getRamProvisioner().getAvailableRam() / host.getRam();
                double space_bw = (double) host.getBwProvisioner().getAvailableBw() / host.getBw();
                weight = space_mips * cv.getWeight()[0] + space_ram * cv.getWeight()[1] + space_bw * cv.getWeight()[2];

            } else if (cv.getDomain() == "M") {
                /*double assumeForMips = host.getTotalMips() - host.getAvailableMips() + cv.getMips() * cv.getNumberOfPes(),
                        assumeForRam = host.getRam() - host.getRamProvisioner().getAvailableRam() + cv.getRam(),
                        assumeForBw = host.getBw() - host.getBwProvisioner().getAvailableBw() + cv.getBw(),
                        maxAvailableMips = host.getTotalMips(),
                        maxAvailableRam = host.getRam(),
                        maxAvailableBw = host.getBw();

                double numerator = maxAvailableMips * assumeForMips +
                        maxAvailableRam * assumeForRam +
                        maxAvailableBw * assumeForBw;
                double denominator = Math.sqrt(Math.pow(assumeForMips, 2) + Math.pow(assumeForRam, 2) + Math.pow(assumeForBw, 2)) *
                        Math.sqrt(Math.pow(maxAvailableMips, 2) + Math.pow(maxAvailableRam, 2) + Math.pow(maxAvailableBw, 2));
                weight = numerator / denominator;*/

                ClusterHost ch = (ClusterHost) host;
                double availableMips = host.getAvailableMips(),
                        availableRam = host.getRamProvisioner().getAvailableRam(),
                        availableBw = host.getBwProvisioner().getAvailableBw();
                weight = availableMips * ch.getMipsFactor() + availableRam * ch.getRamFactor() + availableBw * ch.getBwFactor();

               /* double mips_percent = (double) cv.getMips() * cv.getNumberOfPes() / host.getTotalMips();
                double ram_percent = (double) cv.getRam() / host.getRam();
                weight = mips_percent >= ram_percent ? host.getAvailableMips() / host.getTotalMips() : host.getRamProvisioner().getAvailableRam() / host.getRam();*/
            }
            if (weight > weightOpt) {
                weightOpt = weight;
                index = i;
            }
            i++;
        }
        return index;
    }

    @Override
    public PowerHost findHostForVm(Vm vm) {
        ClusterVm cv;
        if (vm instanceof ClusterVm) {
            cv = (ClusterVm) vm;
        } else {
            return null;
        }

        int index = doWeight(cv);
        if (index == -1) {
            return null;
        }

        return (PowerHost) getHostList().get(index);
    }

    /**
     * Instantiates a new power vm allocation policy migration abstract.
     *
     * @param hostList          the host list
     * @param vmSelectionPolicy the vm selection policy
     */
    public PowerVmAllocationPolicyMigrationDynamicThreshold(List<? extends Host> hostList, PowerVmSelectionPolicy vmSelectionPolicy) {
        super(hostList, vmSelectionPolicy);
    }

    public double getUtilizationDownThreshold() {
        return utilizationDownThreshold;
    }

    public void setUtilizationDownThreshold(double utilizationDownThreshold) {
        this.utilizationDownThreshold = utilizationDownThreshold;
    }

    public double getUtilizationUpThreshold() {
        return utilizationUpThreshold;
    }

    public void setUtilizationUpThreshold(double utilizationUpThreshold) {
        this.utilizationUpThreshold = utilizationUpThreshold;
    }

}
