package com.parsa.irl
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID

private const val API="https://backend-production-98e6.up.railway.app"
data class Quest(val id:String,val title:String,val description:String,val category:String,val difficulty:String,val xp:Int,val coins:Int,val proofType:String)
data class User(val name:String,val level:Int,val xp:Int,val coins:Int,val streak:Int)

class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{IRLApp(this)}}}

private val Bg=Color(0xFF07070C)
private val Panel2=Color(0xFF1A1A26)
private val Pink=Color(0xFFFF6BAE)
private val Muted=Color(0xFF858596)
private val Panel=Color(0xFF11111A)
private val Purple=Color(0xFF9B7BFF)
private val Cyan=Color(0xFF65D6FF)
private val Pink=Color(0xFFFF6BAE)

@Composable fun IRLApp(c:Context){
 var quests by remember{mutableStateOf<List<Quest>>(emptyList())};var user by remember{mutableStateOf<User?>(null)};var achievements by remember{mutableStateOf<List<String>>(emptyList())};var loading by remember{mutableStateOf(true)};var error by remember{mutableStateOf<String?>(null)};var selected by remember{mutableStateOf<Quest?>(null)};var tab by remember{mutableIntStateOf(0)}
 LaunchedEffect(loading){if(!loading)return@LaunchedEffect;try{val id=getUserId(c);Api.ensureUser(id);user=Api.getUser(id);quests=Api.getQuests(id);achievements=Api.getAchievements(id);error=null}catch(e:Exception){error=e.message?:"Connection failed"}finally{loading=false}}
 MaterialTheme(colorScheme=darkColorScheme(primary=Purple,secondary=Cyan,background=Bg,surface=Panel)){Surface(Modifier.fillMaxSize(),color=Bg){
  if(selected!=null)ProofScreen(selected!!,c){selected=null;loading=true}
  else Scaffold(containerColor=Bg,bottomBar={BottomBar(tab){tab=it}}){pad->Box(Modifier.fillMaxSize().padding(pad)){when(tab){0->HomeScreen(quests,user,loading,error){selected=it};1->WorldScreen(user);2->LootScreen(user);3->BadgeScreen(user,achievements);else->ProfileScreen(user)}}}
 }}
}

@Composable fun BottomBar(selected:Int,onSelect:(Int)->Unit){
 NavigationBar(containerColor=Panel){listOf(Icons.Default.Home to "HOME",Icons.Default.Explore to "WORLD",Icons.Default.Inventory2 to "LOOT",Icons.Default.EmojiEvents to "BADGES",Icons.Default.Person to "PROFILE").forEachIndexed{i,p->NavigationBarItem(selected==i,{onSelect(i)},icon={Icon(p.first,null)},label={Text(p.second,fontSize=9.sp)})}}
}

@Composable fun HeroArt(title:String,subtitle:String,icon:ImageVector,modifier:Modifier=Modifier){
 Box(modifier.clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(listOf(Color(0xFF251A45),Color(0xFF102B38),Color(0xFF341528))))){
  Column(Modifier.fillMaxSize().padding(22.dp),verticalArrangement=Arrangement.SpaceBetween){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column{Text("IRL",fontSize=14.sp,fontWeight=FontWeight.Black);Text(title,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Black);Text(subtitle,color=Color.White.copy(alpha=.72f))};Box(Modifier.size(68.dp).clip(RoundedCornerShape(22.dp)).background(Color.White.copy(alpha=.10f)),contentAlignment=Alignment.Center){Icon(icon,null,Modifier.size(40.dp),tint=Cyan)}};Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){repeat(5){Box(Modifier.size(7.dp).clip(RoundedCornerShape(4.dp)).background(if(it<3)Cyan else Color.White.copy(alpha=.2f)))}}}
 }
}

