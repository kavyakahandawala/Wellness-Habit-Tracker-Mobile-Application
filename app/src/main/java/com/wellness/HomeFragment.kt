package com.wellness

import android.os.Bundle
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.wellness.model.Habit
import com.wellness.R
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    HomeScreen()
                }
            }
        }
    }
}

// ─── Data Models ─────────────────────────────────────────────────────────────

data class MoodEntry(val day: String, val emoji: String, val color: Color)

// ─── Color Palette ───────────────────────────────────────────────────────────

object AppColors {
    val Primary = Color(0xFF6C63FF)
    val PrimaryLight = Color(0xFF8B85FF)
    val Secondary = Color(0xFF00BFA6)
    val Accent = Color(0xFFFF6584)
    val Background = Color(0xFFF8F9FE)
    val Surface = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF2D3142)
    val TextSecondary = Color(0xFF9CA3AF)
    val CardGradient1 = listOf(Color(0xFF6C63FF), Color(0xFF8B85FF))
    val CardGradient2 = listOf(Color(0xFF00BFA6), Color(0xFF5EF5D9))
    val CardGradient3 = listOf(Color(0xFFFF6584), Color(0xFFFF9EAA))
    val WaterBlue = Color(0xFF4FC3F7)
    val WaterLight = Color(0xFFB3E5FC)
}

// ─── Main Screen ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val prefs = remember { PrefsRepository(context) }

    // Load data
    val habits = remember { prefs.loadHabits() }
    val completedCount = habits.count { it.completed }
    val totalHabits = habits.size
    val progress = if (totalHabits > 0) completedCount.toFloat() / totalHabits else 0f

    // Animate progress
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
    )

    // Current date
    val currentDate = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    // Sample mood data (replace with actual data from prefs)
    val weeklyMoods = remember {
        listOf(
            MoodEntry("M", "😊", Color(0xFF4CAF50)),
            MoodEntry("T", "😐", Color(0xFFFFC107)),
            MoodEntry("W", "😊", Color(0xFF4CAF50)),
            MoodEntry("T", "😢", Color(0xFFF44336)),
            MoodEntry("F", "😊", Color(0xFF4CAF50)),
            MoodEntry("S", "😐", Color(0xFFFFC107)),
            MoodEntry("S", "😊", Color(0xFF4CAF50))
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Good Morning!",
                            fontSize = 14.sp,
                            color = AppColors.TextSecondary
                        )
                        Text(
                            text = currentDate,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Background
                ),
                actions = {
                    IconButton(onClick = { /* Profile */ }) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AppColors.Primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = AppColors.Primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            )
        },
        containerColor = AppColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ─── Hero Banner with Emoji Art ─────────────────────────────────
            HeroBanner(
                progress = animatedProgress,
                completedCount = completedCount,
                totalHabits = totalHabits
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ─── Weekly Mood Overview ──────────────────────────────────────
            WeeklyMoodCard(weeklyMoods = weeklyMoods)

            Spacer(modifier = Modifier.height(24.dp))

            // ─── Quick Actions Grid ────────────────────────────────────────
            Text(
                text = "Quick Access",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            QuickAccessGrid(
                onHabitsClick = {
                    val bottomNav = (context as? androidx.fragment.app.FragmentActivity)
                        ?.findViewById<BottomNavigationView>(R.id.bottomNav)
                    bottomNav?.selectedItemId = R.id.nav_habits
                },
                onMoodClick = {
                    val bottomNav = (context as? androidx.fragment.app.FragmentActivity)
                        ?.findViewById<BottomNavigationView>(R.id.bottomNav)
                    bottomNav?.selectedItemId = R.id.nav_mood
                },
                onHydrationClick = {
                    val bottomNav = (context as? androidx.fragment.app.FragmentActivity)
                        ?.findViewById<BottomNavigationView>(R.id.bottomNav)
                    bottomNav?.selectedItemId = R.id.nav_settings
                },
                onInsightsClick = {
                    val bottomNav = (context as? androidx.fragment.app.FragmentActivity)
                        ?.findViewById<BottomNavigationView>(R.id.bottomNav)
                    bottomNav?.selectedItemId = R.id.nav_mood
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ─── Wellness Tip Card with Emoji ────────────────────────────────
            WellnessTipCard()

            Spacer(modifier = Modifier.height(24.dp))

            // ─── Today's Habits Preview ────────────────────────────────────
            if (habits.isNotEmpty()) {
                Text(
                    text = "Today's Habits",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                habits.take(3).forEach { habit ->
                    HabitPreviewItem(habit = habit)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (habits.size > 3) {
                    TextButton(
                        onClick = {
                            val bottomNav = (context as? androidx.fragment.app.FragmentActivity)
                                ?.findViewById<BottomNavigationView>(R.id.bottomNav)
                            bottomNav?.selectedItemId = R.id.nav_habits
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "View All (${habits.size})",
                            color = AppColors.Primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─── Component: Hero Banner with Emoji Art ───────────────────────────────────

@Composable
fun HeroBanner(
    progress: Float,
    completedCount: Int,
    totalHabits: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF6C63FF), Color(0xFF8B85FF)),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
            )

            // Decorative circles
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = (-20).dp, y = (-20).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .align(Alignment.TopStart)
            )
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .offset(x = 40.dp, y = (-40).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.03f))
                    .align(Alignment.TopEnd)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Text content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Daily Progress",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "$completedCount of $totalHabits",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "habits completed",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${(progress * 100).toInt()}% completed",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                // Right: Emoji Art Circle
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.2f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🧘",
                            fontSize = 48.sp
                        )
                        Text(
                            text = "✨",
                            fontSize = 24.sp,
                            modifier = Modifier.offset(y = (-8).dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── Component: Wellness Tip Card with Emoji ─────────────────────────────────

@Composable
fun WellnessTipCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tip emoji art
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFE0B2)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌿",
                    fontSize = 40.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Daily Tip",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                Text(
                    text = "Take 5 minutes to breathe deeply and clear your mind. Small moments of mindfulness make a big difference.",
                    fontSize = 14.sp,
                    color = AppColors.TextPrimary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// ─── Component: Weekly Mood Card ─────────────────────────────────────────────

@Composable
fun WeeklyMoodCard(weeklyMoods: List<MoodEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Mood",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )

                TextButton(onClick = { /* View full mood history */ }) {
                    Text(
                        text = "See All",
                        color = AppColors.Primary,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weeklyMoods.forEach { mood ->
                    MoodDayItem(mood = mood)
                }
            }
        }
    }
}

@Composable
fun MoodDayItem(mood: MoodEntry) {
    val isToday = mood.day == "S"
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = mood.day,
            fontSize = 12.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isToday) AppColors.Primary else AppColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (isToday) AppColors.Primary.copy(alpha = 0.1f)
                    else mood.color.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mood.emoji,
                fontSize = 22.sp
            )
        }

        if (isToday) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(AppColors.Primary)
            )
        }
    }
}

