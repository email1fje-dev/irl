package com.parsa.irl
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private const val API="https://backend-production-98e6.up.railway.app"
data class Quest(val id:String,val title:String,val description:String,val category:String,val difficulty:String,val xp:Int,val coins:Int,val proofType:String)

class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{IRLApp(this)}}}

@Composable fun IRLApp(c:Context){
 var quests by remember{mutableStateOf<List<Quest>>(emptyList())};var loading by remember{mutableStateOf(true)};var error by remember{mutableStateOf<String?>(null)};var selected by remember{mutableStateOf<Quest?>(null)}
 LaunchedEffect(loading){if(!loading)return@LaunchedEffect;try{val id=getUserId(c);Api.ensureUser(id);quests=Api.getQuests(id);error=null}catch(e:Exception){error=e.message?:"Connection failed"}finally{loading=false}}
 MaterialTheme(colorScheme=darkColorScheme(primary=Color(0xFF9B7BFF),secondary=Color(0xFF65D6FF),background=Color(0xFF0B0B12),surface=Color(0xFF151520))){Surface(Modifier.fillMaxSize()){if(selected!=null)ProofScreen(selected!!,c){selected=null;loading=true}else HomeScreen(quests,loading,error){selected=it}}}
}
@Composable fun HomeScreen(qs:List<Quest>,loading:Boolean,error:String?,onQuest:(Quest)->Unit){
 Column(Modifier.fillMaxSize().padding(20.dp)){Spacer(Modifier.height(18.dp));Text("IRL",style=MaterialTheme.typography.displaySmall);Text("Your real life. Your quests.",color=MaterialTheme.colorScheme.secondary);Spacer(Modifier.height(24.dp))
  if(loading)CircularProgressIndicator() else if(error!=null)Card(Modifier.fillMaxWidth()){Column(Modifier.padding(18.dp)){Text("Couldn’t connect",style=MaterialTheme.typography.titleLarge);Spacer(Modifier.height(6.dp));Text(error)}} else {Text("ACTIVE QUESTS",style=MaterialTheme.typography.labelLarge);Spacer(Modifier.height(10.dp));LazyColumn(verticalArrangement=Arrangement.spacedBy(12.dp)){items(qs){QuestCard(it,onQuest)}}}
 }
}
@Composable fun QuestCard(q:Quest,onQuest:(Quest)->Unit){Card(onClick={onQuest(q)},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(18.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(q.category.uppercase(),style=MaterialTheme.typography.labelMedium);Text(q.difficulty.uppercase(),color=MaterialTheme.colorScheme.secondary)};Spacer(Modifier.height(8.dp));Text(q.title,style=MaterialTheme.typography.titleLarge);Spacer(Modifier.height(6.dp));Text(q.description);Spacer(Modifier.height(14.dp));Text("+"+q.xp+" XP   •   +"+q.coins+" coins")}}}

@Composable fun ProofScreen(q:Quest,c:Context,onDone:()->Unit){
 var text by remember{mutableStateOf("")};var uri by remember{mutableStateOf<Uri?>(null)};var status by remember{mutableStateOf("")};var sending by remember{mutableStateOf(false)}
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri=it}
 Column(Modifier.fillMaxSize().padding(20.dp)){Text("QUEST",style=MaterialTheme.typography.labelLarge);Spacer(Modifier.height(8.dp));Text(q.title,style=MaterialTheme.typography.headlineSmall);Spacer(Modifier.height(8.dp));Text(q.description);Spacer(Modifier.height(24.dp))
  if(q.proofType=="photo"||q.proofType=="screenshot"){Button(onClick={picker.launch("image/*")}){Text(if(uri==null)"Choose proof image" else "Image selected ✓")};Spacer(Modifier.height(12.dp))}
  OutlinedTextField(value=text,onValueChange={text=it},modifier=Modifier.fillMaxWidth(),label={Text("Proof note (optional)")},minLines=3);Spacer(Modifier.height(18.dp))
  Button(onClick={sending=true},enabled=!sending&&(uri!=null||text.isNotBlank()),modifier=Modifier.fillMaxWidth()){Text(if(sending)"Checking..." else "Submit proof")}
  if(status.isNotBlank()){Spacer(Modifier.height(14.dp));Text(status)}
  LaunchedEffect(sending){if(sending)try{val r=Api.submitProof(getUserId(c),q,text,uri,c);status=if(r.first)"Accepted! +"+r.second+" XP 🎉" else "Rejected: "+r.third;if(r.first)onDone()}catch(e:Exception){status=e.message?:"Submission failed"}finally{sending=false}}
 }
}
private fun getUserId(c:Context):String{val p=c.getSharedPreferences("irl",Context.MODE_PRIVATE);return p.getString("user_id",null)?:UUID.randomUUID().toString().also{p.edit().putString("user_id",it).apply()}}

object Api{
 private val client=OkHttpClient()
 suspend fun ensureUser(id:String)=withContext(Dispatchers.IO){val b=JSONObject().put("id",id).put("display_name","Player").toString().toRequestBody("application/json".toMediaType());request(Request.Builder().url(API+"/api/users").post(b).build())}
 suspend fun getQuests(id:String):List<Quest>=withContext(Dispatchers.IO){val a=JSONArray(request(Request.Builder().url(API+"/api/users/"+id+"/quests").get().build()));buildList{for(i in 0 until a.length()){val o=a.getJSONObject(i);add(Quest(o.getString("id"),o.getString("title"),o.getString("description"),o.optString("category","Challenge"),o.optString("difficulty","Medium"),o.optInt("reward_xp"),o.optInt("reward_coins"),o.optString("proof_type","text")))}}}
 suspend fun submitProof(uid:String,q:Quest,text:String,uri:Uri?,c:Context):Triple<Boolean,Int,String>=withContext(Dispatchers.IO){val f=MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("proofType",q.proofType).addFormDataPart("textContent",text);if(uri!=null){val bytes=c.contentResolver.openInputStream(uri)?.use{it.readBytes()}?:throw Exception("Could not read image");val mime=c.contentResolver.getType(uri)?:"image/jpeg";f.addFormDataPart("proof","proof.jpg",bytes.toRequestBody(mime.toMediaType()))};val o=JSONObject(request(Request.Builder().url(API+"/api/users/"+uid+"/quests/"+q.id+"/proof").post(f.build()).build()));Triple(o.optBoolean("accepted",o.optString("verdict")=="accepted"),o.optInt("xp_awarded"),o.optString("reason",""))}
 private fun request(r:Request):String{client.newCall(r).execute().use{res->val b=res.body?.string().orEmpty();if(!res.isSuccessful)throw Exception("HTTP "+res.code+": "+b);return b}}
}
