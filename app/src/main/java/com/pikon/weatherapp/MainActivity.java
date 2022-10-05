package com.pikon.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import org.w3c.dom.NodeList;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    protected TextView tvPlace, tvTemperature, tvDescription;
    protected LinearLayout llDetails;
    protected ImageView ivIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViews();

        findViewById( R.id.fabFind ).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent( MainActivity.this, SearchActivity.class );
                startActivity( i );
            }
        });

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Log.d( "DEBUG", "location changed" );
                getWeatherData( location.getLatitude(), location.getLongitude() );
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Toast.makeText(MainActivity.this, "Unable to access user location", Toast.LENGTH_LONG).show();
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

    protected void setViews()
    {
        tvPlace = (TextView) findViewById( R.id.tvPlace );
        tvTemperature = (TextView) findViewById( R.id.tvTemperature );
        tvDescription = (TextView) findViewById( R.id.tvDescription );
        llDetails = (LinearLayout) findViewById( R.id.llDetails );
        ivIcon = (ImageView) findViewById( R.id.ivIcon );
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

    public void getWeatherData( String place ) {
        Call<WeatherData> call = RetrofitWeather.getClient().getCity( place );
        weatherCallHandler( call );
    }

    public void getWeatherData( double lat, double lon )
    {
        Call<WeatherData> call = RetrofitWeather.getClient().getLocation( lat, lon );
        weatherCallHandler( call );
    }

    protected void weatherCallHandler( Call call ){
        call.enqueue(new Callback<WeatherData>() {
            @Override
            public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
                if( response.body() == null ){
                    Toast.makeText(getApplicationContext(), "Could not find weather for this location", Toast.LENGTH_LONG).show();
                    return;
                }
                setNewWeatherData( response.body() );
            }

            @Override
            public void onFailure(Call<WeatherData> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Unable to fetch weather data", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void setNewWeatherData( WeatherData data ){
        String place = data.getName() + ", " + data.getSys().getCountry();
        String temp = data.getMain().getTemp().toString() + " °C";
        String desc = data.getWeather().get(0).getDescription();
        tvPlace.setText( place );
        tvTemperature.setText( temp );
        tvDescription.setText( desc );
        String icon = data.getWeather().get(0).getIcon();
        // TODO
        Picasso.Builder builder = new Picasso.Builder(getApplicationContext());
        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                exception.printStackTrace();
                Log.e( "DEBUG", exception.getMessage() );
            }
        });

        Picasso p = builder.build();
//        p.setIndicatorsEnabled( true );
        p.load( "http://openweathermap.org/img/wn/" + icon + "@2x.png" )
                .error( R.drawable.ic_launcher_background )
                .into( ivIcon );

        ((TextView)((LinearLayout)llDetails.getChildAt( 0 )).getChildAt( 1 )).setText( String.valueOf( data.getMain().getHumidity() ) + " %" );
        ((TextView)((LinearLayout)llDetails.getChildAt( 1 )).getChildAt( 1 )).setText( String.valueOf( data.getMain().getTempMax() ) + " °C" );
        ((TextView)((LinearLayout)llDetails.getChildAt( 2 )).getChildAt( 1 )).setText( String.valueOf( data.getMain().getTempMin() ) + " °C" );
        ((TextView)((LinearLayout)llDetails.getChildAt( 3 )).getChildAt( 1 )).setText( String.valueOf( data.getMain().getPressure() ) + " hPa" );
        ((TextView)((LinearLayout)llDetails.getChildAt( 4 )).getChildAt( 1 )).setText( String.valueOf( data.getWind().getSpeed() ) + " m/s" );
    }
}