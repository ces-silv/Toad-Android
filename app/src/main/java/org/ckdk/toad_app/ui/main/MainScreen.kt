package org.ckdk.toad_app.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.ckdk.toad_app.data.model.User
import org.ckdk.toad_app.ui.theme.AlertOrange
import org.ckdk.toad_app.ui.theme.EcoWhite
import org.ckdk.toad_app.ui.theme.LeafGreen
import org.ckdk.toad_app.ui.theme.LightGreen
import org.ckdk.toad_app.ui.theme.SlateGray

@Composable
fun MainScreen(
    user: User,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(28.dp)
        ) {
            // ── Success Icon ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(LeafGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = EcoWhite,
                    modifier = Modifier.size(52.dp)
                )
            }

            // ── Welcome Text ─────────────────────────────────────────────
            Text(
                text = "¡Bienvenido!",
                style = MaterialTheme.typography.headlineLarge,
                color = LeafGreen,
                textAlign = TextAlign.Center
            )

            Text(
                text = user.username,
                style = MaterialTheme.typography.titleMedium,
                color = SlateGray,
                textAlign = TextAlign.Center
            )

            // ── Info Card ────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Pantalla Principal",
                        style = MaterialTheme.typography.titleMedium,
                        color = LeafGreen
                    )
                    HorizontalDivider(color = LeafGreen.copy(alpha = 0.2f))
                    Text(
                        text = "Esta pantalla recibirá actualizaciones próximamente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateGray.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Logout Button ────────────────────────────────────────────
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AlertOrange
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, AlertOrange)
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
