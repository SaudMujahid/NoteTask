package com.example.test.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.example.test.ui.utils.playCheckSound
import kotlinx.coroutines.launch

/**
 * Wraps any composable content with a swipe-right + fade-out animation
 * that fires when [checked] transitions from false → true.
 *
 * Usage:
 *   SwipeOffTaskItem(
 *       checked = task.isChecked,
 *       onCheckedChange = { taskViewModel.toggleTask(task) }
 *   ) {
 *       TaskItem(...)
 *   }
 *
 * The animation plays on check; on uncheck the row snaps back instantly.
 */
@Composable
fun SwipeOffTaskItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current

    val offsetX = remember { Animatable(0f) }
    val alpha   = remember { Animatable(1f) }

    // Track whether an animation is currently running so we don't double-fire
    var animating by remember { mutableStateOf(false) }

    // Intercept the check event — animate first, then propagate
    val handleCheck: (Boolean) -> Unit = { newChecked ->
        if (newChecked && !animating) {
            animating = true   // LaunchedEffect below will pick this up
        } else if (!newChecked) {
            // Unchecking: propagate immediately with no animation
            onCheckedChange(false)
        }
    }

    LaunchedEffect(animating) {
        if (!animating) return@LaunchedEffect

        playCheckSound(context)

        // Slide right and fade out simultaneously
        kotlinx.coroutines.coroutineScope {
            launch {
                offsetX.animateTo(600f, animationSpec = tween(durationMillis = 320))
            }
            launch {
                alpha.animateTo(0f, animationSpec = tween(durationMillis = 280))
            }
        }

        // Commit state change after animation completes
        onCheckedChange(true)

        // Reset visual state (item will be removed from list by ViewModel,
        // but reset anyway in case the list keeps it with checked = true)
        offsetX.snapTo(0f)
        alpha.snapTo(1f)
        animating = false
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { translationX = offsetX.value }
            .alpha(alpha.value)
    ) {
        content(checked, handleCheck)
    }
}