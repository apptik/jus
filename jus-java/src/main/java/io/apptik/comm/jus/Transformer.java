package io.apptik.comm.jus;


public interface Transformer {

    abstract class RequestTransformer {
        protected RequestQueue.RequestFilter filter;
        public RequestTransformer(RequestQueue.RequestFilter filter) {
            this.filter = filter;
        }

        public abstract NetworkRequest transform(NetworkRequest networkRequest);
    }

    abstract class ResponseTransformer {
        protected RequestQueue.RequestFilter filter;
        public ResponseTransformer(RequestQueue.RequestFilter filter) {
            this.filter = filter;
        }

        public abstract NetworkResponse transform(NetworkResponse networkResponse);
    }

}
