package io.apptik.jus.examples.api;


import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.retro.http.GET;
import io.apptik.comm.jus.retro.http.Headers;
import io.apptik.comm.jus.retro.http.Query;

//api doc: https://market.mashape.com/dev132/instructables
public interface Intructables {


    String userString = "7jI9Tl5S67mshwTsdWqkjBSXR9E8p1WeqD5jsnCpTI3lct88ZY";
    String baseUrl = "https://devru-instructables.p.mashape.com/json-api/";

    @Headers({"X-Mashape-Key: " + userString,
            "Accept: text/plain"})
    @GET("getCategories")
    Request<String> getCategories();

    @Headers({"X-Mashape-Key: " + userString,
            "Accept: application/json"})
    @GET("showInstructable")
    Request<String> info(@Query("id") String id);

    @Headers({"X-Mashape-Key: " + userString,
    "Accept: application/json"})
    @GET("list")
    Request<String> list(@Query("limit") int limit, @Query("offset") int offset,
                         @Query("sort") String sort, @Query("type") String type);

}
