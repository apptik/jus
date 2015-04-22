package org.djodjo.comm.jus.examples.mock;

import org.djodjo.json.JsonArray;
import org.djodjo.json.JsonObject;

import java.util.Random;

/**
 * Created by sic on 22/04/15.
 */
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
            res.add(new JsonObject().put("pic", "http://dummyimage.com/" +picSize+ "/"+ color +"/" + color2));
        }

        return res;
    }
}
