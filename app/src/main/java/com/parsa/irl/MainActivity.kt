package com.parsa.irl

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

private const val API="https://backend-production-98e6.up.railway.app"
private val Ink=Color(0xFF0B0B10); private val Panel=Color(0xFF15151D)
private val Paper=Color(0xFFE9E1D1); private val Gold=Color(0xFFD6A84F)
private val Red=Color(0xFFB83B45); private val Muted=Color(0xFF96939A)

data class User(val name:String,val level:Int,val xp:Int,val coins:Int)
data class Case(val id:String,val title:String,val subtitle:String,val description:String,val location:String,val cover:String,val difficulty:String,val status:String,val score:Int)
data class Suspect(val id:String,val name:String,val role:String,val bio:String,val portrait:String)
data class Evidence(val id:String,val title:String,val description:String,val image:String,val type:String)
data class Interview(val question:String,val answer:String)

class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{DetectiveApp(this)}}}

@Composable fun DetectiveApp(c:Context){
 var user by remember{mutableStateOf<User?>(null)};var cases by remember{mutableStateOf<List<Case>>(emptyList())}
 var selected by remember{mutableStateOf<Case?>(null)};var loading by remember{mutableStateOf(true)};var error by remember{mutableStateOf<String?>(null)}
 LaunchedEffect(Unit){try{val id=getId(c);Api.ensureUser(id);user=Api.user(id);cases=Api.cases(id)}catch(e:Exception){error=e.message}finally{loading=false}}
 MaterialTheme(colorScheme=darkColorScheme(primary=Gold,background=Ink,surface=Panel,onSurface=Paper)){CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl){
  if(selected!=null)CaseScreen(c,selected!!){selected=null;loading=true}
  else Home(c,user,cases,loading,error){selected=it}
 }}
}

