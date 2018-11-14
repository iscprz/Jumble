package com.sometimestwo.jumble.Imgur.services;

import com.sometimestwo.jumble.Imgur.response.tags.TagsRoot;

import io.reactivex.Observable;
import retrofit2.http.GET;


public interface TagsService {
    @GET("tags")
    Observable<TagsRoot> getTags();
}
