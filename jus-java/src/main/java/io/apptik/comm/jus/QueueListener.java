package io.apptik.comm.jus;


import io.apptik.comm.jus.toolbox.Utils;

public interface QueueListener {
    abstract class QListener<T> {
        protected final Request<T> request;

        protected QListener(Request<T> request) {
            Utils.checkNotNull(request, "request==null");
            this.request = request;
        }
    }

    abstract class QResponseListener<T> extends QListener implements RequestListener
            .ResponseListener<T> {
        protected QResponseListener(Request<T> request) {
            super(request);
        }
    }

    abstract class QErrorListener extends QListener implements RequestListener
            .ErrorListener {
        protected QErrorListener(Request<?> request) {
            super(request);
        }
    }

    abstract class QMarkerListener extends QListener implements RequestListener
            .MarkerListener {
        protected QMarkerListener(Request<?> request) {
            super(request);
        }
    }

    interface QListenerFactory {
        QResponseListener getResponseListener(Request<?> request);

        QErrorListener getErrorListener(Request<?> request);

        QMarkerListener getMarkerListener(Request<?> request);
    }

    class SimpleQListenerFactory implements QListenerFactory {
        @Override
        public QResponseListener getResponseListener(Request<?> request) {
            return null;
        }

        @Override
        public QErrorListener getErrorListener(Request<?> request) {
            return null;
        }

        @Override
        public QMarkerListener getMarkerListener(Request<?> request) {
            return null;
        }
    }

    abstract class FilteredQListenerFactory implements QListenerFactory {
        protected final RequestQueue.RequestFilter filter;

        protected FilteredQListenerFactory(RequestQueue.RequestFilter filter) {
            this.filter = filter;
        }

        protected abstract <T> QResponseListener<T> getFilteredResponseListener(Request<T> request);

        protected abstract QErrorListener getFilteredErrorListener(Request<?> request);

        protected abstract QMarkerListener getFilteredMarkerListener(Request<?> request);

        @Override
        public final QResponseListener getResponseListener(Request<?> request) {
            if (filter == null || filter.apply(request)) {
                return getFilteredResponseListener(request);
            }
            return null;
        }

        @Override
        public final QErrorListener getErrorListener(Request<?> request) {
            if (filter == null || filter.apply(request)) {
                return getFilteredErrorListener(request);
            }
            return null;
        }

        @Override
        public final QMarkerListener getMarkerListener(Request<?> request) {
            if (filter == null || filter.apply(request)) {
                return getFilteredMarkerListener(request);
            }
            return null;
        }
    }

    class SimpleFilteredQListenerFactory extends FilteredQListenerFactory {

        public SimpleFilteredQListenerFactory() {
            super(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        }

        public SimpleFilteredQListenerFactory(RequestQueue.RequestFilter filter) {
            super(filter);
        }

        @Override
        protected <T> QResponseListener<T> getFilteredResponseListener(Request<T> request) {
            return null;
        }

        @Override
        protected QErrorListener getFilteredErrorListener(Request<?> request) {
            return null;
        }

        @Override
        protected QMarkerListener getFilteredMarkerListener(Request<?> request) {
            return null;
        }
    }

}