@Composable fun Home(c:Context,u:User?,cases:List<Case>,loading:Boolean,error:String?,open:(Case)->Unit){
 Scaffold(containerColor=Ink,bottomBar={NavigationBar(containerColor=Panel){NavigationBarItem(true,{},{icon={Icon(Icons.Default.FolderOpen,null)},label={Text("پرونده‌ها")});NavigationBarItem(false,{},icon={Icon(Icons.Default.Dashboard,null)},label={Text("میز کار")});NavigationBarItem(false,{},icon={Icon(Icons.Default.Person,null)},label={Text("کارآگاه")})}}){p->
  LazyColumn(Modifier.fillMaxSize().padding(p),contentPadding=PaddingValues(bottom=24.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
   item{Header(u)}
   item{Hero()}
   item{Text("پرونده‌های باز",fontSize=22.sp,fontWeight=FontWeight.Black,modifier=Modifier.padding(horizontal=18.dp))}
   if(loading)item{Box(Modifier.fillMaxWidth().height(180.dp),contentAlignment=Alignment.Center){CircularProgressIndicator(color=Gold)}}
   else if(error!=null)item{Card(Modifier.padding(18.dp),colors=CardDefaults.cardColors(containerColor=Panel)){Text("ارتباط با دفتر مرکزی برقرار نشد",fontWeight=FontWeight.Bold);Text(error?:"خطای ناشناخته",color=Muted)}}
   else items(cases){CaseCard(it,open)}
  }
 }
}

@Composable fun Header(u:User?){Row(Modifier.fillMaxWidth().padding(18.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
 Column{Text("دفتر تحقیقات",color=Muted,fontSize=12.sp);Text("کارآگاه ${u?.name?:"ناشناس"}",fontSize=25.sp,fontWeight=FontWeight.Black)}
 Surface(color=Panel,shape=RoundedCornerShape(14.dp)){Row(Modifier.padding(horizontal=12.dp,vertical=9.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Stars,null,tint=Gold);Spacer(Modifier.width(5.dp));Text("${u?.xp?:0} XP",fontWeight=FontWeight.Bold)}}
}}

@Composable fun Hero(){Box(Modifier.fillMaxWidth().height(230.dp).padding(horizontal=18.dp).clip(RoundedCornerShape(30.dp)).background(Brush.linearGradient(listOf(Color(0xFF342B22),Color(0xFF17151A),Color(0xFF411B22))))){
 Column(Modifier.fillMaxSize().padding(22.dp),verticalArrangement=Arrangement.SpaceBetween){
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column{Text("پرونده‌های تاریک",fontSize=13.sp,color=Gold,fontWeight=FontWeight.Bold);Text("حقیقت
پشت دروغ‌هاست.",fontSize=32.sp,color=Paper,fontWeight=FontWeight.Black,lineHeight=37.sp)};Icon(Icons.Default.Search,null,tint=Gold,modifier=Modifier.size(55.dp))}
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Tag("مدرک");Tag("بازجویی");Tag("استدلال")}
 }
}}

@Composable fun Tag(s:String){Surface(color=Color.White.copy(.08f),shape=RoundedCornerShape(20.dp)){Text(s,Modifier.padding(horizontal=12.dp,vertical=7.dp),fontSize=11.sp,color=Paper)}}

@Composable fun CaseCard(x:Case,open:(Case)->Unit){Card(Modifier.padding(horizontal=18.dp).fillMaxWidth().clickable{open(x)},shape=RoundedCornerShape(26.dp),colors=CardDefaults.cardColors(containerColor=Panel)){
 Column{AsyncImage(x.cover,null,Modifier.fillMaxWidth().height(185.dp),contentScale=ContentScale.Crop)
  Column(Modifier.padding(16.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(x.difficulty,color=Gold,fontWeight=FontWeight.Bold);Text(if(x.status=="solved")"حل شده" else "در حال تحقیق",color=if(x.status=="solved")Color(0xFF70C890) else Red,fontSize=12.sp,fontWeight=FontWeight.Bold)}
   Spacer(Modifier.height(6.dp));Text(x.title,fontSize=23.sp,fontWeight=FontWeight.Black);Text(x.subtitle,color=Muted);Spacer(Modifier.height(8.dp));Text(x.description,maxLines=2,color=Paper.copy(.72f));Spacer(Modifier.height(10.dp));Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Place,null,tint=Red,modifier=Modifier.size(17.dp));Spacer(Modifier.width(4.dp));Text(x.location,color=Muted,fontSize=12.sp)}}
 }}
}

@Composable fun CaseScreen(c:Context,case:Case,back:()->Unit){
 var suspects by remember{mutableStateOf<List<Suspect>>(emptyList())};var evidence by remember{mutableStateOf<List<Evidence>>(emptyList())};var selectedSuspect by remember{mutableStateOf<Suspect?>(null)}
 var question by remember{mutableStateOf("")};var answer by remember{mutableStateOf("")};var asking by remember{mutableStateOf(false)};var accuseMsg by remember{mutableStateOf("")};var selectedAccuse by remember{mutableStateOf<Suspect?>(null)}
 LaunchedEffect(case.id){val d=Api.caseDetail(case.id);suspects=d.first;evidence=d.second}
 if(selectedSuspect!=null){InterviewScreen(c,case,selectedSuspect!!,question,answer,asking,{question=it},{asking=true;answer="";}, {asking=false;answer=it},{selectedSuspect=null})}
 else{
 Scaffold(containerColor=Ink,topBar={TopAppBar(title={Text("پرونده",fontWeight=FontWeight.Bold)},navigationIcon={IconButton({back()}){Icon(Icons.Default.ArrowBack,null)}},colors=TopAppBarDefaults.topAppBarColors(containerColor=Ink))}){p->
  LazyColumn(Modifier.padding(p),contentPadding=PaddingValues(bottom=30.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
   item{AsyncImage(case.cover,null,Modifier.fillMaxWidth().height(240.dp),contentScale=ContentScale.Crop)}
   item{Column(Modifier.padding(horizontal=18.dp)){Text(case.title,fontSize=29.sp,fontWeight=FontWeight.Black,color=Paper);Text(case.subtitle,color=Gold,fontWeight=FontWeight.Bold);Spacer(Modifier.height(10.dp));Text(case.description,color=Paper.copy(.78f),fontSize=15.sp);Spacer(Modifier.height(12.dp));Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Tag(case.difficulty);Tag(case.location)}}}
   item{SectionTitle("مدارک پرونده","هر تصویر ممکن است یک جزئیات مهم داشته باشد.")}
   item{Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal=18.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)){evidence.forEach{EvidenceCard(it)}}}
   item{SectionTitle("مظنون‌ها","با آن‌ها حرف بزن. سؤال خودت را بپرس.")}
   items(suspects){s->SuspectCard(s){selectedSuspect=s}}
   item{SectionTitle("اتهام نهایی","وقتی مطمئن شدی، مظنون را انتخاب کن.")}
   item{Column(Modifier.padding(horizontal=18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){suspects.forEach{s->Button(onClick={selectedAccuse=s},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Panel),shape=RoundedCornerShape(16.dp)){Text("اتهام: ${s.name}",color=Paper)}}}}
   if(selectedAccuse!=null)item{AlertDialog(onDismissRequest={selectedAccuse=null},title={Text("ثبت اتهام؟")},text={Text("می‌خواهی ${selectedAccuse!!.name} را متهم کنی؟")},confirmButton={TextButton(onClick={val ss=selectedAccuse!!;selectedAccuse=null;LaunchedEffectOnce(c,case.id,ss.id){accuseMsg=it}}){Text("ثبت")}},dismissButton={TextButton(onClick={selectedAccuse=null}){Text("برگرد")}})}
   if(accuseMsg.isNotBlank())item{Card(Modifier.padding(horizontal=18.dp),colors=CardDefaults.cardColors(containerColor=if(accuseMsg.contains("درست"))Color(0xFF183B2B) else Color(0xFF3B1C20))){Text(accuseMsg,Modifier.padding(18.dp),fontWeight=FontWeight.Bold,color=Paper)}}
  }
 }}
}

