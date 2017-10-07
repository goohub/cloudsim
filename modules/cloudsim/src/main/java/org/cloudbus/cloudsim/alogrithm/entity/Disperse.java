package org.cloudbus.cloudsim.alogrithm.entity;

/**
 * Created by root on 8/13/17.
 */
public class Disperse implements Comparable{
    private Integer index;
    private Double distance;

    public Disperse(int index, double distance){
        this.index = index;
        this.distance = distance;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public int compareTo(Object o) {
        Disperse obj = (Disperse)o;
        return this.distance.compareTo(obj.getDistance());
    }
}
