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

package io.apptik.comm.jus.rx.event;


import java.util.Arrays;

import io.apptik.comm.jus.JusLog;
import io.apptik.comm.jus.Request;

/**
 * General event signal that may contain a request, response and message desctribing the event
 */
public final class MarkerEvent extends JusEvent {

    //actual response
    public final JusLog.MarkerLog.Marker marker;

    //additional args
    public final Object[] args;

    public MarkerEvent(Request request, JusLog.MarkerLog.Marker marker, Object... args) {
        super(request);this.marker = marker;
        this.args = args;
    }

    @Override
    public String toString() {
        return "MarkerEvent{" +
                "request=" + request +
                ", args=" + Arrays.toString(args) +
                ", marker=" + marker +
                "} ";
    }
}
