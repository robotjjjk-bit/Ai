mermaid.initialize({startOnLoad:false});
const $=id=>document.getElementById(id);
const store={get(k,d){try{return JSON.parse(localStorage.getItem(k))??d}catch{return d}},set(k,v){localStorage.setItem(k,JSON.stringify(v))}};
let providers=store.get('nex_providers',[
 {name:'OpenAI',baseUrl:'https://api.openai.com',kind:'OPENAI',models:['gpt-4o-mini','gpt-4o'],key:'',headers:{}},
 {name:'Ollama Local',baseUrl:'http://localhost:11434',kind:'OLLAMA',models:['llama3.1','qwen2.5'],key:'',headers:{}}]);
let thread=store.get('nex_thread',[]); let branches={};
function save(){store.set('nex_providers',providers);store.set('nex_thread',thread)}
function refreshProv(){
 const ps=$('providerSel');ps.innerHTML=providers.map((p,i)=>`<option value="${i}">${p.name}</option>`).join('');
 const ms=$('modelSel');const cur=providers[+ps.value||0];
 ms.innerHTML=(cur?.models||[]).map(m=>`<option>${m}</option>`).join('');
 $('provList').innerHTML=providers.map((p,i)=>`<div class="msg"><span class="role">${p.kind}</span><br><b>${p.name}</b><br><span class="muted">${p.baseUrl} · ${(p.models||[]).join(', ')}</span><br><button data-del="${i}" class="btn-ghost">Remove</button></div>`).join('');
 document.querySelectorAll('[data-del]').forEach(b=>b.onclick=()=>{providers.splice(+b.dataset.del,1);save();refreshProv()});
}
function mdRender(el,text){
 el.innerHTML=marked.parse(text);
 el.querySelectorAll('pre code.language-mermaid').forEach(c=>{const d=document.createElement('div');d.className='mermaid';d.textContent=c.textContent;c.prepend(d);c.textContent=''});
 if(window.mermaid)mermaid.run();
}
function paint(){
 const t=$('thread');t.innerHTML='';
 thread.forEach((m,i)=>{
  const d=document.createElement('div');d.className='msg '+(m.role==='assistant'?'assistant':'');
  const vers=(branches[m.id]?.length||0);
  d.innerHTML=`<span class="role">${m.role}${vers?` · ${vers+1} versions`:''}</span><div class="body"></div>`;
  mdRender(d.querySelector('.body'),m.content);
  if(m.role==='assistant'){const b=document.createElement('button');b.className='btn-ghost';b.textContent='Retry as branch';b.onclick=()=>retry(i);d.appendChild(b)}
  t.appendChild(d);
 });
 t.scrollTop=t.scrollHeight;
 const vars=[...($('input').value.matchAll(/\{\{\s*(\w+)\s*\}\}/g))].map(m=>m[1]);
 $('varBox').innerHTML=vars.length?vars.map(v=>`<label>${v}<input data-var="${v}"></label>`).join(''):'No variables in draft.';
}
async function retry(i){
 const p=providers[+$('providerSel').value];const prev=thread[i-1]?.content||'Continue.';
 (branches[thread[i].id]=branches[thread[i].id]||[]).push(thread[i].content);
 $('branchBox').textContent=`${branches[thread[i].id].length+1} versions kept for answer ${i}.`;
 await callModel(p,prev,true,i);
}
let attached='';
$('file').onchange=async e=>{
 const f=e.target.files[0];if(!f)return;
 if(f.type.startsWith('image/')){attached=`[image ${f.name} attached — vision models only]`;$('attachNote').textContent=`Image ${f.name} ready.`}
 else{$('attachNote').textContent=`Reading ${f.name}…`;attached='\n\nFile '+f.name+':\n'+(await f.text()).slice(0,9000);$('attachNote').textContent=`${f.name} attached.`}
};
function fillVars(text){
 const vals={};document.querySelectorAll('[data-var]').forEach(i=>vals[i.dataset.var]=i.value||'');
 return text.replace(/\{\{\s*(\w+)\s*\}\}/g,(_,k)=>vals[k]??$('memory').value??'');
}
async function callModel(p,prompt,isRetry=false,idx=null){
 const key=p.key||'';const url=p.baseUrl.replace(/\/$/,'')+(p.kind==='ANTHROPIC'?'/v1/messages':'/v1/chat/completions');
 const body=p.kind==='ANTHROPIC'
  ?{model:$('modelSel').value,max_tokens:1024,stream:false,messages:[{role:'user',content:prompt}]}
  :{model:$('modelSel').value,stream:false,messages:[{role:'system',content:'Workspace:\n'+$('work').value+'\nMemory: '+$('memory').value},{role:'user',content:prompt}]};
 try{
  const h={'Content-Type':'application/json',...(p.headers||{})};
  if(key)h[p.kind==='ANTHROPIC'?'x-api-key':'Authorization']=p.kind==='ANTHROPIC'?key:`Bearer ${key}`;
  if(p.kind==='ANTHROPIC')h['anthropic-version']='2023-06-01';
  const r=await fetch(url,{method:'POST',headers:h,body:JSON.stringify(body)});
  const j=await r.json();
  const text=p.kind==='ANTHROPIC'?j.content?.map(c=>c.text).join(''):(j.choices?.[0]?.message?.content||JSON.stringify(j).slice(0,2000));
  if(isRetry){thread[idx]={...thread[idx],content:text};branches[thread[idx].id].push(text)}
  else thread.push({role:'assistant',content:text,id:crypto.randomUUID()});
 }catch(err){thread.push({role:'assistant',content:'Request failed: '+err.message+' — check base URL, key, and CORS.',id:crypto.randomUUID()})}
 save();paint();
}
$('sendBtn').onclick=async()=>{
 let v=$('input').value.trim();if(!v)return;
 v=fillVars(v)+(attached?attached:'');attached='';
 thread.push({role:'user',content:v,id:crypto.randomUUID()});$('input').value='';save();paint();
 await callModel(providers[+$('providerSel').value||0],v);
};
$('newChatBtn').onclick=()=>{thread=[];save();paint()};
$('providerSel').onchange=()=>{const c=providers[+$('providerSel').value];$('modelSel').innerHTML=(c.models||[]).map(m=>`<option>${m}</option>`).join('')};
$('pSave').onclick=()=>{try{providers.push({name:$('pName').value,baseUrl:$('pUrl').value,kind:$('pKind').value,models:$('pModels').value.split(',').map(s=>s.trim()),key:$('pKey').value,headers:JSON.parse($('pHeaders').value||'{}')});save();refreshProv()}catch(e){alert('Headers must be JSON')}};
$('pQr').onclick=()=>{const p=providers[providers.length-1]||providers[0];QRCode.toCanvas($('qr'),JSON.stringify({v:1,...p,key:undefined}))};
$('pImportBtn').onclick=()=>{try{const j=JSON.parse($('pImport').value);providers.push({...j,models:Array.isArray(j.models)?j.models:String(j.models||'').split(',')});save();refreshProv()}catch{alert('Invalid JSON')}};
$('mcpBtn').onclick=async()=>{try{const r=await fetch($('mcpUrl').value,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({jsonrpc:'2.0',id:1,method:'tools/list',params:{}})});$('mcpOut').textContent=(await r.text()).slice(0,2000)}catch(e){$('mcpOut').textContent='MCP error: '+e.message}};
document.querySelectorAll('.acc button').forEach(b=>b.onclick=()=>b.parentElement.classList.toggle('open'));
const io=new IntersectionObserver(es=>es.forEach(e=>{if(e.isIntersecting)e.target.classList.add('in')}),{threshold:.1});
document.querySelectorAll('.reveal').forEach((el,i)=>{el.style.transitionDelay=(i*80)+'ms';io.observe(el)});
document.addEventListener('keydown',e=>{if((e.ctrlKey||e.metaKey)&&e.key==='k'){e.preventDefault();$('modelSel').focus()}});
refreshProv();paint();
