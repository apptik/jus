package io.apptik.comm.jus.perf;

public enum Pipeline {
    VolleyDef {
        @Override
        RequestPipeline create() {
            return new VolleyRequestPipeline();
        }
    },
    JusDef {
        @Override
        RequestPipeline create() {
            return new JusRequestPipeline();
        }
    },
    ;

    abstract RequestPipeline create();
}
