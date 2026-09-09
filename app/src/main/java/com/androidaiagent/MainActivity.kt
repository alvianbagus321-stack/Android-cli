package com.androidaiagent

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.androidaiagent.data.AgentApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidaiagent.agent.ProviderCatalog
import com.androidaiagent.agent.ShizukuConnector
import com.androidaiagent.data.Tool

private val Ink = Color(0xFF101323)
private val Purple = Color(0xFF8B7CFF)
private val Cyan = Color(0xFF55D7E8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AgentTheme { AgentApp() } }
    }
}

@Composable
private fun AgentTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(primary = Purple, secondary = Cyan, background = Ink, surface = Color(0xFF1A1D31), surfaceVariant = Color(0xFF272B43)), content = content)
}

@Composable
fun AgentApp() {
    var tab by remember { mutableIntStateOf(0) }
    var prompt by remember { mutableStateOf("") }
    var permissionMode by remember { mutableStateOf("normal") }
    var accessibilityAllowed by remember { mutableStateOf(false) }
    var screenCaptureAllowed by remember { mutableStateOf(false) }
    var adbAllowed by remember { mutableStateOf(false) }
    var showFullAccessDialog by remember { mutableStateOf(false) }
    var showRestrictedGuidance by remember { mutableStateOf(false) }
    val setPermissionMode: (String) -> Unit = { requested -> if (requested == "full") showFullAccessDialog = true else permissionMode = "normal" }
    val providers = remember { ProviderCatalog().defaults() }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    val trace = remember { mutableStateListOf<TraceItem>() }
    val scope = rememberCoroutineScope()
    val api = remember { AgentApi("http://10.0.2.2:8787") }
    var sending by remember { mutableStateOf(false) }
    val tools = remember { mutableStateListOf(
        Tool("screen", "Analyze screen", "Screenshot + Accessibility Tree", "{\"type\":\"object\"}", "built-in"),
        Tool("device", "Device info", "Read device capabilities", "{\"type\":\"object\"}", "built-in")
    ) }
    val context = LocalContext.current
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    val captureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result -> screenCaptureAllowed = result.resultCode == android.app.Activity.RESULT_OK }

    val openAccessibility = { showRestrictedGuidance = true }
    val openAdb = { settingsLauncher.launch(Intent(Settings.ACTION_WIRELESS_SETTINGS)) }
    val openCapture = { val manager = context.getSystemService(MediaProjectionManager::class.java); captureLauncher.launch(manager.createScreenCaptureIntent()) }

    Scaffold(containerColor = Color.Transparent, bottomBar = {
        NavigationBar(containerColor = Color(0xFF15182A)) {
            val items = listOf("Home" to Icons.Default.Home, "Models" to Icons.Default.AutoAwesome, "Tools" to Icons.Default.Build, "Access" to Icons.Default.Security, "Settings" to Icons.Default.Settings)
            items.forEachIndexed { index, item -> NavigationBarItem(selected = tab == index, onClick = { tab = index }, icon = { Icon(item.second, item.first) }, label = { Text(item.first) }) }
        }
    }) { padding ->
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF101323), Color(0xFF202044), Color(0xFF101323))))) {
            Column(Modifier.padding(padding).padding(horizontal = 18.dp).fillMaxSize()) {
                Header(tab)
                when (tab) {
                    0 -> Dashboard(prompt, { prompt = it }, providers.count { it.configured }, tools.size, permissionMode, setPermissionMode, messages, trace, sending) { text ->
                        if (text.isNotBlank() && !sending) {
                            messages.add(ChatMessage(true, text)); trace.add(TraceItem("REQUEST", "Instruksi diterima; menyiapkan respons AI.")); prompt = ""; sending = true
                            trace.add(TraceItem("POLICY", if (permissionMode == "full") "Full access disetujui; tetap menunggu permission Android." else "Normal access; hanya capability yang diizinkan yang dapat dipakai."))
                            scope.launch { val result = api.chat("openai", "gpt-4o-mini", text); result.fold({ raw -> trace.add(TraceItem("MODEL", "Respons diterima dari provider OpenAI / GPT.")); val answer = runCatching { JSONObject(raw).optString("answer") }.getOrDefault(raw); messages.add(ChatMessage(false, answer)) }, { error -> trace.add(TraceItem("ERROR", "Provider belum menghasilkan respons.")); messages.add(ChatMessage(false, "Backend belum siap: ${error.message}")) }); sending = false }
                        }
                    }
                    1 -> Providers(providers)
                    2 -> Tools(tools)
                    3 -> AccessScreen(openAccessibility, openCapture, openAdb, accessibilityAllowed, { accessibilityAllowed = it }, screenCaptureAllowed, { screenCaptureAllowed = it }, adbAllowed, { adbAllowed = it })
                    else -> SettingsScreen(settingsLauncher, permissionMode, setPermissionMode)
                }
            }
        }
    }
    if (showRestrictedGuidance) AlertDialog(onDismissRequest = { showRestrictedGuidance = false }, title = { Text("Accessibility Service") }, text = { Text("Jika Android menampilkan Setelan terbatas, buka App Info aplikasi ini, tekan menu titik tiga (⋮), pilih Izinkan akses terbatas / Allow restricted settings, lalu kembali ke Accessibility dan aktifkan Android AI Agent. Ini adalah proteksi Android 13+ untuk APK sideload.", color = Color.LightGray) }, confirmButton = { Button(onClick = { showRestrictedGuidance = false; settingsLauncher.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) { Text("Buka Accessibility") } }, dismissButton = { TextButton(onClick = { showRestrictedGuidance = false }) { Text("Mengerti") } })
    if (showFullAccessDialog) AlertDialog(onDismissRequest = { showFullAccessDialog = false }, title = { Text("Enable Full access?") }, text = { Text("This mode may use Accessibility, screen capture, and an optional authenticated ADB bridge. Android still controls what is possible. Do you explicitly approve these permissions?", color = Color.LightGray) }, confirmButton = { Button(onClick = { permissionMode = "full"; showFullAccessDialog = false }) { Text("Yes, I approve") } }, dismissButton = { TextButton(onClick = { showFullAccessDialog = false }) { Text("No") } })
}

