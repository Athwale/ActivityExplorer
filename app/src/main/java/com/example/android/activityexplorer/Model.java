package com.example.android.activityexplorer;

public final class Model {

    final long id;
    final String label;
    final String packageName;
    boolean viewed;
    boolean useful;

    Model(long id, String label, String packageName) {
        this.id = id;
        this.label = label;
        this.packageName = packageName;
        this.viewed = false;
        this.useful = false;
    }

    void setViewed(boolean viewed) {
        this.viewed = viewed;
    }

    boolean getViewed() {
        return this.viewed;
    }

    void setUseful(boolean useful) {
        this.useful = useful;
    }

    boolean getUseful() {
        return this.useful;
    }

}
