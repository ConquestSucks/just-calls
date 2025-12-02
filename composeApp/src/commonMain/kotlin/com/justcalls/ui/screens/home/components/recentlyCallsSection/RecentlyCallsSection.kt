package com.justcalls.ui.screens.home.components.recentlyCallsSection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.justcalls.ui.theme.AppColors
import justcalls.composeapp.generated.resources.Res

@Composable
fun RecentlyCallsSection() {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.padding(25.dp)) {
        Row {
            AsyncImage(
                model = Res.getUri("drawable/ic_recently_calls.svg"),
                contentDescription = "Недавние звонки",
                modifier = Modifier.size(30.dp),
            )
            Spacer(Modifier.width(13.dp))
            Text(
                text = "Недавние звонки",
                fontSize = 13.sp,
                color = AppColors.TextSecondary
            )
        }
        Spacer(Modifier.height(23.dp))
        Column(
            modifier = Modifier
                .padding(15.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            RecentlyCallsItem()
            RecentlyCallsItem()
            RecentlyCallsItem()
            RecentlyCallsItem()
        }
    }
}