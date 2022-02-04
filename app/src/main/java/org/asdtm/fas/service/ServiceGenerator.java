package org.asdtm.fas.service;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//Class to create service to interact with movie database api
public class ServiceGenerator {

    //Set api endpoint and api key for themoviedb
    public static final String ENDPOINT = "https://api.themoviedb.org/3/";
    public static final String API_KEY = "a103367a91b648e561c12948632c9d88";

    //Build new http client to interact with api
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
            .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));

    //Sets base url for api before using builder
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create());


    //build and create http service to interact with api
    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.client(httpClient.build()).build();
        return retrofit.create(serviceClass);
    }
}
