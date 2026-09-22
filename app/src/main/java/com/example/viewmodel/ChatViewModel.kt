package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.ConversationEntity
import com.example.data.local.entities.ProviderConfigEntity
import com.example.data.repository.ChatRepository
import com.example.model.AiModelInfo
import com.example.model.AiProviderType
import com.example.model.ChatMessage
import com.example.model.ComparisonTileState
import com.example.model.MessageRole
import com.example.model.ModelCatalog
import com.example.network.KeyValidationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class WorkstationMode {
    CHAT_CONSOLE,
    MULTI_AI_COMPARE
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    val repository = ChatRepository(application)

    // Current Mode
    private val _workstationMode = MutableStateFlow(WorkstationMode.CHAT_CONSOLE)
    val workstationMode: StateFlow<WorkstationMode> = _workstationMode.asStateFlow()

    // Active Model
    private val _currentModel = MutableStateFlow(ModelCatalog.defaultModels.first())
    val currentModel: StateFlow<AiModelInfo> = _currentModel.asStateFlow()

    // Active Conversation
    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    val conversations: StateFlow<List<ConversationEntity>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val providerConfigs: StateFlow<List<ProviderConfigEntity>> = repository.allProviderConfigs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<String>> = repository.allFolders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Messages for current conversation
    private val _currentMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val currentMessages: StateFlow<List<ChatMessage>> = _currentMessages.asStateFlow()

    // Composer State
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _attachedImageBase64 = MutableStateFlow<String?>(null)
    val attachedImageBase64: StateFlow<String?> = _attachedImageBase64.asStateFlow()

    private val _enableWebSearch = MutableStateFlow(false)
    val enableWebSearch: StateFlow<Boolean> = _enableWebSearch.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    // Compare Mode State
    private val _compareSelectedModels = MutableStateFlow<List<Pair<AiProviderType, String>>>(
        listOf(
            AiProviderType.GEMINI to "gemini-3.5-flash",
            AiProviderType.OPENAI to "gpt-4o",
            AiProviderType.ANTHROPIC to "claude-3-5-sonnet-20241022"
        )
    )
    val compareSelectedModels: StateFlow<List<Pair<AiProviderType, String>>> = _compareSelectedModels.asStateFlow()

    private val _comparisonResults = MutableStateFlow<List<ComparisonTileState>>(emptyList())
    val comparisonResults: StateFlow<List<ComparisonTileState>> = _comparisonResults.asStateFlow()

    private val _isComparing = MutableStateFlow(false)
    val isComparing: StateFlow<Boolean> = _isComparing.asStateFlow()

    // Dialogs & Sheets
    private val _showApiKeyManager = MutableStateFlow(false)
    val showApiKeyManager: StateFlow<Boolean> = _showApiKeyManager.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    private val _showModelProfile = MutableStateFlow(false)
    val showModelProfile: StateFlow<Boolean> = _showModelProfile.asStateFlow()

    // Workstation Settings
    private val _temperature = MutableStateFlow(0.7f)
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    private val _fontSize = MutableStateFlow(14)
    val fontSize: StateFlow<Int> = _fontSize.asStateFlow()

    private val _tactileSounds = MutableStateFlow(true)
    val tactileSounds: StateFlow<Boolean> = _tactileSounds.asStateFlow()

    private var activeJob: Job? = null
    private var messageObservationJob: Job? = null

    init {
        // Observe conversations and pick initial or create one
        viewModelScope.launch {
            conversations.collect { convList ->
                if (_currentConversationId.value == null && convList.isNotEmpty()) {
                    selectConversation(convList.first().id)
                }
            }
        }
    }

    fun setWorkstationMode(mode: WorkstationMode) {
        _workstationMode.value = mode
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun setAttachedImage(base64: String?) {
        _attachedImageBase64.value = base64
    }

    fun toggleWebSearch() {
        _enableWebSearch.value = !_enableWebSearch.value
    }

    fun selectModel(model: AiModelInfo) {
        _currentModel.value = model
    }

    fun selectConversation(id: String) {
        _currentConversationId.value = id
        messageObservationJob?.cancel()
        messageObservationJob = viewModelScope.launch {
            repository.getMessagesForConversation(id).collect { msgs ->
                _currentMessages.value = msgs
            }
        }
    }

    fun startNewConversation() {
        viewModelScope.launch {
            val conv = repository.createNewConversation(
                provider = _currentModel.value.provider,
                modelId = _currentModel.value.id
            )
            selectConversation(conv.id)
            _inputText.value = ""
            _attachedImageBase64.value = null
        }
    }

    fun sendMessage() {
        val prompt = _inputText.value.trim()
        if (prompt.isBlank() && _attachedImageBase64.value == null) return

        var convId = _currentConversationId.value
        val model = _currentModel.value
        val image = _attachedImageBase64.value
        val webSearch = _enableWebSearch.value
        val temp = _temperature.value

        _inputText.value = ""
        _attachedImageBase64.value = null
        _isStreaming.value = true

        activeJob = viewModelScope.launch {
            if (convId == null) {
                val newConv = repository.createNewConversation(
                    title = prompt.take(30),
                    provider = model.provider,
                    modelId = model.id
                )
                convId = newConv.id
                selectConversation(convId!!)
            }

            repository.sendMessage(
                conversationId = convId!!,
                userPrompt = prompt,
                providerType = model.provider,
                modelId = model.id,
                temperature = temp,
                thinkingLevel = if (model.capabilities.hasThinking) "high" else null,
                enableWebSearch = webSearch,
                imageBase64 = image,
                onStreamingChunk = { _, _ -> }
            )

            _isStreaming.value = false
        }
    }

    fun stopGeneration() {
        activeJob?.cancel()
        _isStreaming.value = false
        _isComparing.value = false
    }

    fun regenerateLastMessage() {
        val msgs = _currentMessages.value
        val lastUserMsg = msgs.lastOrNull { it.role == MessageRole.USER } ?: return
        _inputText.value = lastUserMsg.content
        _attachedImageBase64.value = lastUserMsg.imageBase64
        sendMessage()
    }

    fun retryWithModel(model: AiModelInfo) {
        selectModel(model)
        regenerateLastMessage()
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun updateMessage(messageId: String, newContent: String) {
        viewModelScope.launch {
            repository.updateMessage(messageId, newContent)
        }
    }

    fun renameConversation(id: String, newTitle: String) {
        viewModelScope.launch {
            repository.updateConversationTitle(id, newTitle)
        }
    }

    fun togglePinConversation(id: String) {
        viewModelScope.launch {
            repository.togglePin(id)
        }
    }

    fun toggleFavoriteConversation(id: String) {
        viewModelScope.launch {
            repository.toggleFavorite(id)
        }
    }

    fun setConversationFolder(id: String, folder: String?) {
        viewModelScope.launch {
            repository.setFolder(id, folder)
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_currentConversationId.value == id) {
                val remaining = conversations.value.filter { it.id != id }
                if (remaining.isNotEmpty()) {
                    selectConversation(remaining.first().id)
                } else {
                    startNewConversation()
                }
            }
        }
    }

    suspend fun exportCurrentConversation(): String {
        val id = _currentConversationId.value ?: return ""
        return repository.exportConversationAsMarkdown(id)
    }

    // Compare Mode Operations
    fun toggleCompareModel(provider: AiProviderType, modelId: String) {
        val current = _compareSelectedModels.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.first == provider && it.second == modelId }
        if (existingIndex != -1) {
            if (current.size > 2) {
                current.removeAt(existingIndex)
            }
        } else {
            if (current.size < 4) {
                current.add(provider to modelId)
            }
        }
        _compareSelectedModels.value = current
    }

    fun runMultiModelCompare(prompt: String) {
        if (prompt.isBlank()) return
        _isComparing.value = true

        val initialTiles = _compareSelectedModels.value.map { (provider, modelId) ->
            ComparisonTileState(
                modelId = modelId,
                provider = provider,
                isGenerating = true
            )
        }
        _comparisonResults.value = initialTiles

        activeJob = viewModelScope.launch {
            repository.executeComparePrompt(
                prompt = prompt,
                selectedModels = _compareSelectedModels.value,
                temperature = _temperature.value,
                onUpdateTile = { updatedTile ->
                    val list = _comparisonResults.value.toMutableList()
                    val idx = list.indexOfFirst { it.modelId == updatedTile.modelId && it.provider == updatedTile.provider }
                    if (idx != -1) {
                        list[idx] = updatedTile
                    } else {
                        list.add(updatedTile)
                    }
                    _comparisonResults.value = list
                }
            )
            _isComparing.value = false
        }
    }

    // Dialog toggles
    fun setShowApiKeyManager(show: Boolean) {
        _showApiKeyManager.value = show
    }

    fun setShowSettings(show: Boolean) {
        _showSettings.value = show
    }

    fun setShowModelProfile(show: Boolean) {
        _showModelProfile.value = show
    }

    // Settings adjustments
    fun setTemperature(temp: Float) {
        _temperature.value = temp
    }

    fun setFontSize(size: Int) {
        _fontSize.value = size
    }

    fun setTactileSounds(enabled: Boolean) {
        _tactileSounds.value = enabled
    }

    // Key management
    fun saveApiKey(provider: AiProviderType, key: String, endpoint: String? = null, defaultModelId: String? = null) {
        viewModelScope.launch {
            repository.saveApiKey(provider, key, endpoint, defaultModelId)
        }
    }

    fun toggleProvider(provider: AiProviderType, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setProviderEnabled(provider, isEnabled)
        }
    }

    fun deleteApiKey(provider: AiProviderType) {
        viewModelScope.launch {
            repository.deleteApiKey(provider)
        }
    }

    suspend fun testProvider(provider: AiProviderType): KeyValidationResult {
        return repository.testProviderConnection(provider)
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllHistory()
            startNewConversation()
        }
    }
}
