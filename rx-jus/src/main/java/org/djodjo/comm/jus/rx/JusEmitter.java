package org.djodjo.comm.jus.rx;


import android.os.Handler;

import org.djodjo.comm.jus.error.JusError;

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

    public void setJusSubject(BehaviorSubject<JusEvent> jusSubject) {
        this.jusSubject = jusSubject;
    }

    BehaviorSubject<JusEvent> jusSubject = BehaviorSubject.create();
    BehaviorSubject<RequestEvent> requestSubject = BehaviorSubject.create();
    Handler handler;

    //emitter need to post onNext for the subject on the same thread
    public void emitJusEvent(final JusEvent event) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                doEmit(jusSubject, event);
            }
        });
    }

    public void emitRequestEvent(final RequestEvent event) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                doEmit(requestSubject, event);
            }
        });
    }
    public void emitRequestError(final RequestEvent event) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(event.response.getClass().isAssignableFrom(JusError.class)) {
                   //TODO enable this when subject can deliver error but continues after that
                   // doEmitError(requestSubject, (JusError) event.response);
                }
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