@Composable fun HomeScreen(qs:List<Quest>,u:User?,loading:Boolean,error:String?,onQuest:(Quest)->Unit){
 LazyColumn(Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(14.dp),contentPadding=PaddingValues(top=16.dp,bottom=18.dp)){
  item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column{Text("GOOD TO SEE YOU",style=MaterialTheme.typography.labelSmall,color=Muted,fontWeight=FontWeight.Bold);Text("IRL",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Black);Text("REAL LIFE // RPG",style=MaterialTheme.typography.labelSmall,color=Cyan)};Surface(shape=RoundedCornerShape(14.dp),color=Panel){Text("🔥 "+(u?.streak?:0),Modifier.padding(horizontal=12.dp,vertical=9.dp),fontWeight=FontWeight.Bold)}}}
  item{Box(Modifier.fillMaxWidth().height(205.dp).clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(listOf(Color(0xFF2A1B4D),Color(0xFF102E38),Color(0xFF32152B))))){Column(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.SpaceBetween){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column{Text("IRL",fontSize=13.sp,fontWeight=FontWeight.Black,color=Cyan);Text("REAL LIFE RPG",fontSize=27.sp,fontWeight=FontWeight.Black);Text("Turn ordinary moments into missions.",fontSize=12.sp,color=Color.White.copy(.68f))};Box(Modifier.size(66.dp).clip(RoundedCornerShape(22.dp)).background(Color.White.copy(.09f)),contentAlignment=Alignment.Center){Icon(Icons.Default.AutoAwesome,null,Modifier.size(38.dp),tint=Purple)}};Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){listOf("LV "+(u?.level?:1),"🔥 "+(u?.streak?:0),"★ "+(u?.xp?:0)+" XP").forEach{Surface(color=Color.Black.copy(.20f),shape=RoundedCornerShape(12.dp)){Text(it,Modifier.padding(horizontal=10.dp,vertical=7.dp),fontSize=10.sp,fontWeight=FontWeight.Bold)}}}}}}
  item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){StatCard("LVL",(u?.level?:1).toString(),Icons.Default.Bolt,Modifier.weight(1f));StatCard("XP",(u?.xp?:0).toString(),Icons.Default.Star,Modifier.weight(1f));StatCard("COINS",(u?.coins?:0).toString(),Icons.Default.MonetizationOn,Modifier.weight(1f))}}
  item{Text("ACTIVE QUESTS",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Black);Text("Complete them to unlock new missions.",color=Color.White.copy(alpha=.55f),style=MaterialTheme.typography.bodySmall)}
  if(loading)item{Box(Modifier.fillMaxWidth(),contentAlignment=Alignment.Center){CircularProgressIndicator()}}
  else if(error!=null)item{Card(colors=CardDefaults.cardColors(containerColor=Panel)){Column(Modifier.padding(18.dp)){Text("Couldn’t connect",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Spacer(Modifier.height(6.dp));Text(error)}}}
  else items(qs){QuestCard(it,onQuest)}
 }
}

@Composable fun StatCard(label:String,value:String,icon:ImageVector,modifier:Modifier){Card(modifier,shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=Panel)){Column(Modifier.padding(13.dp)){Icon(icon,null,tint=Cyan,modifier=Modifier.size(20.dp));Spacer(Modifier.height(8.dp));Text(value,fontSize=20.sp,fontWeight=FontWeight.Black);Text(label,fontSize=9.sp,color=Color.White.copy(alpha=.55f))}}}

@Composable fun QuestCard(q:Quest,onQuest:(Quest)->Unit){
 val art=when(q.category.lowercase()){"photo"->Icons.Default.PhotoCamera;"observation"->Icons.Default.Visibility;"creative"->Icons.Default.Palette;"gaming"->Icons.Default.SportsEsports;"brain"->Icons.Default.Psychology;"weird"->Icons.Default.BugReport;else->Icons.Default.Bolt}
 Card(onClick={onQuest(q)},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=Panel)){
  Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(74.dp).clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(listOf(Color(0xFF291B4C),Color(0xFF132C36)))),contentAlignment=Alignment.Center){Icon(art,null,Modifier.size(38.dp),tint=Cyan)}
   Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(q.category.uppercase(),fontSize=9.sp,color=Cyan,fontWeight=FontWeight.Bold);Text(q.difficulty.uppercase(),fontSize=9.sp,color=Pink,fontWeight=FontWeight.Bold)};Spacer(Modifier.height(5.dp));Text(q.title,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Black);Spacer(Modifier.height(4.dp));Text(q.description,maxLines=3,color=Color.White.copy(alpha=.70f),style=MaterialTheme.typography.bodySmall);Spacer(Modifier.height(7.dp));Text("+"+q.xp+" XP  •  +"+q.coins+" coins",fontSize=11.sp,fontWeight=FontWeight.Bold)}}}
}

