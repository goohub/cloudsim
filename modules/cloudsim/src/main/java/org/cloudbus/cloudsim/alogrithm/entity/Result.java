package org.cloudbus.cloudsim.alogrithm.entity;

import java.util.List;

/**
 * Created by root on 8/12/17.
 */
public class Result {
    private Double[][] center;
    private Double[][] level;
    private List<Domain> res;

    public Double[][] getCenter() {
        return center;
    }

    public void setCenter(Double[][] center) {
        this.center = center;
    }

    public List<Domain> getRes() {
        return res;
    }

    public void setRes(List<Domain> res) {
        this.res = res;
    }

    public Double[][] getLevel() {
        return level;
    }

    public void setLevel(Double[][] level) {
        this.level = level;
    }

    public Result(Double[][] center, List<Domain> res) {
        this.center = center;
        this.res = res;
    }
}
