package com.sometimestwo.moxie.Imgur.services;

import com.sometimestwo.moxie.Imgur.response.galleries.GalleriesTagRoot;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;


public interface ImgurGalleriesService {

    @GET("gallery/t/{tagName}/{sort}/{window}/{page}")
    Observable<GalleriesTagRoot> getPostsFromTag(@Path("tagName") String tagName, @Path("sort") String sort,
                                                 @Path("window") String window, @Path("page") int page);
}
