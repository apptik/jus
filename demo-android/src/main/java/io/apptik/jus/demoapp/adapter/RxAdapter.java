package io.apptik.jus.demoapp.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;

import io.apptik.json.JsonArray;
import rx.Observable;
import rx.Subscription;

public class RxAdapter extends RecyclerAdapter {

    Subscription subscription;
    Observable<JsonArray> source;

    public RxAdapter(Observable<JsonArray> source) {
        super(null);
        this.source = source;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if(source!=null) {
            subscription = source.subscribe(this::updateData);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        if(subscription!=null) {
            subscription.unsubscribe();
        }
        super.onDetachedFromRecyclerView(recyclerView);
    }
}
