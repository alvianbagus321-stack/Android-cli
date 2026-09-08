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
    val providers = remember { ProviderCatalog().defaults() }
    val tools = remember { mutableStateListOf(
        Tool("screen", "Analyze screen", "Screenshot + Accessibility Tree", "{\"type\":\"object\"}", "built-in"),
        Tool("device", "Device info", "Read device capabilities", "{\"type\":\"object\"}", "built-in")
    ) }
    val context = LocalContext.current
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    val captureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    val openAccessibility = { settingsLauncher.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
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
                    0 -> Dashboard(prompt, { prompt = it }, providers.count { it.configured }, tools.size)
                    1 -> Providers(providers)
                    2 -> Tools(tools)
                    3 -> AccessScreen(openAccessibility, openCapture)
                    else -> SettingsScreen(settingsLauncher)
                }
            }
        }
    }
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

@Composable private fun Dashboard(prompt: String, onPrompt: (String) -> Unit, configured: Int, toolCount: Int) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 20.dp)) {
        item { GlassCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(10.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF59E391))); Spacer(Modifier.width(8.dp)); Text("AGENT READY", color = Color(0xFF59E391), fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text("v0.1.0", color = Color.Gray) }; Spacer(Modifier.height(14.dp)); Text("What can I help you do?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("I will observe, act, and verify — only within the permissions you approve.", color = Color.LightGray); Spacer(Modifier.height(14.dp)); OutlinedTextField(prompt, onPrompt, Modifier.fillMaxWidth(), placeholder = { Text("Open an app, inspect the screen...") }, minLines = 3, shape = RoundedCornerShape(16.dp)); Spacer(Modifier.height(10.dp)); Row { Button(onClick = {}, enabled = prompt.isNotBlank(), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text("Run task") }; Spacer(Modifier.width(8.dp)); OutlinedButton(onClick = {}, shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.Stop, null); Spacer(Modifier.width(6.dp)); Text("Stop") } } } } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { MetricCard("$configured", "models ready", Modifier.weight(1f)); MetricCard("$toolCount", "tools", Modifier.weight(1f)); MetricCard("OFF", "remote mode", Modifier.weight(1f)) } }
        item { GlassCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("LIVE DEVICE VIEW", color = Cyan, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.sp); Spacer(Modifier.height(12.dp)); Box(Modifier.fillMaxWidth().height(165.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFF0C0E18)), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.PhoneAndroid, null, tint = Purple, modifier = Modifier.size(38.dp)); Text("Screen capture permission required", color = Color.LightGray); Text("Enable it from Access", color = Color.Gray, fontSize = 12.sp) } } } } }
    }
}

@Composable private fun MetricCard(value: String, label: String, modifier: Modifier) { GlassCard(modifier) { Column(Modifier.padding(14.dp)) { Text(value, style = MaterialTheme.typography.titleLarge, color = Purple, fontWeight = FontWeight.Bold); Text(label, color = Color.LightGray, fontSize = 12.sp) } } }

@Composable private fun Providers(items: List<com.androidaiagent.data.Provider>) { LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) { item { Text("Choose the brain for each task. Vision-capable models can understand approved screenshots.", color = Color.LightGray) }; items(items) { p -> GlassCard(Modifier.fillMaxWidth()) { Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFF292D4A)), contentAlignment = Alignment.Center) { Text(p.name.take(1), color = Cyan, fontWeight = FontWeight.Bold) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(p.name, fontWeight = FontWeight.Bold); Text(p.models.joinToString(" • "), color = Color.LightGray, fontSize = 12.sp); Text(if (p.vision) "Vision + text" else "Text / reasoning", color = if (p.vision) Cyan else Color.Gray, fontSize = 12.sp) }; OutlinedButton(onClick = {}, shape = RoundedCornerShape(12.dp)) { Text(if (p.configured) "Edit" else "Setup") } } } } } }

