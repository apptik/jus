package io.apptik.comm.jus.examples;

import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.retro.http.GET;
import io.apptik.comm.jus.retro.http.Path;

public interface BeerService {

    String userString = "c6266a50b6603fe87d681ef34fe11e3e";
    String baseUrl = "http://beermapping.com/webservice/";

    //get general info
    String locquery = "locquery";

    //get lat/lon
    String locmap = "locmap";

    //get ratings
    String locscore = "locscore";

    //get pics
    String locimage = "locimage";

    String fullUrl = baseUrl + locquery
            + "/" + userString + "/777";


    @GET("locquery/{user}/{q}")
    Request<String> getBeer(
            @Path("user") String user,
            @Path("q") String q);
}
