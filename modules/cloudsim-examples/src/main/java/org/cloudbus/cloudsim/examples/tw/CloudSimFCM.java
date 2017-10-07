package org.cloudbus.cloudsim.examples.tw;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.alogrithm.clusterImpl.FCM;
import org.cloudbus.cloudsim.alogrithm.clusterImpl.KMeans;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.power.PowerVmSelectionPolicyMinimumUtilization;
import org.cloudbus.cloudsim.power.cluster.ClusterHost;
import org.cloudbus.cloudsim.power.cluster.ClusterVm;
import org.cloudbus.cloudsim.power.cluster.PowerVmAllocationPolicyMigrationDynamicThreshold;
import org.cloudbus.cloudsim.power.models.PowerModelSqrt;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.util.ResponseTimeMonitor;
import org.cloudbus.cloudsim.util.UtilizationMonitor;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by root on 8/30/17.
 */
public class CloudSimFCM {

    /**
     * The cloudlet list.
     */
    private static List<Cloudlet> cloudletList;

    /**
     * The vmlist.
     */
    private static List<PowerVm> vmlist;

    /**
     * The hostlist.
     */
    private static List<Host> hostlist;

    /**
     * Creates main() to run this example
     */
    public static void main(String[] args) {

        Log.printLine("Starting CloudSimTW...");

        try {
            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            int num_user = 1;   // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

            // Second step: Create Datacenters
            //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
            hostlist = createHosts(ConstantsConf.NUMBER_HOSTS);

            Datacenter datacenter0 = createDatacenter("Datacenter_0");

            //Third step: Create Broker
            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            //Forth step: Create Instance
            //create VMs
            vmlist = createVms(brokerId, ConstantsConf.NUMBER_VMS);

            //create cloudlet
            cloudletList = createCloudlets(brokerId, ConstantsConf.NUMBER_CLOUDLETS);

            //submit vm list to the broker
            broker.submitVmList(vmlist);

            //Fifth step: Create two Cloudlets
            cloudletList = createCloudlets(brokerId, ConstantsConf.NUMBER_CLOUDLETS);

            //submit cloudlet list to the broker
            broker.submitCloudletList(cloudletList);

            //bind the cloudlets to the vms. This way, the broker
            // will submit the bound cloudlets only to the specific VM
            bindCloudletToVm(broker, vmlist, cloudletList);

            //train by tricluster
            FCM<PowerVm> fcm = new FCM<PowerVm>(3);
            fcm.fit(vmlist);
            fcm.adapt();

            // Sixth step: Starts the simulation
            CloudSim.startSimulation();

            // Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();

            PowerDatacenter pd = (PowerDatacenter)datacenter0;


            CloudSim.stopSimulation();

            printCloudletList(newList);

            Log.printLine("CloudSimTW finished!");

            Log.printLine("CloudSimTW total energy:"+ pd.getPower());

            ResponseTimeMonitor.computeRespTime(newList);

            UtilizationMonitor.printLoadstatistic();

            UtilizationMonitor.printbalancetatistic();


        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private static List<Host> createHosts(int hostsNumber) {

        List<Host> hostlist = new ArrayList<Host>();

        int lower = (int) Math.ceil(hostsNumber * 0.2);
        int high = (int) Math.ceil(hostsNumber * 0.8);
        for (int i = 0; i < hostsNumber; ++i) {
            int hostType = 1;
            if (i <= lower) {
                hostType = 0;
            } else if (i > high) {
                hostType = 2;
            }
            ArrayList<Pe> peList = new ArrayList<Pe>();
            for (int j = 0; j < ConstantsConf.HOST_PES[hostType]; ++j) {
                peList.add(new Pe(j,
                        new PeProvisionerSimple(ConstantsConf.HOST_MIPS[hostType])));
            }

            hostlist.add(
                    new ClusterHost(
                            i,
                            new RamProvisionerSimple(ConstantsConf.HOST_RAM[hostType]),
                            new BwProvisionerSimple(ConstantsConf.HOST_BW),
                            ConstantsConf.HOST_STORAGE,
                            peList,
                            new VmSchedulerTimeShared(peList),
                            new PowerModelSqrt(169,0.5)
                    )
            );
        }
        return hostlist;
    }

    private static List<PowerVm> createVms(int brokerId, int vmsNumber) {
        List<PowerVm> vmlist = new ArrayList<PowerVm>();
        Random random = new Random();

        for (int i = 0; i < ConstantsConf.NUMBER_VMS; i++) {
            int vmType = i / (int) Math.ceil((double) vmsNumber / 3.0D);

            double mips = ConstantsConf.VM_MIPS[vmType] * random.nextDouble();
            int ram = (int) (ConstantsConf.VM_RAM[vmType] * random.nextDouble());
            int bw = (int) (ConstantsConf.VM_BW * random.nextDouble());

            vmlist.add(
                    new ClusterVm(i, brokerId, mips, ConstantsConf.VM_PES[vmType], ram, bw, ConstantsConf.VM_SIZE, 0, "Xen", new CloudletSchedulerDynamicWorkload(mips, ConstantsConf.VM_PES[vmType]), ConstantsConf.SCHEDULING_INTERVAL)
            );
        }
        Collections.sort(vmlist, new Comparator<Vm>() {
            public int compare(Vm vm0, Vm vm1) {
                double dist0 = (int) (Math.pow(vm0.getMips() * vm0.getNumberOfPes(), 2) + Math.pow(vm0.getRam(), 2) + Math.pow(vm0.getBw(), 2));
                double dist1 = (int) (Math.pow(vm1.getMips() * vm1.getNumberOfPes(), 2) + Math.pow(vm1.getRam(), 2) + Math.pow(vm1.getBw(), 2));
                return dist0 > dist1 ? 1 : -1;
            }
        });
        return vmlist;
    }

    private static List<Cloudlet> createCloudlets(int brokerId, int numberCloudlets) {
        List<Cloudlet> cloudlets = new ArrayList<Cloudlet>();
        Random random = new Random();
        long fileSize = ConstantsConf.CLOUDLET_FILESIZE;
        UtilizationModel utilizationModel = new UtilizationModelFull();
        for (int i = 0; i < numberCloudlets; i++) {
            long outputSize = (long) (ConstantsConf.CLOUDLET_OUTPUTSIZE * random.nextDouble())  + 20L;
            Cloudlet cloudlet = new Cloudlet(i, ConstantsConf.CLOUDLET_LENGTH, ConstantsConf.CLOUDLET_PES, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(brokerId);
            cloudlets.add(cloudlet);
        }
        return cloudlets;
    }

    private static void bindCloudletToVm(DatacenterBroker broker, List<PowerVm> vmlist, List<Cloudlet> cloudletList) {
        long vmSize = vmlist.size();
        long cloudletLength = cloudletList.size();
        for (int i = 0; i < cloudletLength; i++) {
            broker.bindCloudletToVm(i, i);
        }
    }


    private static Datacenter createDatacenter(String name) {

        //  1.Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;        // the cost of using memory in this resource
        double costPerStorage = 0.001;    // the cost of using storage in this resource
        double costPerBw = 0.0;            // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();    //we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostlist, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        // 2. Finally, we need to create a PowerDatacenter object.
        Datacenter datacenter = null;
        try {
            datacenter = new PowerDatacenter(name, characteristics, new PowerVmAllocationPolicyMigrationDynamicThreshold(hostlist, new PowerVmSelectionPolicyMinimumUtilization()), storageList, ConstantsConf.SCHEDULING_INTERVAL);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    //We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
    //to the specific rules of the simulated scenario
    private static DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    /**
     * Prints the Cloudlet objects
     *
     * @param list list of Cloudlets
     */
    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
                        indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime()) +
                        indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }

    }
}
