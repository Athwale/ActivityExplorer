package com.example.android.activityexplorer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public final class Adapter extends BaseAdapter {

    private final ArrayList<Model> models = new ArrayList<>();

    private boolean[] enabled = new boolean[0];
    private boolean filtered;

    @Override
    public int getCount() {
        if (filtered) {
            int count = 0;
            for (boolean item : enabled) {
                if (item) {
                    count++;
                }
            }
            return count;
        } else {
            return models.size();
        }
    }

    @Override
    public Model getItem(int index) {
        return models.get(getRealIndex(index));
    }

    @Override
    public long getItemId(int index) {
        return getItem(index).id;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.model, viewGroup, false);
        }
        String text = getItem(index).label;
        if (getItem(index).getViewed()) {
            text = "+ " + text;
        }
        if (getItem(index).getUseful()) {
            text = "U " + text;
        }
        text = getItem(index).id + " " + text;
        ((TextView) view).setText(text);
        return view;
    }

    void update(List<Model> models) {
        this.models.clear();
        this.models.addAll(models);
        enabled = new boolean[models.size()];
        filtered = false;
        notifyDataSetChanged();
    }

    private int getRealIndex(int index) {
        if (!filtered) {
            return index;
        }
        int virtualIndex = -1;
        int realIndex = -1;
        for (Boolean item : enabled) {
            if (item) {
                virtualIndex++;
            }
            realIndex++;
            if (virtualIndex == index) {
                break;
            }
        }
        return realIndex;
    }

}
