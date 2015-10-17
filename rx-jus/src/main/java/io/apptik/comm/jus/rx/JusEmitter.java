package io.apptik.comm.jus.rx;


import android.os.Handler;

import io.apptik.comm.jus.error.JusError;

import io.apptik.comm.jus.rx.event.JusEvent;
import io.apptik.comm.jus.rx.event.ResultEvent;
import rx.subjects.BehaviorSubject;

public class JusEmitter {

    private static JusEmitter inst;

    public static JusEmitter get() {
        if(inst==null) {
            inst = new JusEmitter();
        }
        return inst;
    }

    private JusEmitter() {
        handler = new Handler();
    }

    public BehaviorSubject<JusEvent> getJusSubject() {
        return jusSubject;
    }

    public BehaviorSubject<ResultEvent> getRequestSubject() {
        return requestSubject;
    }

    BehaviorSubject<JusEvent> jusSubject = BehaviorSubject.create();
    BehaviorSubject<ResultEvent> requestSubject = BehaviorSubject.create();

    Handler handler;

    //emitter need to post onNext for the subject on the same threadId
    public void emitJusEvent(final JusEvent event) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                doEmit(jusSubject, event);
            }
        });
    }

    public void emitRequestEvent(final ResultEvent event) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                doEmit(requestSubject, event);
            }
        });
    }
    public void emitRequestError(final ResultEvent event) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                doEmit(requestSubject, event);
            }
        });
    }

    private void doEmit(final BehaviorSubject subject, final Object signal) {
        subject.onNext(signal);
    }

    private void doEmitError(final BehaviorSubject subject, final JusError error) {
        subject.onError(error);
    }
}
