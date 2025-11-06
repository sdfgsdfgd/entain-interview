package com.entain.nextraces.races.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.entain.nextraces.model.RaceCategory
import com.entain.nextraces.races.R
import com.entain.nextraces.races.presentation.RaceListItem
import com.entain.nextraces.races.presentation.RacesUiState
import com.entain.nextraces.races.presentation.RacesViewModel
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun RaceListRoute(
    modifier: Modifier = Modifier,
    viewModel: RacesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val messageText = state.message?.let { stringResource(id = it.messageRes) }
    LaunchedEffect(messageText) {
        messageText?.let {
            snackbarHostState.showSnackbar(message = it)
            viewModel.onMessageShown()
        }
    }

    RaceListScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onFilterToggle = viewModel::onFilterToggled,
        onClearFilters = viewModel::onClearFilters,
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RaceListScreen(
    state: RacesUiState,
    snackbarHostState: SnackbarHostState,
    onFilterToggle: (RaceCategory) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = com.entain.nextraces.designsystem.R.drawable.bg_racetrack),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC050914),
                            Color(0x88050914),
                            Color(0xCC050914)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            modifier = Modifier.fillMaxSize()
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxSize()
            ) {
                var indicatorVisible by remember { mutableStateOf(false) }
                LaunchedEffect(state.isRefreshing) {
                    if (state.isRefreshing) {
                        indicatorVisible = true
                    } else {
                        delay(700)
                        indicatorVisible = false
                    }
                }

                Text(
                    text = stringResource(id = R.string.race_list_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                state.lastUpdated?.let { lastUpdated ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            id = R.string.race_last_updated,
                            formatLastUpdated(lastUpdated)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val allSelected = state.selectedFilters.isEmpty()
                    AssistChip(
                        onClick = onClearFilters,
                        shape = RoundedCornerShape(24.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (allSelected) {
                                colorResource(id = com.entain.nextraces.designsystem.R.color.chip_background_selected)
                            } else {
                                Color.Transparent
                            },
                            labelColor = if (allSelected) {
                                colorResource(id = com.entain.nextraces.designsystem.R.color.chip_text_selected)
                            } else {
                                colorResource(id = com.entain.nextraces.designsystem.R.color.chip_text)
                            }
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = colorResource(id = com.entain.nextraces.designsystem.R.color.chip_border)
                        ),
                        label = { Text(text = stringResource(id = R.string.filters_all)) }
                    )
                    RaceCategory.entries.forEach { category ->
                        val selected = category in state.selectedFilters
                        FilterChip(
                            selected = selected,
                            onClick = { onFilterToggle(category) },
                            shape = RoundedCornerShape(24.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.Transparent,
                                selectedContainerColor = colorResource(id = com.entain.nextraces.designsystem.R.color.chip_background_selected),
                                labelColor = colorResource(id = com.entain.nextraces.designsystem.R.color.chip_text),
                                selectedLabelColor = colorResource(id = com.entain.nextraces.designsystem.R.color.chip_text_selected)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = colorResource(id = com.entain.nextraces.designsystem.R.color.chip_border)
                            ),
                            label = { Text(text = stringResource(id = category.labelRes())) }
                        )
                    }
                }
                AnimatedVisibility(
                    visible = indicatorVisible,
                    enter = fadeIn(animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)) + expandVertically(
                        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                        expandFrom = Alignment.Top
                    ),
                    exit = fadeOut(animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)) + shrinkVertically(
                        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                        shrinkTowards = Alignment.Top
                    )
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0x66050914),
                                    Color.Transparent
                                )
                            )
                        )
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .animateContentSize()
                            .fillMaxSize()
                            .padding(top = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            start = 4.dp,
                            end = 4.dp,
                            bottom = WindowInsets.navigationBars
                                .asPaddingValues()
                                .calculateBottomPadding() + 12.dp
                        )
                    ) {
                        items(
                            items = state.items,
                            key = { it.key }
                        ) { item ->
                            when (item) {
                                is RaceListItem.RaceCard -> RaceCard(
                                    item = item,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp)
                                        .animateItem(
                                            fadeInSpec = tween(
                                                durationMillis = 450,
                                                easing = FastOutSlowInEasing
                                            ),
                                            placementSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            ),
                                            fadeOutSpec = tween(
                                                durationMillis = 350,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                )

                                is RaceListItem.Placeholder -> RacePlaceholder(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp)
                                        .animateItem(
                                            fadeInSpec = tween(
                                                durationMillis = 450,
                                                easing = FastOutSlowInEasing
                                            ),
                                            placementSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessLow
                                            ),
                                            fadeOutSpec = tween(
                                                durationMillis = 350,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = state.isLoading,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun RaceCard(
    item: RaceListItem.RaceCard,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.28f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.meetingName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = item.category.labelRes()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Race ${item.raceNumber}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.countdown.text,
                style = MaterialTheme.typography.titleMedium,
                color = if (item.countdown.isCritical) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun RacePlaceholder(
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .heightIn(min = 112.dp)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.race_placeholder_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.race_placeholder_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun RaceCategory.labelRes(): Int = when (this) {
    RaceCategory.HORSE -> R.string.race_category_horse
    RaceCategory.HARNESS -> R.string.race_category_harness
    RaceCategory.GREYHOUND -> R.string.race_category_greyhound
}

private fun formatLastUpdated(instant: Instant): String {
    val localTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "%02d:%02d:%02d".format(localTime.hour, localTime.minute, localTime.second)
}
