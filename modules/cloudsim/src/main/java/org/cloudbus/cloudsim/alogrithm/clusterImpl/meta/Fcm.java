package org.cloudbus.cloudsim.alogrithm.clusterImpl.meta;

import org.cloudbus.cloudsim.alogrithm.entity.Domain;
import org.cloudbus.cloudsim.alogrithm.entity.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 */
public class Fcm {

    private List<Item> raw;
    private Double[][] data;
    private Double[][] centers;
    private Double[][] umatrix;
    private List<Domain> cluster;
    private int k;

    private int maxcycle;//最大的迭代次数
    private double m;//参数m
    private double limit;

    private int getMaster(int column) {
        int index = 0;
        int columns = umatrix.length;
        for (int i = 1; i < columns; i++) {
            if (umatrix[i][column] > umatrix[index][column]) {
                index = i;
            }
        }
        return index;
    }

    /**
     * 求矩阵第j列之和操作--用于规范化样本
     *
     * @param array
     * @param j
     * @return
     */
    public static double sumArray(double array[][], int j) {
        double sum = 0;
        for (int i = 0; i < array.length; i++) {
            sum += array[i][j];
        }
        return sum;
    }

    /**
     * 求数组最大值最小值
     *
     * @param a 输入
     * @return 返回一个包含2个元素的一维数组
     */
    public static double[] getExtreNum(double[] a) {

        double minmax[] = new double[2];
        double minValue = a[0];
        double maxValue = a[1];
        for (int i = 1; i < a.length; i++) {
            if (a[i] < minValue)
                minValue = a[i];
            if (a[i] > maxValue)
                maxValue = a[i];
        }
        minmax[0] = minValue;
        minmax[1] = maxValue;
        return minmax;
    }

    /**
     * 规范化样本
     *
     * @return
     */
    public boolean Normalized() {
        int rows = data.length;
        int columns = data[0].length;
        double a[] = new double[rows];//提取列
        //先复制pattern到copypattern
        Double[][] copydata = new Double[rows][columns];
        System.arraycopy(data, 0, copydata, 0, rows);

        for (int col = 0; col < columns; col++) {
            for (int row = 0; row < rows; row++)
                a[row] = data[row][col];
            double[] min_max = getExtreNum(a);
            double minValue = min_max[0];
            double maxValue = min_max[1];
            for (int row = 0; row < rows; row++) {
                data[row][col] = (copydata[row][col] - minValue) / (maxValue - minValue);
            }
        }

        return true;
    }

    private void extractAttr(List<Item> raw) {
        int rows = raw.size();
        int columns = raw.get(0).getAttr().length;
        data = new Double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                data[i][j] = raw.get(i).getAttr()[j];
            }
        }
    }

    public void fit(List<Item> raw) {
        this.raw = raw;
        extractAttr(raw);
        int rows = this.data.length;
        int columns = this.data[0].length;

        //验证输入参数的有效性
        if (k >= rows || m <= 1)
            return;

        Normalized();

        int retry = 0, testflag = 0;//迭代次数，迭代标志
        umatrix = new Double[k][rows];
        centers = new Double[k][columns];

        //初始化隶属度
        double temp[][] = new double[k][rows];
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < rows; j++) {
                umatrix[i][j] = Math.random();
                temp[i][j] = umatrix[i][j];
            }
        }

        for (int i = 0; i < k; i++)
            for (int j = 0; j < rows; j++) {
                umatrix[i][j] = temp[i][j] / sumArray(temp, j);
            }

        Double[][] umatrix_temp = new Double[k][rows];
        while (testflag == 0) {

            //每次保存更新前的隶属度
            System.arraycopy(umatrix, 0, umatrix_temp, 0, k);

            //更新聚类中心
            for (int t = 0; t < k; t++) {
                for (int col = 0; col < columns; col++) {
                    double a = 0, b = 0;
                    for (int row = 0; row < rows; row++) {
                        a += Math.pow(umatrix[t][row], m) * data[row][col];
                        b += Math.pow(umatrix[t][row], m);
                    }
                    centers[t][col] = a / b;
                }
            }

            //更新隶属度
            double c = 0, d = 0;
            for (int t = 0; t < k; t++) {
                for (int row = 0; row < rows; row++) {
                    double e = 0;
                    for (int j = 0; j < k; j++) {
                        for (int col = 0; col < columns; col++) {
                            c += Math.pow(data[row][col] - centers[t][col], 2);
                            d += Math.pow(data[row][col] - centers[j][col], 2);
                        }
                        e += c / d;
                        c = 0;
                        d = 0;
                    }
                    umatrix[t][row] = 1 / e;
                }
            }

            //判断是否收敛或达到最大迭代次数
            double minus[][] = new double[k][rows];
            for (int i = 0; i < k; i++)
                for (int j = 0; j < rows; j++) {
                    minus[i][j] = Math.abs(umatrix_temp[i][j] - umatrix[i][j]);
                }

            double f = 0;
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < rows; j++) {
                    if (minus[i][j] > f)
                        f = minus[i][j];//f矩阵中最大值
                }
            }

            if (f <= limit || retry > maxcycle)
                testflag = 1;

            retry++;
        }
        cluster = new ArrayList<Domain>();
        for (int j = 0; j < k; j++) {
            cluster.add(new Domain());
        }
        for (int j = 0; j < rows; j++) {
            Domain dom = cluster.get(getMaster(j));
            dom.getL().add(raw.get(j));
        }
    }

    public Fcm(int k, int maxcycle, double m, double limit) {
        this.k = k;
        this.maxcycle = maxcycle;
        this.m = m;
        this.limit = limit; //1e-6
    }

    public Double[][] getCenters() {
        return centers;
    }

    public void setCenters(Double[][] centers) {
        this.centers = centers;
    }

    public List<Domain> getCluster() {
        return cluster;
    }
}