// ─── Component: Quick Access Grid with Emoji Art ─────────────────────────────

@Composable
fun QuickAccessGrid(
    onHabitsClick: () -> Unit,
    onMoodClick: () -> Unit,
    onHydrationClick: () -> Unit,
    onInsightsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            title = "Habits",
            subtitle = "Track daily",
            icon = Icons.Default.CheckCircle,
            emojiArt = "✅",
            gradientColors = AppColors.CardGradient1,
            modifier = Modifier.weight(1f),
            onClick = onHabitsClick
        )

        QuickActionCard(
            title = "Mood",
            subtitle = "Log feeling",
            icon = Icons.Default.Favorite,
            emojiArt = "💜",
            gradientColors = AppColors.CardGradient3,
            modifier = Modifier.weight(1f),
            onClick = onMoodClick
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            title = "Hydration",
            subtitle = "Water intake",
            icon = Icons.Default.WaterDrop,
            emojiArt = "💧",
            gradientColors = AppColors.CardGradient2,
            modifier = Modifier.weight(1f),
            onClick = onHydrationClick
        )

        QuickActionCard(
            title = "Insights",
            subtitle = "Analytics",
            icon = Icons.Default.BarChart,
            emojiArt = "📊",
            gradientColors = listOf(Color(0xFFFFA726), Color(0xFFFFCC80)),
            modifier = Modifier.weight(1f),
            onClick = onInsightsClick
        )
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    emojiArt: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(gradientColors)
                    )
            )

            // Decorative emoji art (subtle, bottom-right)
            Text(
                text = emojiArt,
                fontSize = 64.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 10.dp)
                    .alpha(0.25f)
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// ─── Component: Habit Preview Item ─────────────────────────────────────────

@Composable
fun HabitPreviewItem(habit: Habit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (habit.completed) AppColors.Secondary.copy(alpha = 0.1f)
                            else AppColors.Primary.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (habit.completed) Icons.Default.Check else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (habit.completed) AppColors.Secondary else AppColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = habit.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        text = if (habit.completed) "Completed" else "Pending",
                        fontSize = 13.sp,
                        color = if (habit.completed) AppColors.Secondary else AppColors.TextSecondary
                    )
                }
            }

            if (habit.completed) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = AppColors.Secondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}