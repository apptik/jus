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

package io.apptik.jus.samples.dummy;

import android.app.Activity;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import io.apptik.json.JsonArray;
import io.apptik.json.JsonElement;
import io.apptik.json.JsonObject;


public class MockData {

    public static JsonArray getMockJsonArray(int noElements, int picSize) {
        JsonArray res = new JsonArray();
        Random rand = new Random();

        for(int i=0;i<noElements;i++) {
            int cc = (int)(Math.random() * 0x1000000);
            int cc2 = 0xFFFFFF00 ^ cc;
                    String color = Integer.toHexString(cc);
            String color2 = Integer.toHexString((0xFFFFFF - cc) );
           // String color2 = Integer.toHexString(cc2);
            res.add(new JsonObject().put("imageUrl", "http://dummyimage.com/" +picSize+ "/"+ color
                    +"/" + color2 + "&text=" + i).put("title", "title").put("author", "author")
                    .put("views", i).put("favorites", i));
        }

        return res;
    }

    public static JsonArray getAssetsMock(Activity activity) {
        JsonArray res = new JsonArray();
        try {
            res = JsonElement.readFrom(new InputStreamReader(activity.getAssets()
                    .open("pics.json"))).asJsonArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;

    }
}
