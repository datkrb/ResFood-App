package com.muatrenthenang.resfood.ui.screens.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muatrenthenang.resfood.data.model.Review
import com.muatrenthenang.resfood.ui.viewmodel.FoodDetailViewModel
import com.muatrenthenang.resfood.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    foodId: String,
    onNavigateBack: () -> Unit,
    isReadOnly: Boolean = false,
    viewModel: FoodDetailViewModel = viewModel()
) {
    val food by viewModel.food.collectAsState()
    val reviews = food?.reviews ?: emptyList()
    val ratingHistogram by viewModel.ratingHistogram.collectAsState()
    val canReview by viewModel.canReview.collectAsState()
    var showReviewDialog by remember { mutableStateOf(false) }

    LaunchedEffect(foodId) {
        viewModel.loadFoodDetail(foodId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.review_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (canReview && !isReadOnly) {
                FloatingActionButton(
                    onClick = { showReviewDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Text(stringResource(R.string.review_add_btn), modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats
            item {
                RatingStatistics(reviews, ratingHistogram)
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                if (reviews.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.review_empty), color = Color.Gray)
                    }
                }
            }

            // Reviews
            items(reviews) { review ->
                ReviewItem(review)
            }
        }
    }

    val reviewSubmissionState by viewModel.reviewSubmissionState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val successMsg = stringResource(R.string.review_success)
    val errorMsg = stringResource(R.string.review_error)

    LaunchedEffect(reviewSubmissionState) {
        reviewSubmissionState?.let { result ->
            if (result.isSuccess) {
                android.widget.Toast.makeText(context, successMsg, android.widget.Toast.LENGTH_SHORT).show()
                showReviewDialog = false
            } else {
                android.widget.Toast.makeText(context, result.exceptionOrNull()?.message ?: errorMsg, android.widget.Toast.LENGTH_LONG).show()
            }
            viewModel.resetReviewSubmissionState()
        }
    }

    if (showReviewDialog) {
        WriteReviewDialog(
            onDismiss = { showReviewDialog = false },
            onSubmit = { star, comment ->
                viewModel.submitReview(comment, star)
            }
        )
    }
}

@Composable
fun RatingStatistics(reviews: List<Review>, histogram: Map<Int, Int>) {
    val averageRating = if (reviews.isEmpty()) 0.0 else reviews.map { it.star }.average()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Average Score
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format("%.1f", averageRating),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.review_out_of_5),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Row {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < kotlin.math.round(averageRating)) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFFC107)
                    )
                }
            }
        }

        // Histogram Bars
        Column(modifier = Modifier.weight(1f)) {
            (5 downTo 1).forEach { star ->
                val count = histogram[star] ?: 0
                val progress = if (reviews.isNotEmpty()) count.toFloat() / reviews.size else 0f

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$star",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(12.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.Gray
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp),
                        color = Color(0xFFFFC107),
                        trackColor = Color.LightGray.copy(alpha = 0.3f),
                    )
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = review.userName.ifEmpty { stringResource(R.string.review_anonymous) },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date(review.createdAt)),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Row(modifier = Modifier.padding(vertical = 4.dp)) {
            repeat(5) { index ->
                Icon(
                    imageVector = if (index < review.star) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFFFC107)
                )
            }
        }
        if (review.comment.isNotEmpty()) {
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun WriteReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.review_dialog_title)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Star Selection
                Row(horizontalArrangement = Arrangement.Center) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star }) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text(stringResource(R.string.review_hint_comment)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(rating, comment) }) {
                Text(stringResource(R.string.common_send))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
