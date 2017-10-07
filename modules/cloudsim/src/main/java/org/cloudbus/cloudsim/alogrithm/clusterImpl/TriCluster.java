package org.cloudbus.cloudsim.alogrithm.clusterImpl;


import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.alogrithm.clusterImpl.meta.Kmeans;
import org.cloudbus.cloudsim.alogrithm.entity.Disperse;
import org.cloudbus.cloudsim.alogrithm.entity.Domain;
import org.cloudbus.cloudsim.alogrithm.entity.Item;
import org.cloudbus.cloudsim.alogrithm.entity.Result;
import org.cloudbus.cloudsim.power.cluster.ClusterVm;

import java.util.*;

/**
 * Created by root on 8/11/17.
 */
public class TriCluster<T> {

    private List<T> raw;
    private List<Item> data;
    private int neighbor;

    private Result result;

    /**
     * manhadun距离
     *
     * @param paraFirstData
     * @param paraSecondData
     * @return
     */

    private double manhattanDistince(Double[] paraFirstData, Double[] paraSecondData) {
        double tempDistince = 0;
        if ((paraFirstData != null && paraSecondData != null) && paraFirstData.length == paraSecondData.length) {
            for (int i = 0; i < paraFirstData.length; i++) {
                tempDistince += Math.abs(paraFirstData[i] - paraSecondData[i]);
            }
        } else {
            System.out.println("firstData 与 secondData 数据结构不一致");
        }
        return tempDistince;
    }

    /**
     * 三支聚类
     */
    private void triCluster() {
        int k = result.getCenter().length;
        for (int i = 0; i < k; i++) {
            double distance = 0.0;
            List<Disperse> dists = new ArrayList<Disperse>();

            Set<Item> l = result.getRes().get(i).getL();
            Set<Item> m = result.getRes().get(i).getM();
            for (Item obj : l) {
                distance = manhattanDistince(obj.getAttr(), result.getCenter()[i]);
                dists.add(new Disperse(obj.getIndex(), distance));
            }
            Collections.sort(dists);

            int index = 0;
            double maxDiff = Double.MIN_VALUE;
            int size = dists.size();
            for (int j = 1; j < size; j++) {
                double diff = dists.get(j).getDistance() - dists.get(j - 1).getDistance();
                if (diff > maxDiff) {
                    maxDiff = diff;
                    index = j;
                }
            }
            for (int j = index; j < size; j++) {
                int indice = dists.get(j).getIndex();
                m.add(data.get(indice));
            }
            l.removeAll(m);
        }
    }

    /**
     * 类间分离性函数
     * 根据spec函数定义计算类间分离数值
     *
     * @param result 聚类结果
     * @return
     */

    private double specs(List<Domain> result) {
        int k = result.size();
        int length = data.size();
        double specOpt = 0;
        for (int i = 0; i < k; i++) {
            specOpt = Double.MIN_VALUE;
            int spec = 0;
            for (int j = i + 1; j < k; j++) {
                for (Item item : result.get(i).getL()) {
                    int index = item.getIndex();
                    for (int q = 1; q <= neighbor / 2; q++) {
                        if (index + q < length && (result.get(j).getL().contains(data.get(index + q)))) {
                            result.get(i).getM().add(item);
                            spec++;
                        } else if (index - q >= 0 && (result.get(j).getL().contains(data.get(index - q)))) {
                            result.get(i).getM().add(item);
                            spec++;
                        }
                    }
                }
                if (spec > specOpt) specOpt = spec;
            }
        }
        specOpt = specOpt / length / neighbor;
        return specOpt;
    }

    /**
     * 聚类有效性函数
     * cvin = comcs + spces, 评价聚类效果
     */
    private double cvin(List<Domain> result, double distance) {
        double comcs = distance;
        double specs = specs(result);
        return comcs + specs;
    }

    /**
     * 二支聚类
     * 使用kMeans算法对data聚类，结果封装成Result对象，对评价最优的聚类结果赋值给result属性
     */
    private void dictCluster() {
        int scale = (int) Math.sqrt(data.size());
        Map<Double, Result> weights = new TreeMap<Double, Result>();

        for (int i = 2; i < scale; i++) {
            Kmeans kmeans = new Kmeans(i);
            kmeans.fit(data);
            Double[][] centers = kmeans.getCenters();
            List<Domain> result = kmeans.getCluster();
            Result res = new Result(centers, result);
            double distance = kmeans.getMaxDistance();
            double weight = cvin(result, distance);
            weights.put(weight, res);
        }
        Iterator<Result> it = weights.values().iterator();
        this.result = it.next();
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

    /**
     * 三支聚类训练函数
     *
     * @param raw 按manhadun距离升序排列的列表数据
     */
    public void fit(List<T> raw) {
        this.raw = raw;
        newDataFromT();
        dictCluster();
        triCluster();
    }

    public void adapt() {
        int k = result.getCenter().length;
        int c = result.getCenter()[0].length;
        Double[][] level = new Double[k][c];
        double incre = Math.floor(100 / k);

        Map<Double, Integer> sortTree;

        for (int i = 0; i < c; i++) {
            double diff = 0.0;
            sortTree = new TreeMap<Double, Integer>();
            for (int j = 0; j < k; j++) {
                sortTree.put(result.getCenter()[j][i], j);
            }
            for (Map.Entry<Double, Integer> ent : sortTree.entrySet()) {
                level[ent.getValue()][i] = diff;
                diff += incre;
            }
        }
        result.setLevel(level);

        if (raw.get(0) instanceof Vm) {
            for (int i = 0; i < k; i++) {
                Set<Item> s = result.getRes().get(i).getL();
                for (Item item : s) {
                    ClusterVm clusterVm = (ClusterVm) raw.get(item.getIndex());
                    clusterVm.setDomain("L");
                    clusterVm.setWeight(result.getLevel()[i]);
                }
                s = result.getRes().get(i).getM();
                for (Item item : s) {
                    ClusterVm clusterVm = (ClusterVm) raw.get(item.getIndex());
                    clusterVm.setDomain("M");
                    clusterVm.setAttr(item.getAttr());
                }
            }
        }
        System.out.print("pause");
    }

    public TriCluster(int neighbor) {
        this.neighbor = neighbor;
    }

    public List<Item> getData() {
        return data;
    }

    /*public static void main(String[] args) throws FileNotFoundException {
        List<Container> containers = ContainerCloudSimFF.createContainerList(0, 500);
        Collections.sort(containers, new Comparator<Container>() {
            public int compare(Container c0, Container c1) {
                double dist0 = (int) (Math.pow(c0.getMips() * c0.getNumberOfPes(), 2) + Math.pow(c0.getRam(), 2) + Math.pow(c0.getBw(), 2));
                double dist1 = (int) (Math.pow(c1.getMips() * c1.getNumberOfPes(), 2) + Math.pow(c1.getRam(), 2) + Math.pow(c1.getBw(), 2));
                return dist0 > dist1 ? 1 : -1;
            }
        });

        TriCluster<Container> tc = new TriCluster<Container>(4);
        tc.fit(containers);

    }*/

}
