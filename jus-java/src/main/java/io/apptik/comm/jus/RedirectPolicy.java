package io.apptik.comm.jus;

import java.net.HttpURLConnection;

import io.apptik.comm.jus.http.HttpUrl;

/**
 * Defines redirect policy for a request
 */
public interface RedirectPolicy {
    /**
     * If redirect is supported then just modify request as needed and return true
     * otherwise return false which will result eventually in an exception thrown
     *
     * @param request
     * @param networkResponse
     * @return the request to be resend if will redirect, null otherwise
     */
    Request verifyRedirect(Request request, NetworkResponse networkResponse);

    class DefaultRedirectPolicy implements RedirectPolicy {
        @Override
        public Request verifyRedirect(Request request, NetworkResponse networkResponse) {
            Request res = null;
            if (networkResponse != null && networkResponse.headers != null
                    && networkResponse.headers.get("location") != null && (
                    networkResponse.statusCode == HttpURLConnection.HTTP_MULT_CHOICE
                            || networkResponse.statusCode == HttpURLConnection.HTTP_MOVED_PERM
                            || networkResponse.statusCode == HttpURLConnection.HTTP_MOVED_TEMP
                            || networkResponse.statusCode == HttpURLConnection.HTTP_SEE_OTHER
                            || networkResponse.statusCode == 307
                            || networkResponse.statusCode == 308

            )) {
                HttpUrl url = request.getUrl().resolve(networkResponse.headers.get("location"));
                if (networkResponse.statusCode == HttpURLConnection.HTTP_SEE_OTHER) {
                    res = new Request(Request.Method.GET, url)
                            .setNetworkRequest(new NetworkRequest.Builder()
                                    .addHeaders(request.getNetworkRequest().headers).build());
                } else {
                    res = new Request(request.getMethod(), url)
                            .setNetworkRequest(request.getNetworkRequest());
                }
                res.setRetryPolicy(request.getRetryPolicy())
                        .setRedirectPolicy(request.getRedirectPolicy());
            }
            return res;
        }
    }

    abstract class Factory {
        public RedirectPolicy get(Request request) {
            return new DefaultRedirectPolicy();
        }
    }
}