@Composable private fun Header(tab: Int) {
    val titles = listOf("Control center", "AI model hub", "Tool studio", "Permissions & access", "App settings")
    Row(Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Brush.linearGradient(listOf(Purple, Cyan))), contentAlignment = Alignment.Center) { Icon(Icons.Default.Bolt, null, tint = Ink) }
        Spacer(Modifier.width(12.dp)); Column { Text("ANDROID AI", style = MaterialTheme.typography.labelMedium, color = Cyan, letterSpacing = 2.sp); Text(titles[tab], style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
    }
}

@Composable private fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xCC1B1F35)), border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0x558B7CFF), Color(0x2255D7E8)))), content = content)
}

data class ChatMessage(val fromUser: Boolean, val text: String)
data class TraceItem(val stage: String, val summary: String)

@Composable private fun Dashboard(prompt: String, onPrompt: (String) -> Unit, configured: Int, toolCount: Int, permissionMode: String, onMode: (String) -> Unit, messages: List<ChatMessage>, trace: List<TraceItem>, sending: Boolean, onSend: (String) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
        item { GlassCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(10.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF59E391))); Spacer(Modifier.width(8.dp)); Text("AI ONLINE", color = Color(0xFF59E391), fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text("$configured models • $toolCount tools", color = Color.Gray, fontSize = 12.sp) }; Spacer(Modifier.height(12.dp)); Text("Chat with your agent", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Ask normally. The agent can answer, inspect approved screen state, and request tools when needed.", color = Color.LightGray); Spacer(Modifier.height(10.dp)); PermissionModeSelector(permissionMode, onMode); Spacer(Modifier.height(12.dp)); if (messages.isEmpty()) Text("Try: ‘Open Settings and tell me what is visible.’", color = Color.Gray, fontSize = 13.sp); messages.takeLast(8).forEach { message -> ChatBubble(message) }; if (sending) { Text("Agent is thinking…", color = Cyan, modifier = Modifier.padding(vertical = 8.dp)) }; if (trace.isNotEmpty()) { Spacer(Modifier.height(8.dp)); Text("THINKING HISTORY · SUMMARY", color = Cyan, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp); trace.takeLast(6).forEach { item -> Row(Modifier.padding(vertical = 3.dp)) { Text(item.stage, color = Purple, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(70.dp)); Text(item.summary, color = Color.LightGray, fontSize = 12.sp) } }; Text("Ini ringkasan proses dan event, bukan raw private chain-of-thought.", color = Color.Gray, fontSize = 10.sp) }; OutlinedTextField(prompt, onPrompt, Modifier.fillMaxWidth(), placeholder = { Text("Message the AI agent…") }, minLines = 2, shape = RoundedCornerShape(16.dp)); Spacer(Modifier.height(10.dp)); Row { Button(onClick = { onSend(prompt) }, enabled = prompt.isNotBlank() && !sending, shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.Send, null); Spacer(Modifier.width(6.dp)); Text("Send") }; Spacer(Modifier.width(8.dp)); OutlinedButton(onClick = {}, shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.Stop, null); Spacer(Modifier.width(6.dp)); Text("Stop") } } } } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { MetricCard("$configured", "models ready", Modifier.weight(1f)); MetricCard("$toolCount", "tools", Modifier.weight(1f)); MetricCard("${if (permissionMode == "full") "FULL" else "NORMAL"}", "access mode", Modifier.weight(1f)) } }
        item { GlassCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("LIVE DEVICE VIEW", color = Cyan, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp); Spacer(Modifier.height(12.dp)); Box(Modifier.fillMaxWidth().height(165.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFF0C0E18)), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.PhoneAndroid, null, tint = Purple, modifier = Modifier.size(38.dp)); Text("Screen capture permission required", color = Color.LightGray); Text("Enable it from Access", color = Color.Gray, fontSize = 12.sp) } } } } }
    }
}

