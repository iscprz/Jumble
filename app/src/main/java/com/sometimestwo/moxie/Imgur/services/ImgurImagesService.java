package com.sometimestwo.moxie.Imgur.services;

import com.sometimestwo.moxie.Imgur.response.images.SubmissionRoot;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ImgurImagesService {

    @GET("image/{hash}")
    Observable<SubmissionRoot> getImageByHash(@Path("hash") String imgHash);
}
