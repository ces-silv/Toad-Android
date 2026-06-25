package org.ckdk.toad_app.ui.main

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ckdk.toad_app.data.location.LocationHelper
import org.ckdk.toad_app.data.model.ReportListResult
import org.ckdk.toad_app.data.model.ReportResult
import org.ckdk.toad_app.data.database.AppDatabase
import org.ckdk.toad_app.data.database.dao.ReportDao
import org.ckdk.toad_app.data.database.entity.ReportEntity
import org.ckdk.toad_app.data.database.entity.toEntity
import org.ckdk.toad_app.data.database.entity.toResponse
import org.ckdk.toad_app.data.model.User
import org.ckdk.toad_app.data.network.BackendOrchestrator
import org.ckdk.toad_app.data.network.model.ReportImageRequest
import org.ckdk.toad_app.data.network.model.ReportResponse

data class SelectedImage(
    val uri: Uri,
    val contentType: String,
    val base64Data: String
)

data class ReportUiState(
    val description: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val selectedImages: List<SelectedImage> = emptyList(),
    
    val descriptionError: String? = null,
    val addressError: String? = null,
    val locationError: String? = null,
    
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    
    val reports: List<ReportResponse> = emptyList(),
    val isLoadingReports: Boolean = false,
    val reportsError: String? = null,
    
    val isMapOpen: Boolean = false,
    val isFetchingLocation: Boolean = false
)

