package org.cloudbus.cloudsim.alogrithm.clusterImpl.meta;

import org.cloudbus.cloudsim.alogrithm.cluster.Cluster;
import org.cloudbus.cloudsim.alogrithm.entity.Domain;
import org.cloudbus.cloudsim.alogrithm.entity.Item;

import java.util.*;

/**
 * Created by root on 8/11/17.
 */

public class Kmeans implements Cluster {

    private int k;//k个中心点
    private Double[][] centers;

    private List<Item> data;
    private int[] memberShip;

    private Map<Integer, Double[]> weights;
    private int[] elementsInCenters;
    private List<Domain> cluster;
    private double maxDistance;

    public Kmeans(int k) {
        this.k = k;
    }

    private void randomCenters() {
        int length = data.get(0).getAttr().length;
        centers = new Double[k][length];
        Random random = new Random();
        Map map = new HashMap();
        for (int i = 0; i < k; i++) {
            int index = Math.abs(random.nextInt()) % data.size();
            if (map.containsKey(index)) {
                i--;
            } else {
                //将中心点的下标存到MAP中，保证下次选出的中心点不是同一个
                map.put(index, data.get(index));
                //将中心点的下标存入centers[]中
                centers[i] = data.get(index).getAttr();
            }
        }
    }

    private void calMemberShip() {
        int length = data.size();

        memberShip = new int[length];
        elementsInCenters = new int[k];
        for (int j = 0; j < length; j++) {
            double currentDistance = Double.MAX_VALUE;
            int currentIndex = -1;
            Double[] item = data.get(j).getAttr();
            for (int i = 0; i < k; i++) {
                //中心点
                Double[] weight = weights.get(i);
                if (weight == null) {
                    weight = new Double[]{1.0, 1.0, 1.0};
                    weights.put(i, weight);
                }
                Double[] tempCentersValue = centers[i];
                double distance = this.manhattanDistince(item, tempCentersValue, weight);
                if (distance < currentDistance) {
                    currentDistance = distance;
                    currentIndex = i;
                }
            }
            memberShip[j] = currentIndex;
        }

        for (int i = 0; i < memberShip.length; i++) {
            elementsInCenters[memberShip[i]]++;
        }
    }

    private void calNewCenters() {
        int length = data.get(0).getAttr().length;
        centers = new Double[k][length];
        for (int i = 0; i < memberShip.length; i++) {
            for (int j = 0; j < length; j++) {
                if (centers[memberShip[i]][j] == null)
                    centers[memberShip[i]][j] = 0.0;
                centers[memberShip[i]][j] += data.get(i).getAttr()[j];
            }
        }

        for (int i = 0; i < centers.length; i++) {
            for (int j = 0; j < length; j++) {
                centers[i][j] /= elementsInCenters[i];
            }
        }
    }

    private double computeMaxDistance() {
        int length = data.size();
        Double[] weight = new Double[]{1.0, 1.0, 1.0};

        double totalDistance = 0;
        double[] coms = new double[k];
        for (int i = 0; i < length; i++) {

            totalDistance += manhattanDistince(data.get(i).getAttr(), centers[memberShip[i]], weight);
            coms[memberShip[i]] += manhattanDistince(data.get(i).getAttr(), centers[memberShip[i]], weight);
        }

        double opt = Double.MIN_VALUE;
        for (int i = 0; i < k; i++) {
            if (coms[i] > opt) opt = coms[i];
        }
        return opt / totalDistance;
    }

    //计算临近距离
    private double manhattanDistince(Double[] paraFirstData, Double[] paraSecondData, Double[] weight) {
        double tempDistince = 0;
        if ((paraFirstData != null && paraSecondData != null) && paraFirstData.length == paraSecondData.length) {
            for (int i = 0; i < paraFirstData.length; i++) {
                tempDistince += Math.pow(weight[i], 2) * Math.abs(paraFirstData[i] - paraSecondData[i]);
            }
        } else {
            System.out.println("firstData 与 secondData 数据结构不一致");
        }
        return tempDistince;
    }

    public void fit(List<Item> data) {
        this.data = data;
        String lastMembership = "";
        String nowMembership = "";
        if(centers == null){
            randomCenters();
        }
        System.out.println("随机选中的中心index为：" + Arrays.deepToString(centers));
        if (weights == null) {
            weights = new HashMap<Integer, Double[]>();
        }
        for (int i = 0; ; i++) {
            calMemberShip();
            nowMembership = Arrays.toString(memberShip);
            System.out.println("DATA聚类之后Membership为：" + nowMembership);
            System.out.println("Elements in centers cnt:" + Arrays.toString(elementsInCenters));
            calNewCenters();
            if (nowMembership.equals(lastMembership)) {
                System.out.println("聚类结束，共迭代了" + i + "次");
                System.out.println("新中心点为：" + Arrays.deepToString(centers));
//                maxDistance = computeMaxDistance();
//                System.out.println("totalDistance ： " + maxDistance);

                cluster = new ArrayList<Domain>();
                for (int j = 0; j < k; j++) {
                    cluster.add(new Domain());
                }
                for (int j = 0; j < memberShip.length; j++) {
                    Domain dom = cluster.get(memberShip[j]);
                    dom.getL().add(data.get(j));
                }
                break;
            } else lastMembership = nowMembership;
        }
    }

    public List<Domain> getCluster() {
        return cluster;
    }

    public Double[][] getCenters() {
        return centers;
    }

    public void setCenters(Double[][] centers) {
        this.centers = centers;
    }

    public void setWeights(Map<Integer, Double[]> weights) {
        this.weights = weights;
    }

    public double getMaxDistance() {
        return maxDistance;
    }
}
