package com.example.geocoder;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.location.Geocoder;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private TextView locationTextView;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTextView = findViewById(R.id.locationTextView);

        // Инициализация locationManager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Инициализация Geocoder
        geocoder = new Geocoder(this, Locale.getDefault());

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Обработка полученных координат
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Обратное геокодирование для получения адреса
                String address = getAddressFromCoordinates(latitude, longitude);

                // Обновление TextView с адресом
                updateLocationTextView(latitude, longitude, address);
            }

            // Другие методы LocationListener

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };

        // Проверка разрешений на геолокацию
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocation(); // Запрос местоположения только если разрешение уже предоставлено
        }
    }

    private void getLocation() {
        // Проверка разрешений на геолокацию
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            try {
                // Запрос местоположения с использованием сети (Wi-Fi и мобильная связь)
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            // Если разрешения на геолокацию отсутствуют, запросите их
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private String getAddressFromCoordinates(double latitude, double longitude) {
        try {
            List<android.location.Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                android.location.Address address = addresses.get(0);
                // Парсинг нужных данных из объекта Address
                String addressLine = address.getAddressLine(0);
                String city = address.getLocality();
                String state = address.getAdminArea();
                String country = address.getCountryName();
                String postalCode = address.getPostalCode();
                return addressLine + ", " + city + ", " + state + ", " + country + ", " + postalCode;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Адрес не найден";
    }

    public void updateLocationTextView(double latitude, double longitude, String address) {
        String locationText = "Широта: " + latitude + "\nДолгота: " + longitude + "\nАдрес: " + address;
        locationTextView.setText(locationText);
    }

    // Метод для обработки запроса разрешений
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                // Обработка отказа от предоставления разрешения - вы сами должны определить что делать здесь
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Остановка получения местоположения при закрытии приложения
        locationManager.removeUpdates(locationListener);
    }
}
