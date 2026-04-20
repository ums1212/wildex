package dev.comon.wildex.data.capture

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

private const val TAG = "LocationResolver"
private const val LOCATION_TIMEOUT_MS = 8_000L

internal object LocationResolver {

    @SuppressLint("MissingPermission") // CaptureRecordRepository에서 hasLocationPermission 확인 후 호출
    suspend fun currentLocation(context: Context): Location? =
        withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            suspendCancellableCoroutine { cont ->
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        if (cont.isActive) cont.resume(loc)
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "위치 조회 실패", e)
                        if (cont.isActive) cont.resume(null)
                    }
                cont.invokeOnCancellation { cts.cancel() }
            }
        }

    suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                val geocoder = Geocoder(context, Locale.KOREA)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                            cont.resume(addresses.firstOrNull()?.formatAddress(latitude, longitude))
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1)
                        ?.firstOrNull()
                        ?.formatAddress(latitude, longitude)
                }
            }.getOrElse {
                Log.w(TAG, "Geocoder 실패", it)
                null
            }
        }

    private fun Address.formatAddress(lat: Double, lng: Double): String =
        listOfNotNull(adminArea, subLocality ?: locality, thoroughfare)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .trim()
            .ifEmpty { "%.4f, %.4f".format(lat, lng) }
}
