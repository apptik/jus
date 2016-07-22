/*
 * Copyright (C) 2015 AppTik Project
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

package io.apptik.comm.jus.request;

import io.apptik.comm.jus.http.FormEncodingBuilder;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.toolbox.Base64;

public class TokenRequest extends StringRequest {

    protected final String key;
    protected final String secret;

    TokenRequest(String method, String url, String key, String secret) {
        super(method, url);
        this.key = key;
        this.secret = secret;
        this.setNetworkRequest(
                new FormEncodingBuilder().add("grant_type", "client_credentials").build());

        if(key!=null && secret!=null) {
            this.setNetworkRequest(NetworkRequest.Builder.from(getNetworkRequest())
                    .setHeader("Authorization", "Basic "
                            + Base64.encodeToString((key + ":" + secret).getBytes(),
                            Base64.NO_WRAP))
                    .build());
        }
    }

    @Override
    public TokenRequest clone() {
        return (TokenRequest) new TokenRequest(getMethod(), getUrlString(), key, secret)
                .setNetworkRequest(this.getNetworkRequest());
    }
}