@Composable private fun ChatBubble(message: ChatMessage) { Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start) { Surface(color = if (message.fromUser) Color(0xFF514B9B) else Color(0xFF292D4A), shape = RoundedCornerShape(16.dp), modifier = Modifier.widthIn(max = 310.dp)) { Text(message.text, Modifier.padding(12.dp), color = Color.White) } } }

@Composable private fun PermissionModeSelector(mode: String, onMode: (String) -> Unit) { Column { Text("PERMISSION MODE", color = Cyan, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp); Spacer(Modifier.height(6.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { FilterChip(selected = mode == "normal", onClick = { onMode("normal") }, label = { Text("Normal access") }, leadingIcon = { Icon(Icons.Default.Lock, null) }); FilterChip(selected = mode == "full", onClick = { onMode("full") }, label = { Text("Full access") }, leadingIcon = { Icon(Icons.Default.Security, null) }) }; Text(if (mode == "full") "Full access requires explicit Accessibility, screen capture, and optional authenticated ADB bridge approval." else "Normal access uses only the permissions you explicitly enable.", color = if (mode == "full") Color(0xFFFFC857) else Color.Gray, fontSize = 12.sp) } }

@Composable private fun MetricCard(value: String, label: String, modifier: Modifier) { GlassCard(modifier) { Column(Modifier.padding(14.dp)) { Text(value, style = MaterialTheme.typography.titleLarge, color = Purple, fontWeight = FontWeight.Bold); Text(label, color = Color.LightGray, fontSize = 12.sp) } } }

@Composable private fun Providers(items: List<com.androidaiagent.data.Provider>) { LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) { item { Text("Choose the brain for each task. Vision-capable models can understand approved screenshots.", color = Color.LightGray) }; items(items) { p -> GlassCard(Modifier.fillMaxWidth()) { Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFF292D4A)), contentAlignment = Alignment.Center) { Text(p.name.take(1), color = Cyan, fontWeight = FontWeight.Bold) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(p.name, fontWeight = FontWeight.Bold); Text(p.models.joinToString(" • "), color = Color.LightGray, fontSize = 12.sp); Text(if (p.vision) "Vision + text" else "Text / reasoning", color = if (p.vision) Cyan else Color.Gray, fontSize = 12.sp) }; OutlinedButton(onClick = {}, shape = RoundedCornerShape(12.dp)) { Text(if (p.configured) "Edit" else "Setup") } } } } } }

@Composable private fun Tools(items: androidx.compose.runtime.snapshots.SnapshotStateList<Tool>) { var showAdd by remember { mutableStateOf(false) }; var name by remember { mutableStateOf("") }; var description by remember { mutableStateOf("") }; LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) { item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Build your tool belt", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Schema-first tools are safer than arbitrary code.", color = Color.LightGray) }; Button(onClick = { showAdd = true }, shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.Add, null); Text(" Add") } } }; items(items) { t -> GlassCard(Modifier.fillMaxWidth()) { ListItem(headlineContent = { Text(t.name, fontWeight = FontWeight.Bold) }, supportingContent = { Text(t.description) }, trailingContent = { Text(t.type, color = Cyan, fontSize = 12.sp) }, colors = ListItemDefaults.colors(containerColor = Color.Transparent)) } } }; if (showAdd) AlertDialog(onDismissRequest = { showAdd = false }, title = { Text("Add custom tool") }, text = { Column { OutlinedTextField(name, { name = it }, label = { Text("Name") }); OutlinedTextField(description, { description = it }, label = { Text("Description") }) } }, confirmButton = { Button(onClick = { if (name.isNotBlank()) { items.add(Tool(java.util.UUID.randomUUID().toString(), name, description, "{\"type\":\"object\"}", "custom")); name = ""; description = ""; showAdd = false } }) { Text("Add") } }, dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }) }