@Composable fun ProofScreen(q:Quest,c:Context,onDone:()->Unit){
 var text by remember{mutableStateOf("")};var uri by remember{mutableStateOf<Uri?>(null)};var status by remember{mutableStateOf("")};var sending by remember{mutableStateOf(false)}
 val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri=it}
 LazyColumn(Modifier.fillMaxSize().padding(16.dp),contentPadding=PaddingValues(bottom=24.dp)){item{Row(verticalAlignment=Alignment.CenterVertically){Text("‹",style=MaterialTheme.typography.displaySmall);Spacer(Modifier.width(8.dp));Text("MISSION",fontWeight=FontWeight.Black,color=Cyan)};Spacer(Modifier.height(8.dp));HeroArt(q.title,q.category+" • "+q.difficulty,Icons.Default.Flag,Modifier.fillMaxWidth().height(150.dp));Spacer(Modifier.height(16.dp));Text(q.description,style=MaterialTheme.typography.bodyLarge);Spacer(Modifier.height(20.dp))}
  item{if(q.proofType=="photo"||q.proofType=="screenshot"){Button(onClick={picker.launch("image/*")},modifier=Modifier.fillMaxWidth().height(54.dp),shape=RoundedCornerShape(16.dp)){Icon(Icons.Default.AddAPhoto,null);Spacer(Modifier.width(8.dp));Text(if(uri==null)"Choose proof image" else "Image selected ✓")};Spacer(Modifier.height(12.dp))};OutlinedTextField(value=text,onValueChange={text=it},modifier=Modifier.fillMaxWidth(),label={Text("Proof note (optional)")},minLines=3);Spacer(Modifier.height(14.dp));Button(onClick={sending=true},enabled=!sending&&(uri!=null||text.isNotBlank()),modifier=Modifier.fillMaxWidth().height(56.dp),shape=RoundedCornerShape(16.dp)){Text(if(sending)"Checking..." else "SUBMIT PROOF",fontWeight=FontWeight.Black)}}
  item{if(status.isNotBlank()){Spacer(Modifier.height(14.dp));Card(colors=CardDefaults.cardColors(containerColor=Panel)){Text(status,Modifier.padding(16.dp))}}}
  item{LaunchedEffect(sending){if(sending)try{val r=Api.submitProof(getUserId(c),q,text,uri,c);status=if(r.first)"Accepted! +"+r.second+" XP 🎉" else "Rejected: "+r.third;if(r.first)onDone()}catch(e:Exception){status=e.message?:"Submission failed"}finally{sending=false}}}
 }
}

@Composable fun WorldScreen(u:User?){LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp),contentPadding=PaddingValues(bottom=20.dp)){item{HeroArt("WORLD MAP","Explore fictional zones.",Icons.Default.Explore,Modifier.fillMaxWidth().height(190.dp))};item{ZoneCard("🌆 Neon City","Observation + Creative","UNLOCKED")}
  item{HeroArt("GLITCH SIGNAL","Something is waiting for you.",Icons.Default.AutoAwesome,Modifier.fillMaxWidth().height(120.dp))};item{ZoneCard("🧠 Brain Lab","Brain + Challenge","UNLOCKED")};item{ZoneCard("👾 Glitch District","Weird + Secret",if((u?.level?:1)>=5)"UNLOCKED" else "LVL 5")};item{ZoneCard("👑 THE UNKNOWN","Hidden secret area",if((u?.streak?:0)>=7)"UNLOCKED" else "SECRET")};item{ZoneCard("👹 BOSS ZONE","Harder quests, safe challenges only","LVL 5")}}}