class ReportViewModel(
    private val user: User,
    private val reportDao: ReportDao,
    private val orchestrator: BackendOrchestrator = BackendOrchestrator()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun onDescriptionChanged(value: String) {
        _uiState.update { it.copy(description = value, descriptionError = null, successMessage = null, errorMessage = null) }
    }

    fun onAddressChanged(value: String) {
        _uiState.update { it.copy(address = value, addressError = null, successMessage = null, errorMessage = null) }
    }

    fun onLocationSelected(lat: Double, lng: Double) {
        _uiState.update { it.copy(latitude = lat, longitude = lng, locationError = null) }
    }

    fun openMap() {
        _uiState.update { it.copy(isMapOpen = true) }
    }

    fun closeMap() {
        _uiState.update { it.copy(isMapOpen = false) }
    }

    fun onImagesSelected(context: Context, uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.Default) {
            val processed = uris.mapNotNull { uri ->
                uriToBase64(context, uri)
            }
            withContext(Dispatchers.Main) {
                _uiState.update { current ->
                    current.copy(selectedImages = current.selectedImages + processed)
                }
            }
        }
    }

    fun onRemoveImage(index: Int) {
        _uiState.update { current ->
            val updated = current.selectedImages.toMutableList().apply { removeAt(index) }
            current.copy(selectedImages = updated)
        }
    }

    fun onFetchCurrentLocation(context: Context) {
        _uiState.update { it.copy(isFetchingLocation = true, locationError = null) }
        val locationHelper = LocationHelper(context)
        locationHelper.getCurrentLocation(
            onLocationReceived = { location ->
                _uiState.update {
                    it.copy(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        isFetchingLocation = false
                    )
                }
            },
            onError = { errorMsg ->
                _uiState.update {
                    it.copy(
                        isFetchingLocation = false,
                        locationError = errorMsg
                    )
                }
            }
        )
    }

    fun clearStatusMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    fun onSubmitReport() {
        val state = _uiState.value
        if (!validate(state)) return

        _uiState.update { it.copy(isLoading = true, successMessage = null, errorMessage = null) }

        viewModelScope.launch {
            if (user.token == "OFFLINE_MODE") {
                val tempId = java.util.UUID.randomUUID().toString()
                val offlineImages = state.selectedImages.map {
                    org.ckdk.toad_app.data.network.model.ReportImageResponse(
                        id = java.util.UUID.randomUUID().toString(),
                        contentType = it.contentType,
                        base64Data = it.base64Data
                    )
                }
                val offlineEntity = ReportEntity(
                    id = tempId,
                    description = state.description.trim(),
                    latitude = state.latitude,
                    longitude = state.longitude,
                    address = state.address.trim(),
                    status = "PENDIENTE (OFFLINE)",
                    userId = "offline_user",
                    reporterName = "Modo Offline",
                    createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date()),
                    images = offlineImages,
                    cachedByUsername = "Modo Offline"
                )
                try {
                    reportDao.insertReports(listOf(offlineEntity))
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            description = "",
                            address = "",
                            latitude = 0.0,
                            longitude = 0.0,
                            selectedImages = emptyList(),
                            successMessage = "Reporte registrado localmente (Offline)."
                        )
                    }
                    onFetchMyReports()
                } catch (e: Exception) {
                    e.printStackTrace()
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = "Error al registrar reporte localmente: ${e.message}"
                        )
                    }
                }
                return@launch
            }

            val networkImages = state.selectedImages.map {
                ReportImageRequest(contentType = it.contentType, base64Data = it.base64Data)
            }

            val result = orchestrator.createReport(
                token = user.token,
                description = state.description.trim(),
                latitude = state.latitude,
                longitude = state.longitude,
                address = state.address.trim(),
                images = networkImages.ifEmpty { null }
            )

            _uiState.update { current ->
                when (result) {
                    is ReportResult.Success -> {
                        // Guardar en Room tras respuesta exitosa 2XX del servidor
                        try {
                            reportDao.insertReports(listOf(result.report.toEntity(user.username)))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        // Clear form state upon successful submission
                        current.copy(
                            isLoading = false,
                            description = "",
                            address = "",
                            latitude = 0.0,
                            longitude = 0.0,
                            selectedImages = emptyList(),
                            successMessage = "Reporte enviado con éxito."
                        )
                    }
                    is ReportResult.Unauthorized -> {
                        current.copy(
                            isLoading = false,
                            errorMessage = "Sesión expirada. Por favor inicie sesión nuevamente."
                        )
                    }
                    is ReportResult.Error -> {
                        current.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }

            // If success, refresh the user's reports list
            if (result is ReportResult.Success) {
                onFetchMyReports()
            }
        }
    }

    fun onFetchMyReports() {
        _uiState.update { it.copy(isLoadingReports = true, reportsError = null) }
        viewModelScope.launch {
            if (user.token == "OFFLINE_MODE") {
                // Modo Offline: Cargar todos los reportes locales ("registrados desde este celular")
                try {
                    val localReports = reportDao.getAllReports().map { it.toResponse() }
                    _uiState.update { current ->
                        current.copy(
                            isLoadingReports = false,
                            reports = localReports
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    _uiState.update { current ->
                        current.copy(
                            isLoadingReports = false,
                            reportsError = "Error al cargar reportes locales: ${e.message}"
                        )
                    }
                }
                return@launch
            }

            // Modo Online: Cargar EXCLUSIVAMENTE de la API (Sin Room, sin persistencia ni caché local)
            val result = orchestrator.getMyReports(user.token)
            _uiState.update { current ->
                when (result) {
                    is ReportListResult.Success -> {
                        // Actualizar Room en segundo plano
                        try {
                            reportDao.clearReports(user.username)
                            reportDao.insertReports(result.reports.map { it.toEntity(user.username) })
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        current.copy(
                            isLoadingReports = false,
                            reports = result.reports
                        )
                    }
                    is ReportListResult.Unauthorized -> {
                        current.copy(
                            isLoadingReports = false,
                            reportsError = "Sesión expirada al cargar reportes."
                        )
                    }
                    is ReportListResult.Error -> {
                        current.copy(
                            isLoadingReports = false,
                            reportsError = result.message
                        )
                    }
                }
            }
        }
    }

    private fun validate(state: ReportUiState): Boolean {
        var valid = true
        var descError: String? = null
        var addrError: String? = null
        var locError: String? = null

        if (state.description.isBlank()) {
            descError = "La descripción no puede estar vacía."
            valid = false
        }
        if (state.address.isBlank()) {
            addrError = "La dirección de referencia no puede estar vacía."
            valid = false
        }
        if (state.latitude == 0.0 || state.longitude == 0.0) {
            locError = "Debe registrar una ubicación (GPS o mapa)."
            valid = false
        }

        if (!valid) {
            _uiState.update {
                it.copy(
                    descriptionError = descError,
                    addressError = addrError,
                    locationError = locError
                )
            }
        }
        return valid
    }

    private fun uriToBase64(context: Context, uri: Uri): SelectedImage? {
        return try {
            val contentType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                val base64String = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                SelectedImage(uri, contentType, base64String)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

class ReportViewModelFactory(
    private val user: User,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            val database = AppDatabase.getDatabase(context)
            @Suppress("UNCHECKED_CAST")
            return ReportViewModel(user, database.reportDao()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
