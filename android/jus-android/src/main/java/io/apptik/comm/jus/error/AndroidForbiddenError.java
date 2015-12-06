/*
 * Copyright (C) 2015 AppTik Project
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

package io.apptik.comm.jus.error;

import android.content.Intent;

import io.apptik.comm.jus.NetworkResponse;

/**
 * Error indicating that there was an authentication failure when performing a Request.
 */
@SuppressWarnings("serial")
public class AndroidForbiddenError extends RequestError implements AndroidError {
    /**
     * An intent that can be used to resolve this exception. (Brings up the password dialog.)
     */
    private Intent mResolutionIntent;

    public AndroidForbiddenError(Intent intent, NetworkResponse response) {
        super(response, "User needs to (re)enter credentials");
        mResolutionIntent = intent;
    }

    public AndroidForbiddenError(NetworkResponse networkResponse) {
        super(networkResponse);
    }

    public Intent getResolutionIntent() {
        return mResolutionIntent;
    }

    @Override
    public String getMessage() {
        if (mResolutionIntent != null) {
            return "Access not granted with current credentials.\nUser needs to (re)enter " +
                    "credentials.";
        }
        return super.getMessage();
    }
}