@Composable private fun Tools(items: androidx.compose.runtime.snapshots.SnapshotStateList<Tool>) { var showAdd by remember { mutableStateOf(false) }; var name by remember { mutableStateOf("") }; var description by remember { mutableStateOf("") }; LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) { item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Build your tool belt", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Schema-first tools are safer than arbitrary code.", color = Color.LightGray) }; Button(onClick = { showAdd = true }, shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.Add, null); Text(" Add") } } }; items(items) { t -> GlassCard(Modifier.fillMaxWidth()) { ListItem(headlineContent = { Text(t.name, fontWeight = FontWeight.Bold) }, supportingContent = { Text(t.description) }, trailingContent = { Text(t.type, color = Cyan, fontSize = 12.sp) }, colors = ListItemDefaults.colors(containerColor = Color.Transparent)) } } }; if (showAdd) AlertDialog(onDismissRequest = { showAdd = false }, title = { Text("Add custom tool") }, text = { Column { OutlinedTextField(name, { name = it }, label = { Text("Name") }); OutlinedTextField(description, { description = it }, label = { Text("Description") }) } }, confirmButton = { Button(onClick = { if (name.isNotBlank()) { items.add(Tool(java.util.UUID.randomUUID().toString(), name, description, "{\"type\":\"object\"}", "custom")); name = ""; description = ""; showAdd = false } }) { Text("Add") } }, dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }) }

@Composable private fun AccessScreen(openAccessibility: () -> Unit, openCapture: () -> Unit) { LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 20.dp)) { item { GlassCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("The agent is permission-first", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Android limits what any app can do. Turn on only what you understand; every capability degrades safely when unavailable.", color = Color.LightGray) } } }; item { PermissionCard("Accessibility service", "Read UI nodes, click, scroll and type", "Required for UI control", Icons.Default.TouchApp, openAccessibility) }; item { PermissionCard("Screen capture", "Let the agent see an approved screen", "MediaProjection consent is required each time", Icons.Default.ScreenShare, openCapture) }; item { PermissionCard("ADB bridge", "Optional power-user integration", "Not full access by default — see setup below", Icons.Default.Terminal, {}) }; item { GlassCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("ADB CONNECT — POWER USER", color = Cyan, fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); Text("A normal Android app cannot silently run adb or bypass the Android security model. For advanced automation, connect a separate computer or user-owned local bridge with Wireless debugging / USB debugging.", color = Color.LightGray); Spacer(Modifier.height(8.dp)); Text("1. Enable Developer options and Wireless debugging.\n2. Pair using the Android pairing code on your computer.\n3. Keep the bridge on your private network.\n4. Authenticate and approve every destructive action.", color = Color.White); Spacer(Modifier.height(10.dp)); Text("This does not guarantee unrestricted control. Android, OEM policy, app sandboxing and user approval still apply.", color = Color(0xFFFFC857), fontSize = 12.sp) } } } } }

@Composable private fun PermissionCard(title: String, detail: String, status: String, icon: androidx.compose.ui.graphics.vector.ImageVector, action: () -> Unit) { GlassCard(Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Purple, modifier = Modifier.size(30.dp)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold); Text(detail, color = Color.LightGray, fontSize = 13.sp); Text(status, color = Color(0xFFFFC857), fontSize = 12.sp) }; OutlinedButton(onClick = action, shape = RoundedCornerShape(12.dp)) { Text("Open") } } } }

@Composable private fun SettingsScreen(settingsLauncher: androidx.activity.result.ActivityResultLauncher<Intent>) { LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) { item { GlassCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(18.dp)) { Text("Safety defaults", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Remote control is disabled by default. Screenshot sharing requires explicit approval. Sensitive values should never be placed in logs.", color = Color.LightGray) } } }; item { SettingRow("Server endpoint", "http://10.0.2.2:8787", Icons.Default.Cloud) }; item { SettingRow("Action verification", "Always verify after an action", Icons.Default.Verified) }; item { SettingRow("Theme", "Aurora dark", Icons.Default.Palette) }; item { Button(onClick = { settingsLauncher.launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = android.net.Uri.parse("package:com.androidaiagent") }) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.Tune, null); Spacer(Modifier.width(8.dp)); Text("Open Android app settings") } } } }

@Composable private fun SettingRow(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) { GlassCard(Modifier.fillMaxWidth()) { Row(Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Cyan); Spacer(Modifier.width(12.dp)); Column { Text(title, fontWeight = FontWeight.Bold); Text(value, color = Color.LightGray, fontSize = 13.sp) } } } }
