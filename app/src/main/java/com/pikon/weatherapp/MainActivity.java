package com.pikon.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView tvPlace, tvTemperature, tvDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvPlace = (TextView) findViewById( R.id.tvPlace );
        tvTemperature = (TextView) findViewById( R.id.tvTemperature );
        tvDescription = (TextView) findViewById( R.id.tvDescription );

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Log.d( "DEBUG", "location changed" );
                getWeatherData( location.getLatitude(), location.getLongitude() );
            }
        };
        if( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            requestPermissions( new String[] { Manifest.permission.INTERNET, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION }, 2137 );
        }else{
            locationManager.requestLocationUpdates(
                    locationManager.GPS_PROVIDER,
                    500, 50, locationListener
            );
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if( requestCode == 2137 ){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                Log.d( "DEBUG", "internet access" );
            }else
                Toast.makeText(this, "Unable to access the Internet", Toast.LENGTH_LONG).show();

            if( grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED ){
                Log.d( "DEBUG", "location coarse access" );
            }else
                Toast.makeText(this, "Unable to access location", Toast.LENGTH_LONG).show();
        }
    }

    private void getWeatherData( double lat, double lon )
    {
        Call<WeatherData> call = RetrofitWeather.getClient().getLocation( lat, lon );
        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                setNewWeatherData( response.body() );
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Unable to fetch weather data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setNewWeatherData( WeatherData data ){
        String place = data.getName() + ", " + data.getSys().getCountry();
        String temp = data.getMain().getTemp().toString() + " °C";
        String desc = data.getWeather().get(0).getDescription();
        tvPlace.setText( place );
        tvTemperature.setText( temp );
        tvDescription.setText( desc );
    }
}