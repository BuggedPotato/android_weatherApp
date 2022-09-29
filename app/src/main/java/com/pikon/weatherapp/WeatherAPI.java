package com.pikon.weatherapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherAPI {
    @GET( "weather?appid=0450d012acf0cc600edbcf1377ca48e1&units=metric" )
    Call<WeatherData> getLocation(
            @Query( "lat" ) double lat,
            @Query( "lon" ) double lon
    );

    @GET( "weather?appid=0450d012acf0cc600edbcf1377ca48e1&units=metric" )
    Call<WeatherData> getCity( @Query( "q" ) String name );
}
