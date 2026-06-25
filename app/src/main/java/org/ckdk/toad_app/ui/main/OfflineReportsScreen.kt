package org.ckdk.toad_app.ui.main

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import org.ckdk.toad_app.data.model.User
import org.ckdk.toad_app.data.network.model.ReportResponse
import org.ckdk.toad_app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineReportsScreen(
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportViewModel = viewModel(
        key = "OFFLINE_MODE",
        factory = ReportViewModelFactory(
            user = User(token = "OFFLINE_MODE", username = "Modo Offline"),
            context = LocalContext.current.applicationContext
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedReportForDetail by remember { mutableStateOf<ReportResponse?>(null) }
    var zoomImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Load local cached reports on start
    LaunchedEffect(Unit) {
        viewModel.onFetchMyReports()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.CloudOff,
                            contentDescription = null,
                            tint = LeafGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reportes Guardados",
                            color = LeafGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Volver al Login",
                            tint = LeafGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                if (uiState.isLoadingReports && uiState.reports.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LeafGreen)
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
                            text = "No hay reportes guardados",
                            style = MaterialTheme.typography.titleMedium,
                            color = SlateGray,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Los reportes guardados localmente aparecerán en esta lista.",
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

    // 1. Report Details Dialog Pop-up
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
                        verticalAlignment = Alignment.CenterVertically) {
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
                        Text(
                            text = report.address,
                            style = MaterialTheme.typography.bodyLarge,
                            color = SlateGray
                        )
                    }

                    // Coordinates
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Latitud",
                                style = MaterialTheme.typography.titleSmall,
                                color = LeafGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = String.format("%.6f", report.latitude),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SlateGray
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Longitud",
                                style = MaterialTheme.typography.titleSmall,
                                color = LeafGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = String.format("%.6f", report.longitude),
                                style = MaterialTheme.typography.bodyMedium,
                                color = SlateGray
                            )
                        }
                    }

                    // Attached Images Section
                    if (!report.images.isNullOrEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Fotos Adjuntas:",
                                style = MaterialTheme.typography.titleSmall,
                                color = LeafGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(report.images) { img ->
                                    val bitmap = remember(img.base64Data) {
                                        try {
                                            val bytes = android.util.Base64.decode(img.base64Data, android.util.Base64.DEFAULT)
                                            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { zoomImageBitmap = bitmap },
                                            contentScale = ContentScale.Crop
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

    // 2. Zoom Dialog for images
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
                    .background(Color.Black.copy(alpha = 0.9f))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { zoomImageBitmap = null }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = zoomImageBitmap!!.asImageBitmap(),
                    contentDescription = "Zoomed Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .aspectRatio(1f)
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
                        .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Cerrar Zoom",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
