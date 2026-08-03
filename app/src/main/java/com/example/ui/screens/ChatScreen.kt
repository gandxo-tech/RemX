package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.Reel
import com.example.ui.theme.RemXGradientBrush
import com.example.ui.components.RemXHeaderDivider
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {
    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val recentQueries by viewModel.recentQueries.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val listState = rememberLazyListState()
    
    val userId = try { FirebaseAuth.getInstance().currentUser?.uid ?: "" } catch (e: Exception) { "" }
    
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "RemX • Ma Mémoire",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                )
                RemXHeaderDivider()
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Dis-moi ce que tu cherches...") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isTyping) {
                                viewModel.sendMessage(inputText, userId)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !isTyping,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Interroger ma mémoire")
                    }
                }
            }
        }
    ) { innerPadding ->
        if (messages.isEmpty()) {
            // État vide avec ton amical et complice
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Je me rappelle de tout ce qu'on a sauvegardé ensemble.",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Pose-moi n'importe quelle question sur un sujet, un reel ou une astuce qu'on a mise de côté !",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 28.dp)
                )
                
                val defaultSuggestions = listOf(
                    "Tu te souviens du reel sur la finition en 1v1 ?",
                    "Qu'est-ce qu'on avait gardé sur le pressing ?",
                    "Retrouve-moi la dernière astuce qu'on a ajoutée"
                )

                if (recentQueries.isNotEmpty()) {
                    Text(
                        text = "Questions récemment posées à ta mémoire :",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    recentQueries.forEach { recentQuery ->
                        SuggestionChip(
                            onClick = { viewModel.sendMessage(recentQuery.query, userId) },
                            label = { Text(recentQuery.query) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                } else {
                    defaultSuggestions.forEach { suggestion ->
                        SuggestionChip(
                            onClick = { viewModel.sendMessage(suggestion, userId) },
                            label = { Text(suggestion) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    val visibleState = remember { MutableTransitionState(false) }.apply { targetState = true }
                    AnimatedVisibility(
                        visibleState = visibleState,
                        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(500)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ChatBubble(
                            message = message,
                            onSaveHighlight = { viewModel.saveHighlight(it) }
                        )
                    }
                }
                
                if (isTyping) {
                    item {
                        AnimatedVisibility(
                            visible = isTyping,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            TypingIndicator()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: ChatMessage,
    onSaveHighlight: (String) -> Unit = {}
) {
    val isUser = message.role == Role.USER
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showContextMenu by remember { mutableStateOf(false) }
    var isHighlighted by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .background(
                    brush = if (isUser) RemXGradientBrush else Brush.linearGradient(
                        listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                    )
                )
                .combinedClickable(
                    onClick = {},
                    onLongClick = { showContextMenu = true }
                )
                .padding(16.dp)
        ) {
            Column {
                if (isHighlighted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Temps fort",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Temps fort",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isUser) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = message.text,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false },
                shape = RoundedCornerShape(16.dp)
            ) {
                DropdownMenuItem(
                    text = { Text("Copier la réponse") },
                    leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
                    onClick = {
                        showContextMenu = false
                        clipboardManager.setText(AnnotatedString(message.text))
                        Toast.makeText(context, "Réponse copiée dans le presse-papier", Toast.LENGTH_SHORT).show()
                    }
                )
                DropdownMenuItem(
                    text = { Text(if (isHighlighted) "Retirer des temps forts" else "Enregistrer en temps fort") },
                    leadingIcon = {
                        Icon(
                            if (isHighlighted) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = null,
                            tint = if (isHighlighted) Color(0xFFFFC107) else LocalContentColor.current
                        )
                    },
                    onClick = {
                        showContextMenu = false
                        if (!isHighlighted) {
                            onSaveHighlight(message.text)
                        }
                        isHighlighted = !isHighlighted
                        val toastText = if (isHighlighted) "Enregistré dans tes temps forts !" else "Retiré des temps forts"
                        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
        
        if (!isUser && message.referencedReels.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Voici les souvenirs associés :",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                message.referencedReels.forEach { reel ->
                    ReelMiniCard(reel = reel, onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reel.url))
                        context.startActivity(intent)
                    })
                }
            }
        }
    }
}

@Composable
fun ReelMiniCard(reel: Reel, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp) // Ratio proche 9:16 pour la miniature
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = reel.thumbnailUrl.ifEmpty { "https://via.placeholder.com/150" },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(
                text = reel.author.ifEmpty { "Créateur" },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 6.dp)
            )
            Text(
                text = reel.caption.ifEmpty { "Reel sauvegardé" },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = 4.dp,
                    bottomEnd = 20.dp
                )
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val dotModifier = Modifier
            .size(8.dp)
            .clip(androidx.compose.foundation.shape.CircleShape)

        Box(modifier = dotModifier.background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = dot1Alpha)))
        Box(modifier = dotModifier.background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = dot2Alpha)))
        Box(modifier = dotModifier.background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = dot3Alpha)))
        
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Recherche dans nos souvenirs…",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
