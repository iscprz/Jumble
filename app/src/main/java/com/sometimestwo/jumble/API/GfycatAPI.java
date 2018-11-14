package com.sometimestwo.jumble.API;
import com.sometimestwo.jumble.Model.GfycatWrapper;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface GfycatAPI {

    String BASE_URL = "https://gfycat.com/cajax/get";

    //Non-static feed name
    @GET("{hash}")
    Call<GfycatWrapper> getGfycat(@Path("hash") String hash);

    //static feed name
//    @GET("earthporn/.rss")
//    Call<Feed> getGyfCatObjToEnqueue();

/*    @POST("{user}")
    Call<CheckLogin> signIn(
            @HeaderMap Map<String, String> headers,
            @Path("user") String username,
            @Query("user") String user,
            @Query("passwd") String password,
            @Query("api_type") String type
    );

    @POST("{comment}")
    Call<CheckComment> submitComment(
            @HeaderMap Map<String, String> headers,
            @Path("comment") String comment,
            @Query("parent") String parent,
            @Query("amp;text") String text
    );*/
}
