package com.wellness

import android.os.Bundle
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.wellness.R

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View? {
        return ComposeView(requireContext()).apply {
            setContent {
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current

    // Habit completion
    val prefs = remember { PrefsRepository(context) }
    val habits = remember { prefs.loadHabits() }
    val completedCount = habits.count { it.completed }
    val targetProgress = if (habits.isNotEmpty()) completedCount.toFloat() / habits.size else 0f

    // Animate progress
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1000)
    )

    // Page gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF0F8FF), Color(0xFFE0F7FA))
                )
            )
            .padding(horizontal = 16.dp, vertical = 64.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Habit progress card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .shadow(8.dp, RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFB3E5FC), Color(0xFF81D4FA))
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Habit Progress",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF01579B)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    LinearProgressIndicator(
                        progress = animatedProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        color = Color(0xFF4FC3F7),
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF01579B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Vertical navigation cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                VerticalNavigationCard(
                    title = "Habits",
                    imageRes = R.drawable.ic_habits,
                    startColor = Color(0xFFFFCDD2),
                    endColor = Color(0xFFEF9A9A),
                    onClick = {
                        val bottomNav =
                            (context as? androidx.fragment.app.FragmentActivity)
                                ?.findViewById<BottomNavigationView>(R.id.bottomNav)
                        bottomNav?.selectedItemId = R.id.nav_habits
                    }
                )

                VerticalNavigationCard(
                    title = "Mood",
                    imageRes = R.drawable.ic_mood,
                    startColor = Color(0xFFBBDEFB),
                    endColor = Color(0xFF90CAF9),
                    onClick = {
                        val bottomNav =
                            (context as? androidx.fragment.app.FragmentActivity)
                                ?.findViewById<BottomNavigationView>(R.id.bottomNav)
                        bottomNav?.selectedItemId = R.id.nav_mood
                    }
                )

                VerticalNavigationCard(
                    title = "Hydration",
                    imageRes = R.drawable.ic_water_drop,
                    startColor = Color(0xFFC8E6C9),
                    endColor = Color(0xFFA5D6A7),
                    onClick = {
                        val bottomNav =
                            (context as? androidx.fragment.app.FragmentActivity)
                                ?.findViewById<BottomNavigationView>(R.id.bottomNav)
                        bottomNav?.selectedItemId = R.id.nav_settings
                    }
                )
            }
        }
    }
}

@Composable
fun VerticalNavigationCard(
    title: String,
    imageRes: Int,
    startColor: Color,
    endColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .shadow(10.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = startColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .padding(16.dp)
                .background(
                    Brush.horizontalGradient(listOf(startColor, endColor))
                )
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
