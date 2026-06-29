package org.ckdk.toad_app.ui.main

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import org.ckdk.toad_app.data.model.User
import org.ckdk.toad_app.data.network.model.ReportResponse
import org.ckdk.toad_app.ui.main.components.MapSelectionDialog
import org.ckdk.toad_app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    user: User,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = viewModel(
        key = user.token,
        factory = ReportViewModelFactory(user, LocalContext.current.applicationContext)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    // Trigger reports load on entry
    LaunchedEffect(Unit) {
        viewModel.onFetchMyReports()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Eco,
                            contentDescription = null,
                            tint = LeafGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Toad App",
                            color = LeafGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = LeafGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { 
                        selectedTab = 0 
                        viewModel.onFetchMyReports()
                    },
                    icon = { Icon(Icons.AutoMirrored.Outlined.FormatListBulleted, contentDescription = "Mis Reportes") },
                    label = { Text("Mis Reportes") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LeafGreen,
                        selectedTextColor = LeafGreen,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray,
                        indicatorColor = LightGreen
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Outlined.Person, contentDescription = "Mi Perfil") },
                    label = { Text("Mi Perfil") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LeafGreen,
                        selectedTextColor = LeafGreen,
                        unselectedIconColor = TextGray,
                        unselectedTextColor = TextGray,
                        indicatorColor = LightGreen
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> MyReportsScreen(viewModel = viewModel, uiState = uiState)
                2 -> ProfileScreen(user = user, onLogout = onLogout)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(viewModel: ReportViewModel, uiState: ReportUiState) {
    val context = LocalContext.current
    var isCreateReportOpen by remember { mutableStateOf(false) }
    var selectedReportForDetail by remember { mutableStateOf<ReportResponse?>(null) }
    var zoomImageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    // Close the creation popup automatically on success
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            isCreateReportOpen = false
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { isCreateReportOpen = true },
                containerColor = LeafGreen,
                contentColor = EcoWhite,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Outlined.AddLocation, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nuevo Reporte", style = MaterialTheme.typography.labelLarge)
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(LightGreen, EcoWhite)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = "Historial de Reportes",
                    style = MaterialTheme.typography.titleLarge,
                    color = SlateGray,
                    fontWeight = FontWeight.Bold
                )

                // Pull to Refresh Box
                PullToRefreshBox(
                    isRefreshing = uiState.isLoadingReports,
                    onRefresh = { viewModel.onFetchMyReports() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (uiState.isLoadingReports && uiState.reports.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = LeafGreen)
                        }
                    } else if (uiState.reportsError != null) {
                        Column(
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = uiState.reportsError,
                                color = AlertOrange,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = viewModel::onFetchMyReports,
                                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen)
                            ) {
                                Text("Reintentar")
                            }
                        }
                    } else if (uiState.reports.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(LightGray, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FolderOpen,
                                    contentDescription = null,
                                    tint = TextGray,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay reportes registrados",
                                style = MaterialTheme.typography.titleMedium,
                                color = SlateGray,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Los reportes que envíes aparecerán en esta lista.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextGray,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.reports) { report ->
                                ReportCard(
                                    report = report,
                                    onClick = { selectedReportForDetail = report },
                                    onImageClick = { bitmap -> zoomImageBitmap = bitmap }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // dialog overlays

    // 1. Create Report Dialog Pop-up
    if (isCreateReportOpen) {
        Dialog(
            onDismissRequest = { isCreateReportOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EcoWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CreateReportScreenContent(
                        viewModel = viewModel,
                        uiState = uiState,
                        onClose = { isCreateReportOpen = false },
                        onImageClick = { bitmap -> zoomImageBitmap = bitmap }
                    )
                }
            }
        }
    }

    // 2. Report Details Dialog Pop-up
    if (selectedReportForDetail != null) {
        val report = selectedReportForDetail!!
        Dialog(
            onDismissRequest = { selectedReportForDetail = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Detalle del Reporte",
                            style = MaterialTheme.typography.titleLarge,
                            color = LeafGreen,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { selectedReportForDetail = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextGray)
                        }
                    }

                    HorizontalDivider(color = LightGreen)

                    // Status and Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(status = report.status)
                        Text(
                            text = "Fecha: ${formatReportDate(report.createdAt)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray
                        )
                    }

                    // Reporter Name
                    Text(
                        text = "Reportado por: ${report.reporterName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = SlateGray
                    )

                    // Description
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Descripción:",
                            style = MaterialTheme.typography.titleSmall,
                            color = LeafGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = report.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = SlateGray
                        )
                    }

                    // Reference Address
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Dirección de Referencia:",
                            style = MaterialTheme.typography.titleSmall,
                            color = LeafGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = LeafGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = report.address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SlateGray
                            )
                        }
                    }

                    // Coordinates
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Coordenadas GPS:",
                            style = MaterialTheme.typography.titleSmall,
                            color = LeafGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${report.latitude}, ${report.longitude}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray
                        )
                    }

                    // Images Grid
                    val imagesList = report.images
                    if (!imagesList.isNullOrEmpty()) {
                        Text(
                            text = "Evidencia Fotográfica (Clic para ampliar):",
                            style = MaterialTheme.typography.titleSmall,
                            color = LeafGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(imagesList) { reportImg ->
                                if (!reportImg.base64Data.isNullOrBlank()) {
                                    val bitmap = remember(reportImg.id) { base64ToBitmap(reportImg.base64Data) }
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Evidencia",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { zoomImageBitmap = bitmap }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 3. Fullscreen Image Viewer Dialog
    if (zoomImageBitmap != null) {
        Dialog(
            onDismissRequest = { zoomImageBitmap = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            var scale by remember { mutableStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { zoomImageBitmap = null }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = zoomImageBitmap!!.asImageBitmap(),
                    contentDescription = "Imagen Ampliada",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                if (scale > 1f) {
                                    offset = Offset(
                                        x = offset.x + pan.x * scale,
                                        y = offset.y + pan.y * scale
                                    )
                                } else {
                                    offset = Offset.Zero
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale > 1f) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    } else {
                                        scale = 2.5f
                                    }
                                }
                            )
                        }
                )
                IconButton(
                    onClick = { zoomImageBitmap = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CreateReportScreenContent(
    viewModel: ReportViewModel,
    uiState: ReportUiState,
    onClose: () -> Unit,
    onImageClick: (android.graphics.Bitmap) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Pick multiple images
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.onImagesSelected(context, uris)
        }
    }

    // Request GPS permissions
    val locationPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            viewModel.onFetchCurrentLocation(context)
        } else {
            Toast.makeText(context, "Se requieren permisos de ubicación para esta acción.", Toast.LENGTH_SHORT).show()
        }
    }

    // Observe Success / Error Status and notify via Toast
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null) {
            Toast.makeText(context, uiState.successMessage, Toast.LENGTH_LONG).show()
            viewModel.clearStatusMessages()
        }
        if (uiState.errorMessage != null) {
            Toast.makeText(context, uiState.errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearStatusMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ingresar Reporte",
                style = MaterialTheme.typography.titleLarge,
                color = LeafGreen,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextGray)
            }
        }

        Text(
            text = "Registra un problema de acumulación de basura en tu comunidad para que sea atendido.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray
        )

        // Form fields card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Description TextField
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChanged,
                    label = { Text("Descripción del problema") },
                    placeholder = { Text("Describe el tipo de basura, tamaño, etc...") },
                    isError = uiState.descriptionError != null,
                    supportingText = {
                        if (uiState.descriptionError != null) {
                            Text(text = uiState.descriptionError, color = AlertOrange)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LeafGreen,
                        errorBorderColor = AlertOrange
                    )
                )

                // Reference Address TextField
                OutlinedTextField(
                    value = uiState.address,
                    onValueChange = viewModel::onAddressChanged,
                    label = { Text("Dirección de referencia") },
                    placeholder = { Text("Ej. Esquina opuesta a la iglesia...") },
                    isError = uiState.addressError != null,
                    supportingText = {
                        if (uiState.addressError != null) {
                            Text(text = uiState.addressError, color = AlertOrange)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LeafGreen,
                        errorBorderColor = AlertOrange
                    )
                )
            }
        }

        // Location Selection section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Ubicación Geográfica",
                    style = MaterialTheme.typography.titleMedium,
                    color = LeafGreen
                )

                val locationRegistered = uiState.latitude != 0.0 && uiState.longitude != 0.0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (locationRegistered) LightGreen else SoftOrange)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (locationRegistered) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = if (locationRegistered) LeafGreen else AlertOrange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (locationRegistered) {
                            "Coordenadas: ${String.format("%.6f", uiState.latitude)}, ${String.format("%.6f", uiState.longitude)}"
                        } else {
                            uiState.locationError ?: "Falta asignar ubicación."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (locationRegistered) DarkGreen else AlertOrange
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            locationPermissionsLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isFetchingLocation,
                        border = BorderStroke(1.dp, LeafGreen),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LeafGreen)
                    ) {
                        if (uiState.isFetchingLocation) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = LeafGreen, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Outlined.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GPS Actual")
                        }
                    }

                    Button(
                        onClick = viewModel::openMap,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LeafGreen)
                    ) {
                        Icon(Icons.Outlined.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buscar Mapa")
                    }
                }
            }
        }

        // Photo Selection section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Evidencias Fotográficas",
                        style = MaterialTheme.typography.titleMedium,
                        color = LeafGreen
                    )
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") }
                    ) {
                        Icon(Icons.Outlined.AddAPhoto, contentDescription = "Añadir Imagen", tint = LeafGreen)
                    }
                }

                if (uiState.selectedImages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .border(1.dp, MediumGray, RoundedCornerShape(8.dp))
                            .background(LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sin fotos seleccionadas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextGray.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uiState.selectedImages.size) { index ->
                            val image = uiState.selectedImages[index]
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                val bitmap = remember(image.uri) { uriToBitmap(context, image.uri) }
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable { onImageClick(bitmap) }
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .clickable { viewModel.onRemoveImage(index) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Submit Button
        Button(
            onClick = {
                focusManager.clearFocus()
                viewModel.onSubmitReport()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LeafGreen,
                disabledContainerColor = LeafGreen.copy(alpha = 0.5f)
            ),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = EcoWhite, strokeWidth = 2.5.dp)
            } else {
                Text(
                    text = "Enviar Reporte",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }

    // Map selection modal overlay on top of dialog
    if (uiState.isMapOpen) {
        MapSelectionDialog(
            initialLatitude = uiState.latitude,
            initialLongitude = uiState.longitude,
            onDismiss = viewModel::closeMap,
            onConfirm = { lat, lng ->
                viewModel.onLocationSelected(lat, lng)
                viewModel.closeMap()
            }
        )
    }
}

@Composable
fun ReportCard(
    report: ReportResponse,
    onClick: () -> Unit,
    onImageClick: (android.graphics.Bitmap) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Status and Date Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = report.status)
                
                Text(
                    text = formatReportDate(report.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }

            // Description
            Text(
                text = report.description,
                style = MaterialTheme.typography.titleMedium,
                color = SlateGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Address & Location Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = LeafGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = report.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Images Horizontal Row
            val imagesList = report.images
            if (!imagesList.isNullOrEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    items(imagesList) { reportImg ->
                        if (!reportImg.base64Data.isNullOrBlank()) {
                            val bitmap = remember(reportImg.id) { base64ToBitmap(reportImg.base64Data) }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Evidencia",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onImageClick(bitmap) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "INGRESADO" -> Pair(Color(0xFFE0F2FE), Color(0xFF0369A1))
        "PENDIENTE" -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
        "PENDIENTE (OFFLINE)" -> Pair(Color(0xFFF3E8FF), Color(0xFF6B21A8))
        "EN_PROCESO" -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
        "RESUELTO" -> Pair(Color(0xFFD1FAE5), Color(0xFF047857))
        "RECHAZADO" -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C))
        else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
    }
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.15f))
    ) {
        Text(
            text = status,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ProfileScreen(user: User, onLogout: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(LightGreen, EcoWhite)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(28.dp)
        ) {
            // Profile icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(LeafGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = EcoWhite,
                    modifier = Modifier.size(52.dp)
                )
            }

            // User Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.headlineLarge,
                    color = LeafGreen
                )
                Text(
                    text = "Usuario Colaborador",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextGray
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Sobre Toad App",
                        style = MaterialTheme.typography.titleMedium,
                        color = LeafGreen
                    )
                    HorizontalDivider(color = LightGreen)
                    Text(
                        text = "Colabora con tu vecindario reportando vertederos de basura informales de manera directa.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray
                    )
                }
            }

            // Logout Button
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AlertOrange
                ),
                border = BorderStroke(1.dp, AlertOrange)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cerrar Sesión",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// Utility decoders for base64 / uri to bitmaps without using coil
fun base64ToBitmap(base64Str: String): android.graphics.Bitmap? {
    return try {
        val cleanBase64 = if (base64Str.startsWith("data:")) {
            base64Str.substringAfter("base64,")
        } else base64Str
        val decodedBytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
        android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun uriToBitmap(context: android.content.Context, uri: Uri): android.graphics.Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Format the ISO date string (e.g. 2026-06-25T13:13:48) into DD/MM/YYYY
fun formatReportDate(dateStr: String): String {
    return try {
        val rawDate = dateStr.substringBefore("T") // YYYY-MM-DD
        val parts = rawDate.split("-")
        if (parts.size == 3) {
            "${parts[2]}/${parts[1]}/${parts[0]}"
        } else {
            dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}