@Composable fun SectionTitle(a:String,b:String){Column(Modifier.padding(horizontal=18.dp)){Text(a,fontSize=21.sp,fontWeight=FontWeight.Black,color=Paper);Text(b,color=Muted,fontSize=12.sp)}}

@Composable fun EvidenceCard(e:Evidence){Card(Modifier.width(240.dp),shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=Panel)){Column{AsyncImage(e.image,null,Modifier.fillMaxWidth().height(145.dp),contentScale=ContentScale.Crop);Column(Modifier.padding(13.dp)){Text(e.type,color=Gold,fontSize=10.sp,fontWeight=FontWeight.Bold);Text(e.title,fontWeight=FontWeight.Black,fontSize=17.sp);Text(e.description,maxLines=3,color=Muted,fontSize=12.sp)}}}}

@Composable fun SuspectCard(s:Suspect,click:()->Unit){Card(Modifier.padding(horizontal=18.dp).fillMaxWidth().clickable{click()},shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=Panel)){Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){AsyncImage(s.portrait,null,Modifier.size(78.dp).clip(RoundedCornerShape(18.dp)),contentScale=ContentScale.Crop);Spacer(Modifier.width(13.dp));Column(Modifier.weight(1f)){Text(s.name,fontSize=19.sp,fontWeight=FontWeight.Black);Text(s.role,color=Gold,fontSize=12.sp,fontWeight=FontWeight.Bold);Text(s.bio,maxLines=2,color=Muted,fontSize=12.sp)};Icon(Icons.Default.ChevronLeft,null,tint=Gold)}}}

@Composable fun InterviewScreen(c:Context,case:Case,s:Suspect,q:String,a:String,asking:Boolean,onQ:(String)->Unit,onAsk:()->Unit,onAnswer:(String)->Unit,back:()->Unit){
 Scaffold(containerColor=Ink,topBar={TopAppBar(title={Text("بازجویی",fontWeight=FontWeight.Bold)},navigationIcon={IconButton({back()}){Icon(Icons.Default.ArrowBack,null)}},colors=TopAppBarDefaults.topAppBarColors(containerColor=Ink))}){p->
 LazyColumn(Modifier.padding(p),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
  item{Row(verticalAlignment=Alignment.CenterVertically){AsyncImage(s.portrait,null,Modifier.size(92.dp).clip(RoundedCornerShape(24.dp)),contentScale=ContentScale.Crop);Spacer(Modifier.width(14.dp));Column{Text(s.name,fontSize=25.sp,fontWeight=FontWeight.Black);Text(s.role,color=Gold);Text("مظنون پرونده ${case.title}",color=Muted,fontSize=11.sp)}}}
  item{Card(colors=CardDefaults.cardColors(containerColor=Panel)){Text("«${s.bio}»",Modifier.padding(17.dp),color=Paper.copy(.8f))}}
  if(a.isNotBlank())item{Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF211D28)),shape=RoundedCornerShape(20.dp)){Column(Modifier.padding(17.dp)){Text("پاسخ",color=Gold,fontWeight=FontWeight.Bold);Spacer(Modifier.height(6.dp));Text(a,color=Paper,fontSize=15.sp)}}}
  item{OutlinedTextField(value=q,onValueChange=onQ,modifier=Modifier.fillMaxWidth(),label={Text("سؤال خودت را بپرس...")},minLines=3,colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=Gold,focusedLabelColor=Gold))}
  item{Button(onClick=onAsk,enabled=q.isNotBlank()&&!asking,modifier=Modifier.fillMaxWidth().height(55.dp),shape=RoundedCornerShape(17.dp)){Text(if(asking)"در حال بازجویی..." else "پرسیدن سؤال",fontWeight=FontWeight.Black)}}
  item{LaunchedEffect(asking){if(asking)try{onAnswer(Api.interview(getId(c),case.id,s.id,q))}catch(e:Exception){onAnswer("پاسخ دریافت نشد: ${e.message}")}}}
 }
}}
@Composable fun LaunchedEffectOnce(c:Context,caseId:String,suspectId:String,done:(String)->Unit){LaunchedEffect(caseId,suspectId){try{val r=Api.accuse(getId(c),caseId,suspectId);done(if(r.first)"اتهام درست بود! پرونده حل شد 🎉 +250 XP" else "این اتهام درست نبود. هنوز سرنخ‌هایی داری که بررسی نکرده‌ای.")}catch(e:Exception){done("خطا: ${e.message}")}}}

