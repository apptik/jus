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

import android.os.Handler;

import java.util.concurrent.Executor;

import io.apptik.comm.jus.ExecutorDelivery;

/**
 * Delivers responses and errors.
 */
public class AndroidExecutorDelivery extends ExecutorDelivery {
    /**
     * Creates a new response delivery interface.
     * @param handler {@link Handler} to post responses on
     */
    public AndroidExecutorDelivery(final Handler handler) {
        // Make an Executor that just wraps the handler.
        super(new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        });
    }
}
