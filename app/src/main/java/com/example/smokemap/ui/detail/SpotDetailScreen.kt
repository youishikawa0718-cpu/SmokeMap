package com.example.smokemap.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smokemap.domain.model.Review

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotDetailScreen(
    spotId: String,
    onBack: () -> Unit,
    viewModel: SpotDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(spotId) {
        viewModel.loadSpot(spotId)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            val result = snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "OK",
                withDismissAction = true
            )
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.reviewSubmitted) {
        if (uiState.reviewSubmitted) {
            snackbarHostState.showSnackbar("レビューを投稿しました")
            viewModel.clearReviewSubmitted()
        }
    }

    LaunchedEffect(uiState.reportSubmitted) {
        if (uiState.reportSubmitted) {
            snackbarHostState.showSnackbar("報告を送信しました。ご協力ありがとうございます。")
            viewModel.clearReportSubmitted()
        }
    }

    if (uiState.showReportDialog) {
        SpotReportDialog(
            isSubmitting = uiState.isSubmittingReport,
            onDismiss = { viewModel.dismissReportDialog() },
            onSubmit = { reason, comment -> viewModel.submitReport(reason, comment) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.spot?.name?.ifEmpty { "名称なし" } ?: "スポット詳細") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "お気に入り",
                            tint = if (uiState.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val spot = uiState.spot
        if (spot == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = uiState.error ?: "スポットが見つかりません",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.retryLoadSpot() }) {
                    Text("再試行")
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // カテゴリ・距離・評価チップ
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(spot.category.displayName) })
                if (spot.distanceMeters != null) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                if (spot.distanceMeters < 1000) "${spot.distanceMeters.toInt()}m"
                                else "${"%.1f".format(spot.distanceMeters / 1000)}km"
                            )
                        }
                    )
                }
                if ((spot.avgRating ?: 0.0) > 0) {
                    AssistChip(
                        onClick = {},
                        label = { Text("★ ${"%.1f".format(spot.avgRating)}") }
                    )
                }
            }

            // 説明
            if (spot.description != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = spot.description,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // ナビボタン
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val uri = Uri.parse("google.navigation:q=${spot.latitude},${spot.longitude}&mode=w")
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        val browserUri = Uri.parse(
                            "https://www.google.com/maps/dir/?api=1&destination=${spot.latitude},${spot.longitude}&travelmode=walking"
                        )
                        context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ここへナビ（徒歩）")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { viewModel.showReportDialog() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("この情報を報告する")
            }

            // レビュー投稿フォーム
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "レビューを書く",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 星評価
            Row {
                (1..5).forEach { star ->
                    Icon(
                        imageVector = if (star <= uiState.reviewRating) Icons.Default.Star else Icons.Outlined.Star,
                        contentDescription = "$star 星",
                        tint = if (star <= uiState.reviewRating) Color(0xFFFFC107) else Color.Gray,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { viewModel.onReviewRatingChanged(star) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.reviewComment,
                onValueChange = { viewModel.onReviewCommentChanged(it) },
                label = { Text("コメント（任意）") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.submitReview() },
                enabled = !uiState.isSubmittingReview && uiState.reviewRating > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSubmittingReview) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("レビューを投稿")
                }
            }

            // レビュー一覧
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "レビュー（${uiState.reviews.size}件）",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.reviews.isEmpty()) {
                Text(
                    text = "まだレビューはありません",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                uiState.reviews.forEach { review ->
                    ReviewItem(
                        review = review,
                        isOwn = review.userId == uiState.deviceId,
                        onDelete = { viewModel.deleteReview(review.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ReviewItem(
    review: Review,
    isOwn: Boolean = false,
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = if (star <= review.rating) Icons.Default.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = if (star <= review.rating) Color(0xFFFFC107) else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = review.userName ?: "匿名",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (review.comment != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = review.comment,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            if (isOwn) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "削除",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SpotReportDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (reason: String, comment: String?) -> Unit
) {
    val reasons = listOf("閉鎖・撤去された", "場所が間違っている", "情報が不正確", "その他")
    var selectedReason by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("スポットを報告") },
        text = {
            Column {
                Text(
                    text = "報告理由を選択してください",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(reason, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("詳細（任意）") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedReason, comment.ifBlank { null }) },
                enabled = selectedReason.isNotEmpty() && !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("送信")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("キャンセル")
            }
        }
    )
}
