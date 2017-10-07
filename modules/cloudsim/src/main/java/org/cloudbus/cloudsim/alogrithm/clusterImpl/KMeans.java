package org.cloudbus.cloudsim.alogrithm.clusterImpl;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.alogrithm.clusterImpl.meta.Kmeans;
import org.cloudbus.cloudsim.alogrithm.entity.Domain;
import org.cloudbus.cloudsim.alogrithm.entity.Item;
import org.cloudbus.cloudsim.alogrithm.entity.Result;
import org.cloudbus.cloudsim.power.cluster.ClusterVm;

import java.util.*;

/**
 * Created by root on 9/6/17.
 */
public class KMeans<T> {

    private int k;

    private List<T> raw;
    private List<Item> data;
    private Result result;

    private Map<Integer, Double[]> setWeights() {
        Map<Integer, Double[]> weights = new HashMap<Integer, Double[]>();
        weights.put(0, new Double[]{0.6, 0.2, 0.2});
        weights.put(1, new Double[]{0.2, 0.6, 0.2});
        weights.put(2, new Double[]{0.2, 0.2, 0.6});
        return weights;
    }

    private Double[][] generateCenter() {
        int length = data.size();
        Double[] sum = new Double[]{0.0, 0.0, 0.0};
        for (int i = 0; i < length; i++) {
            sum[0] += data.get(i).getAttr()[0];
            sum[1] += data.get(i).getAttr()[1];
            sum[2] += data.get(i).getAttr()[2];
        }
        Double[][] centers = new Double[k][3];
        for (int i = 0; i < k; i++) {
            Double center = sum[i] / length;
            centers[i] = new Double[]{center, center, center};
        }
        return centers;
    }

    /**
     * 二支聚类
     * 使用kMeans算法对data聚类，结果封装成Result对象，对评价最优的聚类结果赋值给result属性
     */
    private void dictCluster() {
        Kmeans kmeans = new Kmeans(k);
        kmeans.setCenters(generateCenter());
        kmeans.setWeights(setWeights());
        kmeans.fit(data);
        Double[][] centers = kmeans.getCenters();
        List<Domain> result = kmeans.getCluster();
        this.result = new Result(centers, result);
    }

    private Double[][] getExtreValue(Iterator<ClusterVm> it) {
        Double[][] extra_attr = new Double[][]{{Double.MIN_VALUE, Double.MAX_VALUE}, {Double.MIN_VALUE, Double.MAX_VALUE}, {Double.MIN_VALUE, Double.MAX_VALUE}};
        while (it.hasNext()) {
            ClusterVm c = it.next();
            Double mips = c.getMips() * c.getNumberOfPes();
            Double ram = Double.valueOf(c.getRam());
            Double bw = Double.valueOf(c.getBw());
            if (mips > extra_attr[0][0]) {
                extra_attr[0][0] = mips;
            } else if (mips < extra_attr[0][1]) {
                extra_attr[0][1] = mips;
            }
            if (ram > extra_attr[1][0]) {
                extra_attr[1][0] = ram;
            } else if (ram < extra_attr[1][1]) {
                extra_attr[1][1] = ram;
            }
            if (bw > extra_attr[2][0]) {
                extra_attr[2][0] = bw;
            } else if (bw < extra_attr[2][1]) {
                extra_attr[2][1] = bw;
            }
        }
        return extra_attr;
    }

    /**
     * 数据预处理
     * 将各数据封装成Item对象，添加到data属性中
     */
    private void newDataFromT() {
        data = new ArrayList<Item>();
        if (raw.get(0) instanceof ClusterVm) {
            Double[][] extra_attr = getExtreValue((Iterator<ClusterVm>) raw.iterator());

            double mips_diff = extra_attr[0][0] - extra_attr[0][1];
            double ram_diff = extra_attr[1][0] - extra_attr[1][1];
            double bw_diff = extra_attr[2][0] - extra_attr[2][1];

            Iterator<ClusterVm> it = (Iterator<ClusterVm>) raw.iterator();
            for (int i = 0; it.hasNext(); i++) {
                ClusterVm c = it.next();
                double mips = (c.getMips()*c.getNumberOfPes() - extra_attr[0][1])/mips_diff;
                double ram = ((double) c.getRam() - extra_attr[1][1]) / ram_diff;
                double bw = ((double) c.getBw() - extra_attr[2][1]) / bw_diff;
                data.add(
                        new Item(c.getId(), i, new Double[]{mips, ram, bw})
                );
            }
        }
    }

    public void fit(List<T> raw) {
        this.raw = raw;
        newDataFromT();
        dictCluster();
    }

    public void adapt() {
        int k = result.getCenter().length;
        int c = result.getCenter()[0].length;

        if (raw.get(0) instanceof Vm) {
            for (int i = 0; i < k; i++) {
                Set<Item> s = result.getRes().get(i).getL();
                for (Item item : s) {
                    ClusterVm clusterVm = (ClusterVm) raw.get(item.getIndex());
                    clusterVm.setDomain("L");
                    Double[] weight = new Double[c];
                    for (int j = 0; j < c; j++) {
                        weight[j] = 0.0;
                    }
                    weight[i] = 1.0;
                    clusterVm.setWeight(weight);
                }

            }
        }
        System.out.print("pause");
    }

    public KMeans(int k) {
        this.k = k;
    }
}
