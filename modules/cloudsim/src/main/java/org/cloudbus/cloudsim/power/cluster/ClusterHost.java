package org.cloudbus.cloudsim.power.cluster;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.List;

/**
 * Created by root on 9/6/17.
 */
public class ClusterHost extends PowerHostUtilizationHistory {

    private double mipsFactor;

    private double ramFactor;

    private double bwFactor;

    public double getUtilization() {
        double mipsUtilization = (getTotalMips() - getVmScheduler().getAvailableMips()) / getTotalMips();
        double ramUtilization = getUtilizationOfRam() / getRamProvisioner().getRam();
        double bwUtilization = getUtilizationOfBw() / getBwProvisioner().getBw();
        return mipsUtilization * mipsFactor + ramUtilization * ramFactor + bwUtilization * bwFactor;
    }

    public double getBalance() {
        double allocatedMips = getTotalMips() - getVmScheduler().getAvailableMips(), allocableMips = getTotalMips(),
                allocatedRam = getUtilizationOfRam(), allocableRam = getRamProvisioner().getRam(),
                allocatedBw = getUtilizationOfBw(), allocableBw = getBwProvisioner().getBw();
        double numerator = allocableMips * allocatedMips +
                allocableRam * allocatedRam +
                allocableBw * allocatedBw;
        double denominator = Math.sqrt(Math.pow(allocatedMips, 2) + Math.pow(allocatedRam, 2) + Math.pow(allocatedBw, 2)) *
                Math.sqrt(Math.pow(allocableMips, 2) + Math.pow(allocableRam, 2) + Math.pow(allocableBw, 2));
        if(denominator == 0){
            System.out.println(denominator);
        }
        return numerator / denominator;
    }

    /**
     * Instantiates a new host.
     *
     * @param id             the id
     * @param ramProvisioner the ram provisioner
     * @param bwProvisioner  the bw provisioner
     * @param storage        the storage
     * @param peList         the pe list
     * @param vmScheduler    the VM scheduler
     * @param powerModel
     */
    public ClusterHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner, long storage, List<? extends Pe> peList, VmScheduler vmScheduler, PowerModel powerModel) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel);
        this.mipsFactor = 0.7;
        this.ramFactor = 0.2;
        this.bwFactor = 0.1;
    }

    public double getMipsFactor() {
        return mipsFactor;
    }

    public double getRamFactor() {
        return ramFactor;
    }

    public double getBwFactor() {
        return bwFactor;
    }
}
