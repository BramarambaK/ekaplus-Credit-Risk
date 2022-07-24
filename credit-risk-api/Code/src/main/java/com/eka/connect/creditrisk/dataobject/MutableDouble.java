package com.eka.connect.creditrisk.dataobject;

public class MutableDouble {
    public double value;

    public MutableDouble() {

    }

    public MutableDouble(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void add(double value) {
        this.value += value;
    }
    public void subtract(double value) {
        this.value -= value;
    }
}
