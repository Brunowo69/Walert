package com.example.walert.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SuccessSnackbar(data: SnackbarData) {
    Snackbar(
        modifier = Modifier.padding(12.dp),
        shape = RoundedCornerShape(8.dp),
        containerColor = Color(0xFF4CAF50), // Green for success
        contentColor = Color.White,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Success")
            Spacer(Modifier.width(8.dp))
            Text(data.visuals.message)
        }
    }
}