@Composable fun ZoneCard(a:String,b:String,c:String){Card(shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=Panel)){Column(Modifier.padding(18.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(a,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Black);Text(c,color=Cyan,fontSize=11.sp,fontWeight=FontWeight.Bold)};Spacer(Modifier.height(6.dp));Text(b,color=Color.White.copy(alpha=.65f))}}}
@Composable fun LootScreen(u:User?){val level=u?.level?:1;val streak=u?.streak?:0;LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp),contentPadding=PaddingValues(bottom=20.dp)){item{HeroArt("INVENTORY","Cosmetics and game rewards.",Icons.Default.Inventory2,Modifier.fillMaxWidth().height(190.dp))};item{ZoneCard("🪙 Coin Cache","Earned coins: "+(u?.coins?:0),"ACTIVE")};item{ZoneCard("⚡ XP Core","Level "+level,"ACTIVE")};item{ZoneCard("🎭 Glitch Mask","Secret cosmetic",if(level>=5)"UNLOCKED" else "LOCKED")};item{ZoneCard("🌌 Unknown Key","Secret cosmetic",if(streak>=7)"UNLOCKED" else "LOCKED")}}}
@Composable fun BadgeScreen(u:User?,a:List<String>){val level=u?.level?:1;val streak=u?.streak?:0;val coins=u?.coins?:0;val list=listOf("FIRST_QUEST" to "First Contact","STREAK_3" to "On Fire","STREAK_7" to "Seven Day Run","LEVEL_5" to "Glitch Hunter","COINS_100" to "Pocket NPC");LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp),contentPadding=PaddingValues(bottom=20.dp)){item{HeroArt("ACHIEVEMENTS","Collect badges by playing.",Icons.Default.EmojiEvents,Modifier.fillMaxWidth().height(190.dp))};items(list){p->val ok=a.contains(p.first)||(p.first=="STREAK_3"&&streak>=3)||(p.first=="STREAK_7"&&streak>=7)||(p.first=="LEVEL_5"&&level>=5)||(p.first=="COINS_100"&&coins>=100);ZoneCard(if(ok)"🏆 "+p.second else "🔒 "+p.second,p.first,if(ok)"UNLOCKED" else "LOCKED")}}}
@Composable fun ProfileScreen(u:User?){Column(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){HeroArt("PLAYER PROFILE","Your IRL character sheet.",Icons.Default.Person,Modifier.fillMaxWidth().height(190.dp));ZoneCard("PLAYER","Level "+(u?.level?:1)+" • "+(u?.xp?:0)+" XP","🔥 "+(u?.streak?:0)+" streak");ZoneCard("NPC PERSONALITIES","Quest Giver • Troll • Detective • Glitched NPC","AI");ZoneCard("ADAPTIVE AI","Difficulty adapts to your progress","ONLINE")}}

private fun getUserId(c:Context):String{val p=c.getSharedPreferences("irl",Context.MODE_PRIVATE);return p.getString("user_id",null)?:UUID.randomUUID().toString().also{p.edit().putString("user_id",it).apply()}}

object Api{
 private val client=OkHttpClient()
 suspend fun ensureUser(id:String)=withContext(Dispatchers.IO){val b=JSONObject().put("id",id).put("display_name","Player").toString().toRequestBody("application/json".toMediaType());request(Request.Builder().url(API+"/api/users").post(b).build())}
 suspend fun getUser(id:String)=withContext(Dispatchers.IO){val o=JSONObject(request(Request.Builder().url(API+"/api/users/"+id).get().build())).getJSONObject("user");User(o.optString("display_name","Player"),o.optInt("level",1),o.optInt("xp",0),o.optInt("coins",0),o.optInt("streak",0))}
 suspend fun getAchievements(id:String):List<String> = withContext(Dispatchers.IO){val a=JSONObject(request(Request.Builder().url(API+"/api/users/"+id+"/achievements").get().build())).getJSONArray("achievements");buildList{for(i in 0 until a.length())add(a.getJSONObject(i).optString("code"))}}
 suspend fun getQuests(id:String):List<Quest> = withContext(Dispatchers.IO){val a=JSONObject(request(Request.Builder().url(API+"/api/users/"+id+"/quests").get().build())).getJSONArray("quests");buildList{for(i in 0 until a.length()){val o=a.getJSONObject(i);add(Quest(o.getString("id"),o.getString("title"),o.getString("description"),o.optString("category","Challenge"),o.optString("difficulty","Medium"),o.optInt("reward_xp"),o.optInt("reward_coins"),o.optString("proof_type","text")))}}}
 suspend fun submitProof(uid:String,q:Quest,text:String,uri:Uri?,c:Context):Triple<Boolean,Int,String> = withContext(Dispatchers.IO){val f=MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("proof_type",q.proofType).addFormDataPart("text",text);if(uri!=null){val bytes=c.contentResolver.openInputStream(uri)?.use{it.readBytes()}?:throw Exception("Could not read image");val mime=c.contentResolver.getType(uri)?:"image/jpeg";f.addFormDataPart("proof","proof.jpg",bytes.toRequestBody(mime.toMediaType()))};val o=JSONObject(request(Request.Builder().url(API+"/api/users/"+uid+"/quests/"+q.id+"/proof").post(f.build()).build()));Triple(o.optBoolean("accepted",o.optString("verdict")=="accepted"),o.optJSONObject("reward")?.optInt("xp",0)?:0,o.optJSONObject("proof")?.optString("reason","")?:o.optString("error",""))}
 private fun request(r:Request):String{client.newCall(r).execute().use{res->val b=res.body?.string().orEmpty();if(!res.isSuccessful)throw Exception("HTTP "+res.code+": "+b);return b}}
}