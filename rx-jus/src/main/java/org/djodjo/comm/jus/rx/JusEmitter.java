package org.djodjo.comm.jus.rx;


import android.os.Handler;

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

    BehaviorSubject<JusSignal> jusSubject = BehaviorSubject.create();
    Handler handler;

    public void emit(final JusSignal signal) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                doEmit(signal);
            }
        });
    }

    private void doEmit(final JusSignal signal) {
        jusSubject.onNext(signal);
    }

}
