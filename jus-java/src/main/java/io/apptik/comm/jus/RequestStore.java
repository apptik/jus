package io.apptik.comm.jus;



public interface RequestStore {

    /**
     * Push request to the store
     * @param request
     */
    void push(Request<?> request);

    /**
     * Remove request from the store if it exists
     * @param request
     */
    void remove(Request<?> request);

    /**
     * Retrieve the next available {@link Request} and removes it from the store
     * @param requestFilter useful to filter the next {@link Request} in case specific type is
     *                      needed
     * @return appropriate {@link Request} or null
     */
    Request<?> pop(RequestQueue.RequestFilter requestFilter);
}
