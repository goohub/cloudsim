package org.cloudbus.cloudsim.alogrithm.entity;

/**
 * Created by root on 8/12/17.
 */
public class Item {
    private int id;
    private int index;
    private Double[] attr;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Double[] getAttr() {
        return attr;
    }

    public void setAttr(Double[] attr) {
        this.attr = attr;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Item(int id, int index, Double[] attr) {
        this.id = id;
        this.index = index;
        this.attr = attr;
    }
}
