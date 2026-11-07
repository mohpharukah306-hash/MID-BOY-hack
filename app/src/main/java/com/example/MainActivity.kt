package com.example

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleSaves
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.BottomInputBar
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TopBar
import com.example.ui.screens.ApkInspectorScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.FileViewerScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ScanOrbScreen
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceContainer
import com.example.ui.theme.DeepEmeraldBorder
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonMint
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        MainApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun MainApp(viewModel: MainViewModel) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val snackbarHostState = remember { SnackbarHostState() }

  val currentScreen by viewModel.currentScreen.collectAsState()
  val currentFile by viewModel.currentFile.collectAsState()
  val chatMessages by viewModel.chatMessages.collectAsState()
  val operationsHistory by viewModel.operationsHistory.collectAsState()
  val apkMetadata by viewModel.apkMetadata.collectAsState()
  val isAiThinking by viewModel.isAiThinking.collectAsState()
  val statusNotification by viewModel.statusNotification.collectAsState()

  var inputBarText by remember { mutableStateOf("") }
  var showSettingsDialog by remember { mutableStateOf(false) }

  // SAF File Picker Launcher (Open Save File or APK)
  val openDocumentLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri: Uri? ->
    if (uri != null) {
      scope.launch {
        try {
          val contentResolver: ContentResolver = context.contentResolver
          var fileName = "unknown_save"
          var fileSize = 0L

          contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
              if (nameIndex != -1) fileName = cursor.getString(nameIndex)
              if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
            }
          }

          val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
          viewModel.importFileFromUri(uri, fileName, fileSize, bytes)
        } catch (e: Exception) {
          viewModel.showNotification("Error loading file: ${e.message}")
        }
      }
    }
  }

  // SAF Export Launcher (Save Modified Save File)
  val createDocumentLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/octet-stream")
  ) { uri: Uri? ->
    if (uri != null) {
      viewModel.exportModifiedFile(uri)
    }
  }

  // Observe and show notifications
  LaunchedEffect(statusNotification) {
    statusNotification?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearNotification()
    }
  }

  // Back Handler for non-Home screens
  BackHandler(enabled = currentScreen != Screen.HOME) {
    if (drawerState.isOpen) {
      scope.launch { drawerState.close() }
    } else {
      viewModel.setScreen(Screen.HOME)
    }
  }

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet(
        drawerContainerColor = CyberSurface,
        drawerContentColor = TextPrimary,
        modifier = Modifier
          .width(310.dp)
          .border(1.dp, DeepEmeraldBorder, RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp))
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Drawer Header
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NeonEmerald),
              contentAlignment = Alignment.Center
            ) {
              Text("👾", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "MID GAME HACKER",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = NeonEmerald
              )
              Text(
                text = "AI Save Editor v2.4",
                fontSize = 11.sp,
                color = TextSecondary
              )
            }
          }

          HorizontalDivider(color = DeepEmeraldBorder)
          Spacer(modifier = Modifier.height(4.dp))

          // Navigation Items
          DrawerNavItem(
            icon = Icons.Default.Home,
            label = "Home Dashboard",
            selected = currentScreen == Screen.HOME,
            onClick = {
              viewModel.setScreen(Screen.HOME)
              scope.launch { drawerState.close() }
            }
          )

          DrawerNavItem(
            icon = Icons.Default.Psychology,
            label = "AI Chat & Modifier",
            selected = currentScreen == Screen.CHAT,
            badge = "${chatMessages.size}",
            onClick = {
              viewModel.setScreen(Screen.CHAT)
              scope.launch { drawerState.close() }
            }
          )

          DrawerNavItem(
            icon = Icons.Default.Tune,
            label = "Neural Scanner Orb",
            selected = currentScreen == Screen.SCAN_ORB,
            onClick = {
              viewModel.setScreen(Screen.SCAN_ORB)
              scope.launch { drawerState.close() }
            }
          )

          DrawerNavItem(
            icon = Icons.Default.Description,
            label = "Save File Inspector",
            selected = currentScreen == Screen.FILE_VIEWER,
            onClick = {
              viewModel.setScreen(Screen.FILE_VIEWER)
              scope.launch { drawerState.close() }
            }
          )

          DrawerNavItem(
            icon = Icons.Default.Security,
            label = "APK Security Audit",
            selected = currentScreen == Screen.APK_INSPECTOR,
            onClick = {
              viewModel.setScreen(Screen.APK_INSPECTOR)
              scope.launch { drawerState.close() }
            }
          )

          DrawerNavItem(
            icon = Icons.Default.History,
            label = "Backups & Audit Log",
            selected = currentScreen == Screen.HISTORY,
            badge = "${operationsHistory.size}",
            onClick = {
              viewModel.setScreen(Screen.HISTORY)
              scope.launch { drawerState.close() }
            }
          )

          Spacer(modifier = Modifier.weight(1f))

          HorizontalDivider(color = DeepEmeraldBorder)

          // Load sample files quick section
          Text(
            text = "PRELOADED SAMPLE SAVES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NeonEmerald,
            modifier = Modifier.padding(top = 8.dp)
          )

          val samples = SampleSaves.getSamples()
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(CyberSurfaceContainer)
                .clickable {
                  viewModel.loadSample(samples[0])
                  scope.launch { drawerState.close() }
                }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text("RPG JSON", fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(CyberSurfaceContainer)
                .clickable {
                  viewModel.loadSample(samples[1])
                  scope.launch { drawerState.close() }
                }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text("SPEED XML", fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(CyberSurfaceContainer)
                .clickable {
                  viewModel.loadSample(samples[2])
                  scope.launch { drawerState.close() }
                }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text("MECHA INI", fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          DrawerNavItem(
            icon = Icons.Default.Settings,
            label = "AI & Proxy Settings",
            selected = false,
            onClick = {
              showSettingsDialog = true
              scope.launch { drawerState.close() }
            }
          )
        }
      }
    }
  ) {
    Scaffold(
      modifier = Modifier
        .fillMaxSize()
        .background(CyberBackground)
        .statusBarsPadding()
        .imePadding(),
      containerColor = CyberBackground,
      snackbarHost = { SnackbarHost(snackbarHostState) },
      topBar = {
        if (currentScreen != Screen.SCAN_ORB) {
          TopBar(
            activeFileName = currentFile?.name,
            onMenuClick = { scope.launch { drawerState.open() } },
            onPillClick = {
              if (currentFile != null) viewModel.setScreen(Screen.FILE_VIEWER)
              else openDocumentLauncher.launch(arrayOf("*/*"))
            },
            onActionClick = {
              // Open new file or sample
              openDocumentLauncher.launch(arrayOf("*/*"))
            }
          )
        }
      },
      bottomBar = {
        // Bottom input bar visible on Home and Chat screens
        if (currentScreen == Screen.HOME || currentScreen == Screen.CHAT) {
          BottomInputBar(
            inputText = inputBarText,
            onInputTextChange = { inputBarText = it },
            onSendClick = {
              val cmd = inputBarText
              inputBarText = ""
              if (currentScreen != Screen.CHAT) {
                viewModel.setScreen(Screen.CHAT)
              }
              viewModel.sendNaturalLanguageCommand(cmd)
            },
            onAttachClick = {
              openDocumentLauncher.launch(arrayOf("*/*"))
            },
            onMicClick = {
              viewModel.setScreen(Screen.SCAN_ORB)
            },
            onPresetsClick = {
              inputBarText = "Give me unlimited coins"
            },
            placeholder = "What do you want to change?",
            modifier = Modifier.navigationBarsPadding()
          )
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        when (currentScreen) {
          Screen.HOME -> HomeScreen(
            currentFile = currentFile,
            onImportClick = { openDocumentLauncher.launch(arrayOf("*/*")) },
            onInspectApkClick = { openDocumentLauncher.launch(arrayOf("application/vnd.android.package-archive", "*/*")) },
            onQuickCommand = { cmd ->
              viewModel.setScreen(Screen.CHAT)
              viewModel.sendNaturalLanguageCommand(cmd)
            },
            onLoadSample = { sample -> viewModel.loadSample(sample) },
            onNavigateToScreen = { screen -> viewModel.setScreen(screen) },
            onRestoreBackupClick = { viewModel.restoreOriginalBackup() }
          )

          Screen.SCAN_ORB -> ScanOrbScreen(
            currentFile = currentFile,
            onTriggerVoiceHack = { voiceCmd ->
              viewModel.setScreen(Screen.CHAT)
              viewModel.sendNaturalLanguageCommand(voiceCmd)
            },
            onClose = { viewModel.setScreen(Screen.HOME) },
            onNavigateToFileViewer = { viewModel.setScreen(Screen.FILE_VIEWER) }
          )

          Screen.CHAT -> ChatScreen(
            messages = chatMessages,
            currentFile = currentFile,
            isThinking = isAiThinking,
            onApplyPendingChanges = { mods, cmd -> viewModel.applyModifications(mods, cmd) },
            onExportClick = {
              val exportName = currentFile?.let { "modified_${it.name}" } ?: "modified_save.json"
              createDocumentLauncher.launch(exportName)
            },
            onRestoreBackupClick = { viewModel.restoreOriginalBackup() }
          )

          Screen.FILE_VIEWER -> FileViewerScreen(
            saveFile = currentFile,
            onBack = { viewModel.setScreen(Screen.HOME) },
            onApplyFieldModification = { path, newVal ->
              viewModel.applyModifications(mapOf(path to newVal), "Manual set $path = $newVal")
            },
            onExportClick = {
              val exportName = currentFile?.let { "modified_${it.name}" } ?: "modified_save.json"
              createDocumentLauncher.launch(exportName)
            },
            onRestoreBackupClick = { viewModel.restoreOriginalBackup() }
          )

          Screen.APK_INSPECTOR -> ApkInspectorScreen(
            metadata = apkMetadata,
            onBack = { viewModel.setScreen(Screen.HOME) },
            onImportOtherFile = { openDocumentLauncher.launch(arrayOf("application/vnd.android.package-archive", "*/*")) }
          )

          Screen.HISTORY -> HistoryScreen(
            operations = operationsHistory,
            onBack = { viewModel.setScreen(Screen.HOME) },
            onRestoreOperation = { op -> viewModel.restoreFromHistory(op) }
          )

          Screen.SETTINGS -> {
            // Handled via Dialog
          }
        }
      }
    }
  }

  if (showSettingsDialog) {
    SettingsDialog(
      initialProxyUrl = viewModel.getProxyUrl(),
      initialApiKey = viewModel.getApiKey(),
      initialModel = viewModel.getModelName(),
      onDismiss = { showSettingsDialog = false },
      onSave = { proxyUrl, apiKey, model ->
        viewModel.updateSettings(proxyUrl, apiKey, model)
        showSettingsDialog = false
      }
    )
  }
}

@Composable
fun DrawerNavItem(
  icon: ImageVector,
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  badge: String? = null
) {
  NavigationDrawerItem(
    icon = { Icon(icon, contentDescription = null, tint = if (selected) NeonEmerald else TextSecondary) },
    label = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = label,
          color = if (selected) NeonEmerald else TextPrimary,
          fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
          fontSize = 13.sp
        )
        if (badge != null) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (selected) NeonEmerald else CyberSurfaceContainer)
              .padding(horizontal = 7.dp, vertical = 2.dp)
          ) {
            Text(
              text = badge,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = if (selected) CyberBlack else TextSecondary
            )
          }
        }
      }
    },
    selected = selected,
    onClick = onClick,
    colors = NavigationDrawerItemDefaults.colors(
      selectedContainerColor = CyberSurfaceContainer,
      unselectedContainerColor = Color.Transparent
    ),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier.padding(vertical = 2.dp)
  )
}
