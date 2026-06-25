package org.ckdk.toad_app.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

class LocationHelper(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onLocationReceived: (Location) -> Unit, onError: (String) -> Unit) {
        try {
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                onError("El GPS y la localización por red están desactivados.")
                return
            }

            // Get last known location first to see if it's recent
            val lastKnownGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastKnownNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            var bestLocation = lastKnownGps
            if (lastKnownNet != null && (bestLocation == null || lastKnownNet.time > bestLocation.time)) {
                bestLocation = lastKnownNet
            }

            if (bestLocation != null && (System.currentTimeMillis() - bestLocation.time) < 30000) {
                // If last known location is fresh (less than 30 seconds old), use it immediately
                onLocationReceived(bestLocation)
                return
            }

            // Otherwise, request a single fresh update
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    onLocationReceived(location)
                    locationManager.removeUpdates(this)
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            val provider = if (isGpsEnabled) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
            locationManager.requestLocationUpdates(provider, 0L, 0f, listener)
        } catch (e: SecurityException) {
            onError("Permisos de ubicación denegados.")
        } catch (e: Exception) {
            onError(e.message ?: "Error al obtener la ubicación.")
        }
    }
}
