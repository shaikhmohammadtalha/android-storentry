package com.shaikh.storentry.presentation.screens.help

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shaikh.storentry.R
import com.shaikh.storentry.presentation.components.AppButton
import com.shaikh.storentry.presentation.components.AppCard
import com.shaikh.storentry.presentation.components.AppSecondaryButton
import com.shaikh.storentry.presentation.components.AppTopBar

/**
 * HelpSupportScreen — Beautiful FAQ layout with expandable cards and active contact actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // State to track which FAQ item is expanded (-1 means none)
    var expandedIndex by remember { mutableStateOf(-1) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(id = R.string.help_support),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            // Header Hero Banner
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QuestionAnswer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.faq_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Get answers instantly",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // FAQs List Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // FAQ 1
                FaqItemCard(
                    question = stringResource(id = R.string.faq_1_question),
                    answer = stringResource(id = R.string.faq_1_answer),
                    isExpanded = expandedIndex == 0,
                    onToggle = { expandedIndex = if (expandedIndex == 0) -1 else 0 }
                )

                // FAQ 2
                FaqItemCard(
                    question = stringResource(id = R.string.faq_2_question),
                    answer = stringResource(id = R.string.faq_2_answer),
                    isExpanded = expandedIndex == 1,
                    onToggle = { expandedIndex = if (expandedIndex == 1) -1 else 1 }
                )

                // FAQ 3
                FaqItemCard(
                    question = stringResource(id = R.string.faq_3_question),
                    answer = stringResource(id = R.string.faq_3_answer),
                    isExpanded = expandedIndex == 2,
                    onToggle = { expandedIndex = if (expandedIndex == 2) -1 else 2 }
                )

                // FAQ 4
                FaqItemCard(
                    question = stringResource(id = R.string.faq_4_question),
                    answer = stringResource(id = R.string.faq_4_answer),
                    isExpanded = expandedIndex == 3,
                    onToggle = { expandedIndex = if (expandedIndex == 3) -1 else 3 }
                )
            }

            // Contact Support Card
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.contact_support),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = stringResource(id = R.string.contact_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Email Support Button
                    val supportEmail = stringResource(id = R.string.support_email_address)
                    AppButton(
                        text = stringResource(id = R.string.btn_email_support),
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:")
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail))
                                    putExtra(Intent.EXTRA_SUBJECT, "Storentry Support Request")
                                    putExtra(Intent.EXTRA_TEXT, "Hello Storentry Support Team,\n\n")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No email client found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    // WhatsApp Support Button
                    val whatsappNumber = stringResource(id = R.string.support_whatsapp_number)
                    AppSecondaryButton(
                        text = stringResource(id = R.string.btn_whatsapp_support),
                        onClick = {
                            try {
                                val url = "https://api.whatsapp.com/send?phone=$whatsappNumber&text=Hello%20Storentry%20Team!%20I%20need%20assistance%20with%20"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Expandable FAQ card helper.
 */
@Composable
fun FaqItemCard(
    question: String,
    answer: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 90f else 0f)

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = question,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotationState)
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
