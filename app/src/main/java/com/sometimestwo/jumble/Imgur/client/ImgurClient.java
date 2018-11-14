package com.sometimestwo.jumble.Imgur.client;

import com.sometimestwo.jumble.Imgur.services.ImgurGalleriesService;
import com.sometimestwo.jumble.Imgur.services.ImgurImagesService;
import com.sometimestwo.jumble.Imgur.services.TagsService;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImgurClient {


    private static final String BASE_URL = "https://api.imgur.com/3/";
    private static final String CLIENT_ID = "e7e94ec57195bb6";

    public static String getBackgroundImageUrl(String imgHash) {
        return "http://i.imgur.com/" + imgHash + ".png";
    }


    public TagsService getTagsObservable() {

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(ImgurClient.BASE_URL)
                .client(getAuthorizationHeaderInterceptor())
                .build();

        return retrofit.create(TagsService.class);
    }

    public ImgurGalleriesService getGallerieForTag() {

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(ImgurClient.BASE_URL)
                .client(getAuthorizationHeaderInterceptor())
                .build();

        return retrofit.create(ImgurGalleriesService.class);

    }

    public ImgurImagesService getImageService(){
        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(ImgurClient.BASE_URL)
                .client(getAuthorizationHeaderInterceptor())
                .build();
        return retrofit.create(ImgurImagesService.class);
    }

    public OkHttpClient getAuthorizationHeaderInterceptor() {

        return new OkHttpClient().newBuilder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                Request originalRequest = chain.request();

                Request.Builder builder = originalRequest.newBuilder().header("Authorization",
                        "Client-ID " + ImgurClient.CLIENT_ID);

                Request newRequest = builder.build();
                return chain.proceed(newRequest);
            }
        }).build();
    }
}
