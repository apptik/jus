package io.apptik.jus.demoapp;


import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.rx.RxQueueHub;
import io.apptik.json.JsonArray;
import io.apptik.json.JsonObject;
import rx.Observable;

import static io.apptik.jus.demoapp.api.Instructables.REQ_INFO;
import static io.apptik.jus.demoapp.api.Instructables.REQ_LIST;

public class SampleHubShield {
    private final RxQueueHub hub;

    public SampleHubShield(RequestQueue q) {
        this.hub = new RxQueueHub(q);
    }

    Observable<JsonArray> getList() {
        return hub.results(REQ_LIST).cast(JsonArray.class);
    }

    Observable<JsonObject> getInfo() {
        return hub.results(REQ_INFO).cast(JsonObject.class);
    }
 }
