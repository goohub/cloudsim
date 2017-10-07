package org.cloudbus.cloudsim.alogrithm.entity;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by root on 8/13/17.
 */
public class Domain {
    private Set<Item> l;
    private Set<Item> m;

    public Domain() {
        l = new HashSet<Item>();
        m = new HashSet<Item>();
    }

    public Domain(Set<Item> l, Set<Item> m) {
        this.l = l;
        this.m = m;
    }

    public Set<Item> getL() {
        return l;
    }

    public void setL(Set<Item> l) {
        this.l = l;
    }

    public Set<Item> getM() {
        return m;
    }

    public void setM(Set<Item> m) {
        this.m = m;
    }
}
