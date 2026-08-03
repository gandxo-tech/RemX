package com.example.ui.screens

import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.Reel
import com.example.ui.components.GradientButton
import com.example.ui.theme.RemXGradientBrush
import com.example.ui.components.RemXHeaderDivider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onNavigateToDetail: (String) -> Unit,
    feedViewModel: FeedViewModel = viewModel()
) {
    val feedState by feedViewModel.feedState.collectAsState()
    val searchQuery by feedViewModel.searchQuery.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "RemX • Mes Souvenirs",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { feedViewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    placeholder = { Text("Rechercher par caption ou thème...") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Rechercher",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { feedViewModel.onSearchQueryChange("") }) {
                                Icon(
                                    Icons.Filled.Clear,
                                    contentDescription = "Effacer la recherche",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                RemXHeaderDivider()
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Confier un Reel")
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    delay(800)
                    isRefreshing = false
                }
            },
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            when (val state = feedState) {
                is FeedState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is FeedState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is FeedState.Success -> {
                    if (state.reels.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (searchQuery.isNotBlank()) {
                                    Text(
                                        text = "Aucun souvenir ne correspond à « $searchQuery »",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Essaye avec un autre mot-clé de la description ou du thème.",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { feedViewModel.onSearchQueryChange("") },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Réinitialiser la recherche")
                                    }
                                } else {
                                    Text(
                                        text = "Aucun souvenir enregistré pour l'instant.",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Partage un reel depuis Instagram vers RemX ou colle son lien ici. Je le garderai en mémoire avec toutes ses infos utiles !",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    GradientButton(onClick = { showAddDialog = true }) {
                                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Confier un premier reel", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalItemSpacing = 16.dp,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(state.reels, key = { _, reel -> reel.id }) { index, reel ->
                                val visibleState = remember(reel.id) { MutableTransitionState(false) }.apply { targetState = true }
                                val delayMs = (index * 60).coerceAtMost(300)

                                AnimatedVisibility(
                                    visibleState = visibleState,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 350, delayMillis = delayMs)) +
                                            slideInVertically(
                                                initialOffsetY = { it / 3 },
                                                animationSpec = tween(durationMillis = 350, delayMillis = delayMs)
                                            )
                                ) {
                                    val dismissState = rememberSwipeToDismissBoxState(
                                        confirmValueChange = { dismissValue ->
                                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                                feedViewModel.deleteReel(reel)
                                                true
                                            } else {
                                                false
                                            }
                                        }
                                    )

                                    SwipeToDismissBox(
                                        state = dismissState,
                                        backgroundContent = {
                                            val color = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                                MaterialTheme.colorScheme.errorContainer
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(color, RoundedCornerShape(16.dp))
                                                    .padding(horizontal = 20.dp),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                                    Icon(
                                                        Icons.Filled.Delete,
                                                        contentDescription = "Oublier ce souvenir",
                                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                }
                                            }
                                        },
                                        enableDismissFromStartToEnd = false
                                    ) {
                                        ReelItem(reel = reel, onClick = { onNavigateToDetail(reel.id) })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (showAddDialog) {
            AddReelDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { url ->
                    feedViewModel.addReel(url)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ReelItem(reel: Reel, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Thumbnail format dynamic pour le staggered grid
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f) // Ratio vertical style image
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                if (reel.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = reel.thumbnailUrl,
                        contentDescription = "Miniature vidéo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = if (reel.status == "error") Icons.Filled.Error else Icons.Filled.HourglassEmpty,
                        contentDescription = "En attente",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Context & Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (reel.author.isNotEmpty()) {
                    Text(
                        text = "@${reel.author}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (reel.status == "pending") {
                    Text(
                        text = "Analyse en cours...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                } else if (reel.status == "error") {
                    Text(
                        text = "Erreur",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Text(
                    text = reel.caption.takeIf { it.isNotEmpty() } ?: if (reel.status == "pending") "En cours..." else "Sans description",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (reel.themes.isNotEmpty()) {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        reel.themes.take(2).forEach { theme ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(theme, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp) },
                                modifier = Modifier.height(24.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddReelDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var url by remember { mutableStateOf("") }
    
    AlertDialog(
        shape = RoundedCornerShape(20.dp),
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Confier un Reel",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Colle le lien Instagram d'un reel pour que je l'ajoute à ta mémoire.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Lien Instagram") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (url.isNotBlank()) {
                        onAdd(url.trim())
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Se souvenir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
