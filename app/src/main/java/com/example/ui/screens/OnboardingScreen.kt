package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GradientButton
import com.example.ui.components.LegalInfoModal
import com.example.ui.components.LegalModalType
import kotlinx.coroutines.launch

data class OnboardingSlide(
    val id: Int,
    val title: String,
    val subtitle: String,
    val hookTag: String,
    val icon: ImageVector,
    val gradientColors: List<Color>,
    val detailFeatureText: String,
    val stepBadgeText: String? = null
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onRegister: (String, String, (Boolean, String?) -> Unit) -> Unit,
    onLogin: (String, String, (Boolean, String?) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showAuthForm by remember { mutableStateOf(false) }
    var activeLegalModal by remember { mutableStateOf<LegalModalType?>(null) }

    val slides = remember {
        listOf(
            OnboardingSlide(
                id = 1,
                title = "Votre Second Cerveau Visuel",
                subtitle = "Transformez vos Instagram Reels en une bibliothèque de connaissances structurée et consultable à tout moment.",
                hookTag = "LE CONCEPT REMX",
                icon = Icons.Filled.Psychology,
                gradientColors = listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2)),
                detailFeatureText = "Ne laissez plus jamais s'effacer une recette, un conseil de voyage ou un tuto croisé sur Instagram.",
                stepBadgeText = "CONCEPT"
            ),
            OnboardingSlide(
                id = 2,
                title = "Tutoriel : Partager un Reel",
                subtitle = "Depuis l'application Instagram, appuyez sur le bouton Partager ↗ sous le Reel, puis sélectionnez 'RemX' dans la liste.",
                hookTag = "ÉTAPE 1 • PARTAGE INSTAGRAM",
                icon = Icons.Filled.Share,
                gradientColors = listOf(Color(0xFF833AB4), Color(0xFFFD1D1D), Color(0xFFFCB045)),
                detailFeatureText = "Vous pouvez aussi simplement copier le lien du Reel et le coller directement dans RemX.",
                stepBadgeText = "ÉTAPE 1 / 3"
            ),
            OnboardingSlide(
                id = 3,
                title = "Analyse & Résumé Gemini AI",
                subtitle = "L'IA Gemini analyse automatiquement le contenu, extrait les étapes clés et vous envoie une notification dès que la fiche est prête.",
                hookTag = "ÉTAPE 2 • TRAITEMENT IA & NOTIF",
                icon = Icons.Filled.AutoAwesome,
                gradientColors = listOf(Color(0xFFFF416C), Color(0xFFFF4B2B)),
                detailFeatureText = "Visualisez le résumé en 1 ligne et les mots-clés thématiques sans revoir toute la vidéo.",
                stepBadgeText = "ÉTAPE 2 / 3"
            ),
            OnboardingSlide(
                id = 4,
                title = "Recherche & Chat Intelligent",
                subtitle = "Retrouvez vos souvenirs en posant des questions naturelles comme dans un chat WhatsApp : 'Retrouve ma recette de pâtes !'",
                hookTag = "ÉTAPE 3 • RETROUVEZ TOUT",
                icon = Icons.AutoMirrored.Filled.Chat,
                gradientColors = listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
                detailFeatureText = "Vos souvenirs et votre compte restent stockés à 100% sur votre appareil Android.",
                stepBadgeText = "ÉTAPE 3 / 3"
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { slides.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("R", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RemX", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                actions = {
                    if (!showAuthForm) {
                        TextButton(onClick = { showAuthForm = true }) {
                            Text("Passer", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        TextButton(onClick = { showAuthForm = false }) {
                            Text("Découverte", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = showAuthForm,
                transitionSpec = {
                    fadeIn() + slideInHorizontally { it } togetherWith fadeOut() + slideOutHorizontally { -it }
                },
                label = "OnboardingSwitch"
            ) { isAuth ->
                if (!isAuth) {
                    // PAGER SLIDES VIEW
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) { page ->
                            OnboardingSlideItem(slide = slides[page])
                        }

                        // Bottom Controls
                        val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 1.0f,
                            targetValue = 1.05f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(900, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "PulseScale"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Dots Indicator
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(slides.size) { index ->
                                    val isSelected = pagerState.currentPage == index
                                    Box(
                                        modifier = Modifier
                                            .height(8.dp)
                                            .width(if (isSelected) 24.dp else 8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outlineVariant
                                            )
                                    )
                                }
                            }

                            // Next / Start Button
                            if (pagerState.currentPage < slides.size - 1) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Suivant")
                                    Spacer(Modifier.width(6.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            } else {
                                GradientButton(
                                    onClick = { showAuthForm = true },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.scale(pulseScale)
                                ) {
                                    Text("Terminer & Commencer ✨", color = Color.White, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(6.dp))
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                } else {
                    // WHATSAPP-STYLE AUTH / REGISTER FORM
                    WhatsAppAuthForm(
                        onRegister = onRegister,
                        onLogin = onLogin,
                        onOpenLegal = { type -> activeLegalModal = type }
                    )
                }
            }
        }

        // Active Legal Modal if selected
        activeLegalModal?.let { modalType ->
            LegalInfoModal(
                type = modalType,
                onDismiss = { activeLegalModal = null }
            )
        }
    }
}

@Composable
fun OnboardingSlideItem(slide: OnboardingSlide) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Hero visual card with gradient and dynamic diagram
        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(slide.gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    slide.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = slide.hookTag,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Visual Tutorial Step Diagram Card
                    SlideTutorialDiagram(slideId = slide.id)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = slide.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = slide.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = slide.detailFeatureText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SlideTutorialDiagram(slideId: Int) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.18f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            when (slideId) {
                1 -> {
                    // Concept Diagram
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.VideoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            Text("Reels Instagram", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Psychology, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(32.dp))
                            Text("Second Cerveau", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                            Text("Savoirs IA", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                2 -> {
                    // Share Tutorial Step Diagram
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.3f)) {
                                Text("1", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Text("Sur Instagram : Appuyez sur Partager ↗", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.3f)) {
                                Text("2", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Text("Dans la liste des apps : Choisissez RemX 📱", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                3 -> {
                    // Gemini AI Processing Diagram
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Link, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            Text("Lien partagé", fontSize = 11.sp, color = Color.White)
                        }
                        Icon(Icons.Filled.Bolt, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            Text("Gemini Résume", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = Color(0xFF64FFDA), modifier = Modifier.size(24.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔔 Notif Push", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                4 -> {
                    // Chat Assistant Diagram
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("💬 'Quelle est la recette de pâtes ?'", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.25f),
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Text("🤖 'Voici le Reel du chef @mario avec 4 étapes !'", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppAuthForm(
    onRegister: (String, String, (Boolean, String?) -> Unit) -> Unit,
    onLogin: (String, String, (Boolean, String?) -> Unit) -> Unit,
    onOpenLegal: (LegalModalType) -> Unit
) {
    val context = LocalContext.current
    var isRegisterMode by remember { mutableStateOf(true) }

    // Form fields
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var acceptTerms by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // WhatsApp Style Header Icon
        Surface(
            shape = CircleShape,
            color = Color(0xFF25D366).copy(alpha = 0.15f),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = "Protection RemX",
                    tint = Color(0xFF25D366),
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = if (isRegisterMode) "Créer votre compte RemX" else "Connexion à votre espace",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = if (isRegisterMode) "Entrez un pseudo et un mot de passe pour sécuriser votre compte local" else "Entrez votre pseudo et mot de passe pour déverrouiller vos souvenirs",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Mode Switcher Tabs
        PrimaryTabRow(
            selectedTabIndex = if (isRegisterMode) 0 else 1,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        ) {
            Tab(
                selected = isRegisterMode,
                onClick = { 
                    isRegisterMode = true
                    errorMessage = null 
                },
                text = { Text("Inscription", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = !isRegisterMode,
                onClick = { 
                    isRegisterMode = false
                    errorMessage = null 
                },
                text = { Text("Se connecter", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(Modifier.height(20.dp))

        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Filled.Error, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Form Fields
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Pseudo field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Pseudo") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe (4+ caractères)") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Afficher mot de passe"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (isRegisterMode) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmer le mot de passe") },
                    leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Terms Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = acceptTerms,
                        onCheckedChange = { acceptTerms = it }
                    )
                    Text(
                        text = "J'accepte les ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Mentions Légales",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onOpenLegal(LegalModalType.MentionsLegales) }
                    )
                    Text(
                        text = " et la ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Confidentialité",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onOpenLegal(LegalModalType.PolitiqueConfidentialite) }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Submit Button
        GradientButton(
            onClick = {
                errorMessage = null
                if (isRegisterMode) {
                    if (name.isBlank()) {
                        errorMessage = "Veuillez entrer votre pseudo."
                        return@GradientButton
                    }
                    if (password.length < 4) {
                        errorMessage = "Le mot de passe doit contenir au moins 4 caractères."
                        return@GradientButton
                    }
                    if (password != confirmPassword) {
                        errorMessage = "Les mots de passe ne correspondent pas."
                        return@GradientButton
                    }
                    if (!acceptTerms) {
                        errorMessage = "Vous devez accepter les conditions pour continuer."
                        return@GradientButton
                    }

                    isLoading = true
                    onRegister(name.trim(), password.trim()) { success, err ->
                        isLoading = false
                        if (!success) {
                            errorMessage = err ?: "Erreur lors de la création du compte."
                        } else {
                            Toast.makeText(context, "Compte créé avec succès !", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    if (name.isBlank() || password.isBlank()) {
                        errorMessage = "Veuillez remplir tous les champs."
                        return@GradientButton
                    }
                    isLoading = true
                    onLogin(name.trim(), password.trim()) { success, err ->
                        isLoading = false
                        if (!success) {
                            errorMessage = err ?: "Erreur de connexion."
                        } else {
                            Toast.makeText(context, "Bienvenue de retour !", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(
                    if (isRegisterMode) Icons.Filled.AccountCircle else Icons.Filled.Login,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isRegisterMode) "Créer mon compte" else "Se connecter",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Legal Links Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { onOpenLegal(LegalModalType.MentionsLegales) }) {
                Text("Mentions Légales", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            }
            Text("•", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            TextButton(onClick = { onOpenLegal(LegalModalType.PolitiqueConfidentialite) }) {
                Text("Confidentialité", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            }
            Text("•", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            TextButton(onClick = { onOpenLegal(LegalModalType.APropos) }) {
                Text("À Propos", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
