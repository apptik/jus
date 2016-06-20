package io.apptik.jus.samples.api;


import com.google.gson.JsonObject;

import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.retro.http.GET;
import io.apptik.comm.jus.retro.http.Headers;
import io.apptik.comm.jus.retro.http.Query;
import io.apptik.comm.jus.retro.http.Tag;
import io.apptik.json.JsonArray;

//api doc: https://market.mashape.com/dev132/instructables
public interface Instructables {



    String userString = "7jI9Tl5S67mshwTsdWqkjBSXR9E8p1WeqD5jsnCpTI3lct88ZY";
    String baseUrl = "https://devru-instructables.p.mashape.com/";

    String SORT_RECENT = "recent";
    String SORT_FEATURED = "featured";
    String SORT_POPULAR = "popular";
    String REQ_CATEGOTIES = "categories";
    String REQ_INFO = "info";
    String REQ_LIST = "list";

    @Tag(REQ_CATEGOTIES)
    @Headers({"X-Mashape-Key: " + userString,
            "Accept: text/plain"})
    @GET("json-api/getCategories")
    Request<JsonArray> getCategories();

    @Tag(REQ_INFO)
    @Headers({"X-Mashape-Key: " + userString,
            "Accept: application/json"})
    @GET("json-api/showInstructable")
    Request<JsonObject> info(@Query("id") String id);

    @Tag(REQ_LIST)
    @Headers({"X-Mashape-Key:" + userString,
    "Accept:application/json"})
    @GET("list")
    Request<JsonArray> list(@Query("limit") int limit, @Query("offset") int offset,
                            @Query("sort") String sort, @Query("type") String type);

}