@Composable private fun AccessScreen(openAccessibility: () -> Unit, openCapture: () -> Unit, openAdb: () -> Unit, accessibility: Boolean, setAccessibility: (Boolean) -> Unit, capture: Boolean, setCapture: (Boolean) -> Unit, adb: Boolean, setAdb: (Boolean) -> Unit) { val shizuku = remember { ShizukuConnector() }; val shizukuAvailable = shizuku.isServiceAvailable(); val shizukuGranted = shizuku.hasUserGrant(); LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) { item { GlassCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("Permission checklist", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Choose Yes or No for every capability. Full access only becomes active after you approve the required items.", color = Color.LightGray) } } }; item { PermissionToggleCard("Accessibility service", "Read UI nodes, click, scroll and type", accessibility, setAccessibility, Icons.Default.TouchApp, openAccessibility) }; item { PermissionToggleCard("Screen capture", "Let the agent see an approved screen", capture, setCapture, Icons.Default.ScreenShare, openCapture) }; item { PermissionToggleCard("ADB bridge", "Optional user-owned computer / wireless debugging bridge", adb, setAdb, Icons.Default.Terminal, openAdb) }; item { ShizukuCard(shizuku, shizukuAvailable, shizukuGranted) }; item { GlassCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("ADB CONNECT — POWER USER", color = Cyan, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); Text("A normal Android app cannot silently run adb or bypass Android security. Pair a separate local bridge through USB or Wireless debugging; this screen only records your explicit approval.", color = Color.LightGray); Spacer(Modifier.height(8.dp)); Text("1. Enable Developer options and Wireless debugging.\n2. Pair with the code shown by Android.\n3. Connect only to your private computer.\n4. Keep authentication, audit logs, and emergency stop enabled.", color = Color.White); Spacer(Modifier.height(10.dp)); Text(if (adb) "ADB permission: YES — bridge connection still needs to be paired and authenticated." else "ADB permission: NO — no ADB action will be requested.", color = if (adb) Color(0xFFFFC857) else Color.Gray, fontSize = 12.sp) } } } } }

@Composable private fun ShizukuCard(connector: ShizukuConnector, available: Boolean, granted: Boolean) { GlassCard(Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Terminal, null, tint = if (granted) Color(0xFF59E391) else Purple, modifier = Modifier.size(30.dp)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text("Shizuku connector", fontWeight = FontWeight.Bold); Text(if (!available) "Shizuku service not running" else if (!granted) "Service found — permission required" else "Connected with user approval", color = Color.LightGray, fontSize = 13.sp); Text("Not root • revocable", color = Color(0xFFFFC857), fontSize = 12.sp) }; if (available && !granted) OutlinedButton(onClick = { connector.requestUserGrant() }, shape = RoundedCornerShape(12.dp)) { Text("Allow") } } } }

@Composable private fun PermissionToggleCard(title: String, detail: String, enabled: Boolean, onEnabled: (Boolean) -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, open: () -> Unit) { GlassCard(Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = if (enabled) Color(0xFF59E391) else Purple, modifier = Modifier.size(30.dp)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold); Text(detail, color = Color.LightGray, fontSize = 13.sp); Text(if (enabled) "YES — approved" else "NO — not approved", color = if (enabled) Color(0xFF59E391) else Color(0xFFFFC857), fontSize = 12.sp) }; Switch(checked = enabled, onCheckedChange = { onEnabled(it); if (it) open() }) } } }

@Composable private fun PermissionCard(title: String, detail: String, status: String, icon: androidx.compose.ui.graphics.vector.ImageVector, action: () -> Unit) { GlassCard(Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Purple, modifier = Modifier.size(30.dp)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold); Text(detail, color = Color.LightGray, fontSize = 13.sp); Text(status, color = Color(0xFFFFC857), fontSize = 12.sp) }; OutlinedButton(onClick = action, shape = RoundedCornerShape(12.dp)) { Text("Open") } } } }

@Composable private fun SettingsScreen(settingsLauncher: androidx.activity.result.ActivityResultLauncher<Intent>, permissionMode: String, onMode: (String) -> Unit) { LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) { item { GlassCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("Safety defaults", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Remote control is disabled by default. Screenshot sharing requires explicit approval. Sensitive values should never be placed in logs.", color = Color.LightGray) } } }; item { GlassCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { PermissionModeSelector(permissionMode, onMode); if (permissionMode == "full") { Spacer(Modifier.height(10.dp)); Text("Full access is a consent mode, not a bypass. The agent will remain limited when Android or the connected ADB bridge denies an operation.", color = Color(0xFFFFC857), fontSize = 12.sp) } } } }; item { SettingRow("Server endpoint", "http://10.0.2.2:8787", Icons.Default.Cloud) }; item { SettingRow("Action verification", "Always verify after an action", Icons.Default.Verified) }; item { SettingRow("Theme", "Aurora dark", Icons.Default.Palette) }; item { Button(onClick = { settingsLauncher.launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = android.net.Uri.parse("package:com.androidaiagent") }) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.Tune, null); Spacer(Modifier.width(8.dp)); Text("Open Android app settings") } } } }

@Composable private fun SettingRow(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) { GlassCard(Modifier.fillMaxWidth()) { Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Cyan); Spacer(Modifier.width(12.dp)); Column { Text(title, fontWeight = FontWeight.Bold); Text(value, color = Color.LightGray, fontSize = 13.sp) } } } }
