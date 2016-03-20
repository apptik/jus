/*
 * Copyright (C) 2015 Apptik Project
 * Copyright (C) 2014 Kalin Maldzhanski
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apptik.comm.jus;

import io.apptik.comm.jus.error.JusError;

/**
 * Default retry policy for requests.
 */
public class DefaultRetryPolicy implements RetryPolicy {

    /** The default socket timeout in milliseconds */
    public static final int DEFAULT_TIMEOUT_MS = 5000;

    /** The default number of retries */
    public static final int DEFAULT_MAX_RETRIES = 1;

    /** The default backoff multiplier */
    public static final float DEFAULT_BACKOFF_MULT = 2f;

    /** The current timeout in milliseconds. */
    private int currentTimeoutMs;

    /** The current retry count. */
    private int mCurrentRetryCount;

    /** The maximum number of attempts. */
    private final int maxNumRetries;

    /** The backoff multiplier for the policy. */
    private final float backoffMultiplier;

    private final JusErrorFilter errorFilter;

    /**
     * Constructs a new retry policy using the default timeouts.
     */
    public DefaultRetryPolicy() {
        this(DEFAULT_TIMEOUT_MS, DEFAULT_MAX_RETRIES, DEFAULT_BACKOFF_MULT);
    }

    /**
     * Constructs a new retry policy.
     * @param initialTimeoutMs The initial timeout for the policy.
     * @param maxNumRetries The maximum number of retries.
     * @param backoffMultiplier Backoff multiplier for the policy.
     */
    public DefaultRetryPolicy(int initialTimeoutMs, int maxNumRetries, float backoffMultiplier) {
        this(initialTimeoutMs, maxNumRetries, backoffMultiplier, null);
    }

    /**
     * Constructs a new retry policy.
     * @param initialTimeoutMs The initial timeout for the policy.
     * @param maxNumRetries The maximum number of retries.
     * @param backoffMultiplier Backoff multiplier for the policy.
     * @param errorFilter Filter which should return true if we want to retry, false otherwise
     */
    public DefaultRetryPolicy(int initialTimeoutMs, int maxNumRetries, float backoffMultiplier,
                              JusErrorFilter errorFilter) {
        this.currentTimeoutMs = initialTimeoutMs;
        this.maxNumRetries = maxNumRetries;
        this.backoffMultiplier = backoffMultiplier;
        this.errorFilter = errorFilter;
    }

    /**
     * Returns the current timeout.
     */
    @Override
    public int getCurrentConnectTimeout() {
        return currentTimeoutMs;
    }

    /**
     * Returns the current timeout.
     */
    @Override
    public int getCurrentReadTimeout() {
        return currentTimeoutMs;
    }

    /**
     * Returns the current retry count.
     */
    @Override
    public int getCurrentRetryCount() {
        return mCurrentRetryCount;
    }

    /**
     * Returns the backoff multiplier for the policy.
     */
    public float getBackoffMultiplier() {
        return backoffMultiplier;
    }



    /**
     * Prepares for the next retry by applying a backoff to the timeout.
     * @param error The error code of the last attempt.
     */
    @Override
    public void retry(JusError error) throws JusError {
        if(errorFilter != null && !errorFilter.apply(error)) {
            throw error;
        }
        mCurrentRetryCount++;
        currentTimeoutMs = (int) (currentTimeoutMs * backoffMultiplier);
        if (!hasAttemptRemaining()) {
            throw error;
        }
    }

    /**
     * Returns true if this policy has attempts remaining, false otherwise.
     */
    protected boolean hasAttemptRemaining() {
        return mCurrentRetryCount <= maxNumRetries;
    }

    /**
     * A simple predicate or filter interface for JusError
     */
    public interface JusErrorFilter {
        boolean apply(JusError jusError);
    }
}
