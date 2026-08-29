package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InitialData
import com.example.ui.theme.*

@Composable
fun TopHeader(
    selectedMonth: Int,
    selectedYear: Int,
    isGoogleConnected: Boolean,
    currentThemeMode: AppThemeMode,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthYearSelected: (Int, Int) -> Unit,
    onOpenGoogleSync: () -> Unit,
    onOpenNewTransaction: () -> Unit
) {
    var showMonthPicker by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: App Title, Theme Switcher, Google Sheets Sync Button, Quick Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Emerald600, Color(0xFF0F766E))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Controle Financeiro",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.3).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Gestão Pessoal & 2026",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Theme Switcher Button
                    Box {
                        IconButton(
                            onClick = { showThemeMenu = true },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("theme_switcher_button")
                        ) {
                            Icon(
                                imageVector = when (currentThemeMode) {
                                    AppThemeMode.SEPIA -> Icons.Default.AutoStories
                                    AppThemeMode.DARK -> Icons.Default.DarkMode
                                    AppThemeMode.CLASSIC -> Icons.Default.LightMode
                                },
                                contentDescription = "Trocar Tema",
                                tint = when (currentThemeMode) {
                                    AppThemeMode.SEPIA -> Color(0xFFB45309)
                                    AppThemeMode.DARK -> Color(0xFF60A5FA)
                                    AppThemeMode.CLASSIC -> Emerald600
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.LightMode, contentDescription = null, tint = Emerald600, modifier = Modifier.size(16.dp))
                                        Text("Clássico (Claro)", fontWeight = if (currentThemeMode == AppThemeMode.CLASSIC) FontWeight.Bold else FontWeight.Normal)
                                    }
                                },
                                onClick = {
                                    onThemeModeChange(AppThemeMode.CLASSIC)
                                    showThemeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.AutoStories, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                                        Text("Sépia (Vintage)", fontWeight = if (currentThemeMode == AppThemeMode.SEPIA) FontWeight.Bold else FontWeight.Normal)
                                    }
                                },
                                onClick = {
                                    onThemeModeChange(AppThemeMode.SEPIA)
                                    showThemeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(16.dp))
                                        Text("Dark (Escuro)", fontWeight = if (currentThemeMode == AppThemeMode.DARK) FontWeight.Bold else FontWeight.Normal)
                                    }
                                },
                                onClick = {
                                    onThemeModeChange(AppThemeMode.DARK)
                                    showThemeMenu = false
                                }
                            )
                        }
                    }

                    // Google Sheets Sync Status Chip
                    FilledTonalButton(
                        onClick = onOpenGoogleSync,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isGoogleConnected) Emerald50 else Slate100,
                            contentColor = if (isGoogleConnected) Emerald700 else Slate600
                        ),
                        modifier = Modifier.testTag("google_sync_button")
                    ) {
                        Icon(
                            imageVector = if (isGoogleConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = "Sync Sheets",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isGoogleConnected) "Planilha" else "Conectar",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Quick Add Button
                    Button(
                        onClick = onOpenNewTransaction,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald600,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("quick_add_transaction_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Novo",
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Novo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Row 2: Month & Year Selector Navigation
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPrevMonth,
                        modifier = Modifier.size(34.dp).testTag("prev_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Mês Anterior",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showMonthPicker = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("month_selector_dropdown"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Emerald600,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${InitialData.MONTH_NAMES[selectedMonth - 1]} de $selectedYear",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMonthPicker,
                            onDismissRequest = { showMonthPicker = false }
                        ) {
                            InitialData.MONTH_NAMES.forEachIndexed { index, name ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "$name $selectedYear",
                                            fontWeight = if (index + 1 == selectedMonth) FontWeight.Bold else FontWeight.Normal,
                                            color = if (index + 1 == selectedMonth) Emerald600 else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        onMonthYearSelected(index + 1, selectedYear)
                                        showMonthPicker = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier.size(34.dp).testTag("next_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Próximo Mês",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
