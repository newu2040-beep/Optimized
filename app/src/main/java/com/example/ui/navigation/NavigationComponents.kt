package com.example.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.OptimizedIcons

enum class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    HOME("home", "Home", OptimizedIcons.Home),
    PROJECTS("projects", "Projects", OptimizedIcons.Video),
    TOOLS("tools", "Tools", OptimizedIcons.Tools),
    HISTORY("history", "History", OptimizedIcons.History),
    SETTINGS("settings", "Settings", OptimizedIcons.Settings)
}

@Composable
fun FloatingBottomBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val compact = com.example.ui.util.LocalCompactConfig.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = if (compact.isCompact) 10.dp else 20.dp,
                vertical = if (compact.isCompact) 6.dp else 12.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = if (compact.isCompact) 10.dp else 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    ambientColor = Color.Black.copy(alpha = 0.2f)
                ),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            tonalElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        horizontal = if (compact.isCompact) 4.dp else 8.dp,
                        vertical = if (compact.isCompact) 4.dp else 6.dp
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Screen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "iconColor"
                    )

                    val pillBgColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else Color.Transparent,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "pillBgColor"
                    )

                    Box(
                        modifier = Modifier
                            .testTag("nav_${screen.route}")
                            .clip(RoundedCornerShape(16.dp))
                            .background(pillBgColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onScreenSelected(screen)
                            }
                            .padding(
                                horizontal = if (compact.isCompact) 8.dp else 12.dp,
                                vertical = if (compact.isCompact) 4.dp else 8.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = iconColor,
                                modifier = Modifier.size(if (compact.isCompact) 18.dp else 22.dp)
                            )
                            Text(
                                text = screen.title,
                                color = iconColor,
                                fontSize = if (compact.isCompact) 9.sp else 10.sp,
                                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