object Api{
 private val client=OkHttpClient.Builder().connectTimeout(15,TimeUnit.SECONDS).readTimeout(30,TimeUnit.SECONDS).writeTimeout(30,TimeUnit.SECONDS).callTimeout(45,TimeUnit.SECONDS).build()
 private fun req(r:Request):String=client.newCall(r).execute().use{res->val b=res.body?.string().orEmpty();if(!res.isSuccessful)throw Exception("HTTP ${res.code}: ${b.take(180)}");b}
 suspend fun ensureUser(id:String)=withContext(Dispatchers.IO){val b=JSONObject().put("id",id).put("display_name","کارآگاه").toString().toRequestBody("application/json".toMediaType());req(Request.Builder().url("$API/api/users").post(b).build())}
 suspend fun user(id:String)=withContext(Dispatchers.IO){val o=JSONObject(req(Request.Builder().url("$API/api/users/$id").get().build())).getJSONObject("user");User(o.optString("display_name","کارآگاه"),o.optInt("level",1),o.optInt("xp",0),o.optInt("coins",0))}
 suspend fun cases(uid:String)=withContext(Dispatchers.IO){val a=JSONObject(req(Request.Builder().url("$API/api/cases?user_id=$uid").get().build())).getJSONArray("cases");buildList{for(i in 0 until a.length()){val o=a.getJSONObject(i);add(Case(o.getString("id"),o.getString("title"),o.optString("subtitle"),o.getString("description"),o.optString("location"),o.optString("cover_url"),o.optString("difficulty"),o.optString("progress_status","locked"),o.optInt("score")))}}}
 suspend fun caseDetail(id:String):Pair<List<Suspect>,List<Evidence>>=withContext(Dispatchers.IO){val o=JSONObject(req(Request.Builder().url("$API/api/cases/$id").get().build()));val ss=o.getJSONArray("suspects");val ee=o.getJSONArray("evidence");Pair(buildList{for(i in 0 until ss.length()){val x=ss.getJSONObject(i);add(Suspect(x.getString("id"),x.getString("name"),x.optString("role"),x.optString("bio"),x.optString("portrait_url"))) }},buildList{for(i in 0 until ee.length()){val x=ee.getJSONObject(i);add(Evidence(x.getString("id"),x.getString("title"),x.optString("description"),x.optString("image_url"),x.optString("type")))}})}
 suspend fun interview(uid:String,cid:String,sid:String,q:String)=withContext(Dispatchers.IO){val b=JSONObject().put("user_id",uid).put("question",q).toString().toRequestBody("application/json".toMediaType());JSONObject(req(Request.Builder().url("$API/api/cases/$cid/suspects/$sid/interview").post(b).build())).optString("answer","...")}
 suspend fun accuse(uid:String,cid:String,sid:String)=withContext(Dispatchers.IO){val b=JSONObject().put("user_id",uid).put("suspect_id",sid).toString().toRequestBody("application/json".toMediaType());val o=JSONObject(req(Request.Builder().url("$API/api/cases/$cid/accuse").post(b).build()));Pair(o.optBoolean("correct"),o.optString("message"))}
}
private fun getId(c:Context):String{val p=c.getSharedPreferences("detective",Context.MODE_PRIVATE);return p.getString("id",null)?:UUID.randomUUID().toString().also{p.edit().putString("id",it).apply()}}
