package io.apptik.comm.jus.mock;


import io.apptik.comm.jus.auth.TokenAuthenticator;
import io.apptik.comm.jus.error.AuthError;

public class MockTokenAuth extends TokenAuthenticator {



    @Override
    public String getToken() throws AuthError {
        return "mock-token";
    }

    @Override
    public void clearAuthValue() {

    }

    @Override
    public void invalidateToken() {

    }
}
