package com.sometimestwo.moxie.Imgur.services;

import com.sometimestwo.moxie.Imgur.response.tags.TagsRoot;

import io.reactivex.Observable;
import retrofit2.http.GET;


public interface TagsService {
    @GET("tags")
    Observable<TagsRoot> getTags();
}
