package com.androidaiagent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.androidaiagent.agent.ProviderCatalog
import com.androidaiagent.data.Tool

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { AgentApp() } }
}
@Composable fun AgentApp() {
    var tab by remember { mutableIntStateOf(0) }; var prompt by remember { mutableStateOf("") }
    val providers = remember { ProviderCatalog().defaults() }
    val tools = remember { mutableStateListOf(Tool("screen", "Analyze screen", "Combine screenshot and accessibility tree", "{\"type\":\"object\"}", "built-in"), Tool("device", "Device info", "Read non-sensitive device capabilities", "{\"type\":\"object\"}", "built-in")) }
    Scaffold(topBar={ TopAppBar(title={Text("Android AI Agent")}, actions={ IconButton(onClick={}){Icon(Icons.Default.Settings,"Settings")} })}, bottomBar={NavigationBar{listOf("Dashboard","Providers","Tools","Permissions").forEachIndexed { i,n -> NavigationBarItem(selected=i==tab,onClick={tab=i},icon={Icon(if(i==0)Icons.Default.Home else if(i==1)Icons.Default.Cloud else if(i==2)Icons.Default.Build else Icons.Default.Lock,n)},label={Text(n)})}}}) { p -> Column(Modifier.padding(p).padding(16.dp)) { when(tab){0->Dashboard(prompt,{prompt=it},providers.count{it.configured},tools.size);1->Providers(providers);2->Tools(tools);else->Permissions()} } } }
}
@Composable private fun Dashboard(prompt:String,onPrompt:(String)->Unit,configured:Int,toolCount:Int){ Text("Agent control center",style=MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(12.dp)); Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("Ready",color=MaterialTheme.colorScheme.primary);Text("Provider $configured configured • $toolCount tools available");Spacer(Modifier.height(12.dp));OutlinedTextField(prompt,onPrompt,Modifier.fillMaxWidth(),label={Text("What should the agent do?")},minLines=3);Spacer(Modifier.height(8.dp));Row{Button(onClick={},enabled=prompt.isNotBlank()){Text("Run task")};Spacer(Modifier.width(8.dp));OutlinedButton(onClick={}){Text("Stop")}}}};Spacer(Modifier.height(16.dp));Text("Live screen",style=MaterialTheme.typography.titleLarge);Card(Modifier.fillMaxWidth().height(180.dp)){Box(Modifier.fillMaxSize(),contentAlignment=androidx.compose.ui.Alignment.Center){Text("Screen capture permission required")}};Spacer(Modifier.height(16.dp));Text("Capabilities",style=MaterialTheme.typography.titleLarge);Text("Accessibility: Permission required\nScreen capture: Permission required\nShell: Limited to app process\nRemote control: Off by default")}
@Composable private fun Providers(items:List<com.androidaiagent.data.Provider>){Text("AI providers",style=MaterialTheme.typography.headlineMedium);Text("Keys are stored on the server in its secret store, never in the APK.");LazyColumn{items(items){p->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Row(Modifier.padding(16.dp),horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Text(p.name,style=MaterialTheme.typography.titleMedium);Text(p.models.joinToString(" • "));Text(if(p.vision)"Vision supported" else "Text only")};Button(onClick={}){Text(if(p.configured)"Edit" else"Configure")}}}}}}
@Composable private fun Tools(items: androidx.compose.runtime.snapshots.SnapshotStateList<Tool>){
    var showAdd by remember { mutableStateOf(false) }; var name by remember { mutableStateOf("") }; var description by remember { mutableStateOf("") }
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Tools",style=MaterialTheme.typography.headlineMedium);Button(onClick={showAdd=true}){Text("Add tool")}}
    Text("Built-in tools are capability checked. Custom tools use a schema and require explicit approval.")
    LazyColumn{items(items){t->ListItem(headlineContent={Text(t.name)},supportingContent={Text(t.description)},trailingContent={Text(t.type)})}}
    if(showAdd) AlertDialog(onDismissRequest={showAdd=false},title={Text("Add custom tool")},text={Column{OutlinedTextField(name,{name=it},label={Text("Name")});OutlinedTextField(description,{description=it},label={Text("Description")})}},confirmButton={Button(onClick={if(name.isNotBlank()){items.add(Tool(java.util.UUID.randomUUID().toString(),name,description,"{\\"type\\":\\"object\\"}","custom"));name="";description="";showAdd=false}}){Text("Add")}},dismissButton={TextButton(onClick={showAdd=false}){Text("Cancel")}})
}
@Composable private fun Permissions(){Text("Permissions & safety",style=MaterialTheme.typography.headlineMedium);listOf("Accessibility service","Screen capture (MediaProjection)","Notifications","Remote connection").forEach{Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){ListItem(headlineContent={Text(it)},supportingContent={Text("Not enabled — Android will ask for your approval")},trailingContent={Button(onClick={}){Text("Open")}})}};Text("No root or unrestricted shell is claimed. Every action is logged and can be stopped.",Modifier.padding(top=16.dp))}
