import express from 'express';
import cors from 'cors';
import crypto from 'node:crypto';

const app = express(); app.use(cors()); app.use(express.json({limit:'2mb'}));
const port = Number(process.env.PORT || 8787);
const keyFor = id => process.env[`AI_KEY_${id.toUpperCase()}`] || '';
const providerDefaults = [
  ['openai','OpenAI / GPT','openai-compatible','https://api.openai.com/v1',['gpt-4.1','gpt-4.1-mini','gpt-4o','gpt-4o-mini','o3','o4-mini'],true],
  ['google','Google Gemini','gemini','https://generativelanguage.googleapis.com/v1beta',['gemini-2.5-pro','gemini-3.1-pro','gemini-3.6-flash','gemini-2.5-flash'],true],
  ['deepseek','DeepSeek','openai-compatible','https://api.deepseek.com/v1',['deepseek-chat','deepseek-reasoner','deepseek-v3.1','deepseek-r1'],false],
  ['anthropic','Anthropic Claude','anthropic','https://api.anthropic.com/v1',['claude-sonnet-4-20250514','claude-opus-4-20250514','claude-3-7-sonnet-20250219','claude-3-5-haiku-20241022'],true],
  ['moonshot','Kimi / Moonshot','openai-compatible','https://api.moonshot.ai/v1',['kimi-k2','kimi-k2-thinking','moonshot-v1-8k','moonshot-v1-32k','moonshot-v1-128k'],false],
  ['minimax','MiniMax','openai-compatible','https://api.minimax.io/v1',['MiniMax-Text-01','MiniMax-M2','MiniMax-M2.1','MiniMax-VL-01'],true],
  ['custom','Custom / Local','openai-compatible',process.env.CUSTOM_BASE_URL || 'http://host.docker.internal:11434/v1',['llama3.3','qwen3','mistral-small','deepseek-r1','local-model'],false]
].map(([id,name,kind,baseUrl,models,vision])=>({id,name,kind,baseUrl,models,vision,configured:Boolean(keyFor(id))}));
let providers = providerDefaults.map(p=>({...p, apiKey:keyFor(p.id)}));
let tools = [
 {id:'get_screen',name:'Get screen',description:'Returns screenshot and accessibility tree when permissions are available.',type:'built-in',inputSchema:{type:'object',properties:{}},enabled:true},
 {id:'analyze_screen',name:'Analyze screen',description:'Sends an explicitly approved screenshot to a vision-capable provider.',type:'built-in',inputSchema:{type:'object',properties:{question:{type:'string'}}},enabled:true},
 {id:'device_info',name:'Device information',description:'Returns non-sensitive device capability information.',type:'built-in',inputSchema:{type:'object',properties:{}},enabled:true},
 {id:'wait',name:'Wait',description:'Waits for a bounded duration.',type:'built-in',inputSchema:{type:'object',properties:{milliseconds:{type:'integer',minimum:1,maximum:10000}},required:['milliseconds']},enabled:true}
];
const templates = [
 {id:'http-json',name:'HTTP JSON tool',description:'Call an allowlisted HTTPS JSON endpoint through your server.',type:'http',inputSchema:{type:'object',properties:{url:{type:'string'},method:{enum:['GET','POST']},body:{type:'object'}},required:['url'] }},
 {id:'prompt',name:'Prompt tool',description:'Turn a structured action into an instruction for the selected model.',type:'prompt',inputSchema:{type:'object',properties:{instruction:{type:'string'}},required:['instruction']}}
];
const publicProvider = ({apiKey,...p})=>p;
app.get('/health',(req,res)=>res.json({ok:true,service:'android-ai-agent-server',version:'0.1.0'}));
app.get('/api/providers',(req,res)=>res.json(providers.map(publicProvider)));
app.put('/api/providers/:id',(req,res)=>{const p=providers.find(x=>x.id===req.params.id);if(!p)return res.status(404).json({error:'PROVIDER_NOT_FOUND'});const b=req.body||{};if(b.baseUrl&&!/^https?:\/\//i.test(b.baseUrl))return res.status(400).json({error:'INVALID_BASE_URL'});Object.assign(p,{baseUrl:b.baseUrl??p.baseUrl,models:Array.isArray(b.models)?b.models:p.models,apiKey:typeof b.apiKey==='string'?b.apiKey:p.apiKey,vision:typeof b.vision==='boolean'?b.vision:p.vision});p.configured=Boolean(p.apiKey);res.json(publicProvider(p));});
app.get('/api/tools',(req,res)=>res.json({tools,templates}));
app.post('/api/tools',(req,res)=>{const b=req.body||{};if(!b.name||!b.description||!b.inputSchema)return res.status(400).json({error:'NAME_DESCRIPTION_SCHEMA_REQUIRED'});if(b.type==='code'||b.execute) return res.status(400).json({error:'ARBITRARY_CODE_DISABLED',message:'Tools are declarative; server-side code is not accepted.'});const t={id:b.id||crypto.randomUUID(),name:b.name,description:b.description,type:b.type||'prompt',inputSchema:b.inputSchema,endpoint:b.endpoint,enabled:b.enabled!==false};tools.push(t);res.status(201).json(t);});
app.delete('/api/tools/:id',(req,res)=>{const n=tools.length;tools=tools.filter(t=>t.id!==req.params.id);res.status(n===tools.length?404:204).end();});
app.post('/api/chat',async(req,res)=>{const {providerId='openai',model,prompt,imageBase64,approved=false}=req.body||{};const p=providers.find(x=>x.id===providerId);if(!p)return res.status(404).json({error:'PROVIDER_NOT_FOUND'});if(!p.apiKey)return res.status(409).json({error:'PROVIDER_NOT_CONFIGURED'});if(imageBase64&&!approved)return res.status(403).json({error:'SCREEN_SHARING_NOT_APPROVED'});try { const answer=await callProvider(p,model||p.models[0],prompt,imageBase64);res.json({ok:true,provider:providerId,model:model||p.models[0],answer}); } catch(e){res.status(502).json({error:'PROVIDER_REQUEST_FAILED',message:e.message});}});
async function callProvider(p,model,prompt,imageBase64){const content=imageBase64?[{type:'text',text:prompt},{type:'image_url',image_url:{url:`data:image/jpeg;base64,${imageBase64}`}}]:prompt;let url,headers={'content-type':'application/json'};let body;
 if(p.kind==='anthropic'){url=p.baseUrl+'/messages';headers['x-api-key']=p.apiKey;headers['anthropic-version']='2023-06-01';body={model,max_tokens:2048,messages:[{role:'user',content:imageBase64?[{type:'text',text:prompt},{type:'image',source:{type:'base64',media_type:'image/jpeg',data:imageBase64}}]:[{type:'text',text:prompt}]}]};}
 else if(p.kind==='gemini'){url=`${p.baseUrl}/models/${model}:generateContent?key=${encodeURIComponent(p.apiKey)}`;body={contents:[{role:'user',parts:imageBase64?[{text:prompt},{inline_data:{mime_type:'image/jpeg',data:imageBase64}}]:[{text:prompt}]}]};}
 else {url=p.baseUrl+'/chat/completions';headers.authorization=`Bearer ${p.apiKey}`;body={model,messages:[{role:'user',content}],temperature:0.2};}
 const r=await fetch(url,{method:'POST',headers,body:JSON.stringify(body),signal:AbortSignal.timeout(30000)});const data=await r.json().catch(()=>({}));if(!r.ok)throw new Error(data.error?.message||data.message||`HTTP ${r.status}`);return p.kind==='anthropic'?data.content?.[0]?.text||'':p.kind==='gemini'?data.candidates?.[0]?.content?.parts?.map(x=>x.text||'').join('')||'':data.choices?.[0]?.message?.content||'';}
app.listen(port,'0.0.0.0',()=>console.log(`AI Agent server listening on 0.0.0.0:${port}`));
