package com.beanbeanjuice.simpleproxychat.shared.utility.datastructures;

import java.util.ArrayList;

public class BoundedArrayList<T> extends ArrayList<T> {

    private final int maximumSize;

    public BoundedArrayList(final int maximumSize) {
        this.maximumSize = maximumSize;
    }

    @Override
    public boolean add(T t) {
        if (super.size() >= maximumSize) super.remove(0);
        return super.add(t);
    }

}
