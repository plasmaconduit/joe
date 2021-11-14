package com.plasmaconduit.joe;


public interface Expressive {

    static Void voided(Runnable op) {
        op.run();
        return null;
    }

}
