package com.shaikh.storentry.presentation.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shaikh.storentry.R
import com.shaikh.storentry.presentation.components.AppButton
import kotlinx.coroutines.launch

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

/**
 * OnboardingScreen — shown after Welcome.
 * Uses a HorizontalPager to cycle through three onboarding pages.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onNavigateToHome: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val onComplete = {
        viewModel.completeOnboarding()
        onNavigateToHome()
    }
    val pages = listOf(
        OnboardingPage(
            titleRes = R.string.onboarding_title,
            subtitleRes = R.string.onboarding_subtitle,
            imageRes = R.drawable.onboarding_hero
        ),
        OnboardingPage(
            titleRes = R.string.onboarding_offline_title,
            subtitleRes = R.string.onboarding_offline_subtitle,
            imageRes = R.drawable.onboarding_offline
        ),
        OnboardingPage(
            titleRes = R.string.onboarding_alerts_title,
            subtitleRes = R.string.onboarding_alerts_subtitle,
            imageRes = R.drawable.onboarding_privacy
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        // Skip button (Only show if not on last page)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            AnimatedVisibility(
                visible = pagerState.currentPage < pages.size - 1,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TextButton(onClick = onComplete) {
                    Text(
                        text = stringResource(id = R.string.skip),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top
        ) { index ->
            OnboardingPageContent(page = pages[index])
        }

        // Pagination and CTA Footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pagination dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isActive = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .width(if (isActive) 24.dp else 8.dp)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // CTA Button
            AppButton(
                text = if (pagerState.currentPage == pages.size - 1) 
                    stringResource(id = R.string.get_started) 
                else 
                    stringResource(id = R.string.next),
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero illustration
        Box(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(28.dp))
        ) {
            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Title
        Text(
            text = stringResource(id = page.titleRes),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle
        Text(
            text = stringResource(id = page.subtitleRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
        )
    }
}

private data class OnboardingPage(
    val titleRes: Int,
    val subtitleRes: Int,
    val imageRes: Int
)
