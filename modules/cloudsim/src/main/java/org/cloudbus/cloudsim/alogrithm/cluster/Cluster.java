package org.cloudbus.cloudsim.alogrithm.cluster;

import org.cloudbus.cloudsim.alogrithm.entity.Item;

import java.util.List;

/**
 * Created by root on 8/12/17.
 */
public interface Cluster {
    public void fit(List<Item> data);
}
