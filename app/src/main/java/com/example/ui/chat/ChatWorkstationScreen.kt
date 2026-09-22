package com.example.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.apikeys.ApiKeyManagerDialog
import com.example.ui.compare.MultiModelCompareScreen
import com.example.ui.components.ModelSelectorTrigger
import com.example.ui.components.TactileIconButton
import com.example.ui.components.TactileRockerSwitch
import com.example.ui.settings.ModelProfileDialog
import com.example.ui.settings.SettingsDialog
import com.example.ui.sidebar.WorkstationSidebar
import com.example.ui.theme.HardwareScrew
import com.example.ui.theme.SkeuoColors
import com.example.ui.theme.StatusLed
import com.example.ui.theme.tactileRaised
import com.example.viewmodel.ChatViewModel
import com.example.viewmodel.WorkstationMode
import kotlinx.coroutines.launch

@Composable
fun ChatWorkstationScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val workstationMode by viewModel.workstationMode.collectAsState()
    val currentModel by viewModel.currentModel.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val currentConversationId by viewModel.currentConversationId.collectAsState()
    val currentMessages by viewModel.currentMessages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val attachedImage by viewModel.attachedImageBase64.collectAsState()
    val enableWebSearch by viewModel.enableWebSearch.collectAsState()
    val isStreaming by viewModel.isStreaming.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val temperature by viewModel.temperature.collectAsState()
    val providerConfigs by viewModel.providerConfigs.collectAsState()

    val compareSelectedModels by viewModel.compareSelectedModels.collectAsState()
    val comparisonResults by viewModel.comparisonResults.collectAsState()
    val isComparing by viewModel.isComparing.collectAsState()

    val showApiKeyManager by viewModel.showApiKeyManager.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()
    val showModelProfile by viewModel.showModelProfile.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 840.dp

        if (isWideScreen) {
            // Wide Screen: Dual-Pane Persistent Workstation Chassis
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SkeuoColors.ChassisBg)
            ) {
                WorkstationSidebar(
                    conversations = conversations,
                    currentConversationId = currentConversationId,
                    currentMode = workstationMode,
                    onSelectMode = { viewModel.setWorkstationMode(it) },
                    onSelectConversation = { viewModel.selectConversation(it) },
                    onNewConversation = { viewModel.startNewConversation() },
                    onTogglePin = { viewModel.togglePinConversation(it) },
                    onToggleFavorite = { viewModel.toggleFavoriteConversation(it) },
                    onRenameConversation = { id, title -> viewModel.renameConversation(id, title) },
                    onDeleteConversation = { viewModel.deleteConversation(it) },
                    onOpenApiKeys = { viewModel.setShowApiKeyManager(true) },
                    onOpenSettings = { viewModel.setShowSettings(true) }
                )

                // Main Console Frame
                WorkstationMainConsole(
                    viewModel = viewModel,
                    isWideScreen = true,
                    onOpenDrawer = {}
                )
            }
        } else {
            // Handheld Screen: Tactile Slide-out Drawer Chassis
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    WorkstationSidebar(
                        conversations = conversations,
                        currentConversationId = currentConversationId,
                        currentMode = workstationMode,
                        onSelectMode = {
                            viewModel.setWorkstationMode(it)
                            scope.launch { drawerState.close() }
                        },
                        onSelectConversation = {
                            viewModel.selectConversation(it)
                            scope.launch { drawerState.close() }
                        },
                        onNewConversation = {
                            viewModel.startNewConversation()
                            scope.launch { drawerState.close() }
                        },
                        onTogglePin = { viewModel.togglePinConversation(it) },
                        onToggleFavorite = { viewModel.toggleFavoriteConversation(it) },
                        onRenameConversation = { id, title -> viewModel.renameConversation(id, title) },
                        onDeleteConversation = { viewModel.deleteConversation(it) },
                        onOpenApiKeys = {
                            viewModel.setShowApiKeyManager(true)
                            scope.launch { drawerState.close() }
                        },
                        onOpenSettings = {
                            viewModel.setShowSettings(true)
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            ) {
                WorkstationMainConsole(
                    viewModel = viewModel,
                    isWideScreen = false,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
        }
    }

    // Modal Dialogs
    if (showApiKeyManager) {
        ApiKeyManagerDialog(
            configs = providerConfigs,
            onDismiss = { viewModel.setShowApiKeyManager(false) },
            onSaveKey = { provider, key, endpoint ->
                viewModel.saveApiKey(provider, key, endpoint)
            },
            onToggleEnabled = { provider, enabled ->
                viewModel.toggleProvider(provider, enabled)
            },
            onDeleteKey = { provider ->
                viewModel.deleteApiKey(provider)
            },
            onTestConnection = { provider ->
                viewModel.testProvider(provider)
            }
        )
    }

    if (showSettings) {
        SettingsDialog(
            temperature = temperature,
            onTemperatureChanged = { viewModel.setTemperature(it) },
            fontSize = fontSize,
            onFontSizeChanged = { viewModel.setFontSize(it) },
            onExportMarkdown = { viewModel.exportCurrentConversation() },
            onClearAllData = { viewModel.clearAllData() },
            onDismiss = { viewModel.setShowSettings(false) }
        )
    }

    if (showModelProfile) {
        ModelProfileDialog(
            model = currentModel,
            onDismiss = { viewModel.setShowModelProfile(false) }
        )
    }
}

@Composable
fun WorkstationMainConsole(
    viewModel: ChatViewModel,
    isWideScreen: Boolean,
    onOpenDrawer: () -> Unit
) {
    val workstationMode by viewModel.workstationMode.collectAsState()
    val currentModel by viewModel.currentModel.collectAsState()
    val currentMessages by viewModel.currentMessages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val attachedImage by viewModel.attachedImageBase64.collectAsState()
    val enableWebSearch by viewModel.enableWebSearch.collectAsState()
    val isStreaming by viewModel.isStreaming.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()

    val compareSelectedModels by viewModel.compareSelectedModels.collectAsState()
    val comparisonResults by viewModel.comparisonResults.collectAsState()
    val isComparing by viewModel.isComparing.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SkeuoColors.ChassisBg)
    ) {
        // Physical Workstation Top Control Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SkeuoColors.ChassisPlate)
                .border(1.dp, SkeuoColors.ChassisPlateBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isWideScreen) {
                    TactileIconButton(
                        icon = Icons.Default.Menu,
                        contentDescription = "Open Drawer",
                        size = 34.dp,
                        onClick = onOpenDrawer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Active Model Selector Hardware Trigger
                ModelSelectorTrigger(
                    currentModel = currentModel,
                    onModelSelected = { viewModel.selectModel(it) }
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Quick Model Profile specs icon
                TactileIconButton(
                    icon = Icons.Default.Info,
                    contentDescription = "Model Specs",
                    size = 32.dp,
                    onClick = { viewModel.setShowModelProfile(true) }
                )
            }
        }

        // Center Content: Either Chat Console or Multi-AI Compare Matrix
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (workstationMode == WorkstationMode.CHAT_CONSOLE) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ChatMessagesView(
                        messages = currentMessages,
                        isStreaming = isStreaming,
                        fontSize = fontSize,
                        onRegenerate = { viewModel.regenerateLastMessage() },
                        onRetryWithModel = { viewModel.retryWithModel(it) },
                        onEditMessage = { id, content -> viewModel.updateMessage(id, content) },
                        onDeleteMessage = { viewModel.deleteMessage(it) },
                        modifier = Modifier.weight(1f)
                    )

                    TactileComposer(
                        inputText = inputText,
                        onInputTextChanged = { viewModel.setInputText(it) },
                        attachedImageBase64 = attachedImage,
                        onAttachedImageChanged = { viewModel.setAttachedImage(it) },
                        enableWebSearch = enableWebSearch,
                        onToggleWebSearch = { viewModel.toggleWebSearch() },
                        isStreaming = isStreaming,
                        currentModel = currentModel,
                        onSendMessage = { viewModel.sendMessage() },
                        onStopGeneration = { viewModel.stopGeneration() }
                    )
                }
            } else {
                MultiModelCompareScreen(
                    selectedModels = compareSelectedModels,
                    comparisonResults = comparisonResults,
                    isComparing = isComparing,
                    onAddOrChangeModel = { provider, modelId ->
                        viewModel.toggleCompareModel(provider, modelId)
                    },
                    onRemoveModel = { provider, modelId ->
                        viewModel.toggleCompareModel(provider, modelId)
                    },
                    onRunCompare = { prompt ->
                        viewModel.runMultiModelCompare(prompt)
                    },
                    onStopCompare = { viewModel.stopGeneration() }
                )
            }
        }
    }
}
