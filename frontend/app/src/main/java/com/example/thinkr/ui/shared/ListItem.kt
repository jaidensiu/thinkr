package com.example.thinkr.ui.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thinkr.R
import com.example.thinkr.data.models.DocumentItem
import com.example.thinkr.ui.home.HomeScreenAction


@Composable
fun ListItem(item: DocumentItem, onAction: (HomeScreenAction) -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = item.uploadCompleted) {
                onAction(HomeScreenAction.DocumentItemClicked(documentItem = item))
            }
            .padding(vertical = 8.dp)
            .alpha(if (item.uploadCompleted) 1f else 0.5f), // Faded effect if not completed
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular logo
        Image(
            painter = painterResource(id = R.drawable.document_placeholder_logo),
            contentDescription = "Logo",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Name
        Text(text = item.name, fontSize = 18.sp, fontWeight = FontWeight.Medium)

        if (!item.uploadCompleted) {
            Spacer(modifier = Modifier.weight(1f))

            // Circular loading animation
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp)
            )
        }
    }
}