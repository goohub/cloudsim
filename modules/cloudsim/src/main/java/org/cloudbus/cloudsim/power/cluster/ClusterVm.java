package org.cloudbus.cloudsim.power.cluster;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.power.PowerVm;

/**
 * Created by root on 8/30/17.
 */
public class ClusterVm extends PowerVm {

    private String domain;
    private Double[] weight;
    private Double[] attr;

    /**
     * Instantiates a new power vm.
     *
     * @param id                 the id
     * @param userId             the user id
     * @param mips               the mips
     * @param pesNumber          the pes number
     * @param ram                the ram
     * @param bw                 the bw
     * @param size               the size
     * @param priority           the priority
     * @param vmm                the vmm
     * @param cloudletScheduler  the cloudlet scheduler
     * @param schedulingInterval the scheduling interval
     */
    public ClusterVm(int id, int userId, double mips, int pesNumber, int ram, long bw, long size, int priority, String vmm, CloudletScheduler cloudletScheduler, double schedulingInterval) {
        super(id, userId, mips, pesNumber, ram, bw, size, priority, vmm, cloudletScheduler, schedulingInterval);
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Double[] getWeight() {
        return weight;
    }

    public void setWeight(Double[] weight) {
        this.weight = weight;
    }

    public Double[] getAttr() {
        return attr;
    }

    public void setAttr(Double[] attr) {
        this.attr = attr;
    }

}
