package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER, AI, SYSTEM
}

class GeminiChatService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val systemInstructionText = """
        أنت مستشار ذكاء اصطناعي خبير ومتخصص في استرجاع البيانات والطب الشرعي الرقمي لأجهزة الأندرويد (Android Data Recovery & Digital Forensics Expert).
        مهمتك:
        1. تقديم مساعدة تفصيلية ودقيقة لمستخدمي التطبيق حول استرجاع الصور، الفيديوهات، الملفات، ومحادثات واتساب.
        2. الإجابة بشفافية وعلمية عن حالات ما بعد الفورمات (إعادة ضبط المصنع): اشرح تقنية تشفير FBE (File-Based Encryption)، وكيف يمكن استرجاع الصور من سلة محذوفات Google Photos، النسخ الاحتياطي في Google Drive، مجلدات الكاش المصغرة (Thumbnails)، واسترجاع كروت الذاكرة الخارجية SD Cards، واستخراج قواعد بيانات WhatsApp crypt14/crypt15.
        3. إعطاء خطوات وحلول عملية بدقة واحترافية وبأسلوب عربي واضح وودود ومشجع.
    """.trimIndent()

    suspend fun sendMessage(
        history: List<ChatMessage>,
        userMessage: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getExpertOfflineResponse(userMessage)
        }

        try {
            val rootJson = JSONObject()

            // System instruction
            val systemInstruction = JSONObject()
            val systemParts = JSONArray()
            systemParts.put(JSONObject().put("text", systemInstructionText))
            systemInstruction.put("parts", systemParts)
            rootJson.put("systemInstruction", systemInstruction)

            // Conversation history + new message
            val contentsArray = JSONArray()
            for (msg in history.takeLast(10)) {
                if (msg.sender == MessageSender.SYSTEM) continue
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                val item = JSONObject()
                item.put("role", role)
                val parts = JSONArray()
                parts.put(JSONObject().put("text", msg.text))
                item.put("parts", parts)
                contentsArray.put(item)
            }

            // Current prompt
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", userMessage))
            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            rootJson.put("contents", contentsArray)

            // Generation config
            val config = JSONObject()
            config.put("temperature", 0.7)
            config.put("topP", 0.95)
            rootJson.put("generationConfig", config)

            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val body = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string()

            if (response.isSuccessful && !responseString.isNullOrBlank()) {
                val respJson = JSONObject(responseString)
                val candidates = respJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "لم يتم توليد رد.")
                    }
                }
            }
            return@withContext getExpertOfflineResponse(userMessage)
        } catch (e: Exception) {
            return@withContext getExpertOfflineResponse(userMessage)
        }
    }

    private fun getExpertOfflineResponse(query: String): String {
        val q = query.lowercase()
        return when {
            q.contains("خزنة") || q.contains("خزنه") || q.contains("vault") || q.contains("مخفي") || q.contains("secret") || q.contains("قفل") -> {
                """
                🔐 **استرجاع وسائط الخزنة والمجلدات المخفية (حتى بدون نسخ احتياطي):**

                1️⃣ **كيف تعمل الخزنة وتطبيقات القفل؟**
                أغلب تطبيقات الخزنة (مثل Calculator Vault, KeepSafe, Gallery Vault, ومجلدات المجلد الآمن) تقوم بتغيير امتداد الملفات (مثلاً من `.jpg` إلى `.bin` أو `.dat` أو `.enc`) أو إخفائها بملف `.nomedia` دون تشفير عسكري معقد.

                2️⃣ **تقنية الاسترجاع الذاتي في التطبيق:**
                - يقوم تطبيقنا بفحص التواقيع الثنائية (Magic Bytes / Header Carving). بمجرد قراءة أول 16 بايت من الملف، يكتشف فوراً إن كان صورة JPEG/PNG أو فيديو MP4 أو مقطع موسيقى MP3 مهما كان اسمه أو امتداده!
                - يتم استخراج الوسائط حتى لو لم يتم عمل أي نسخ احتياطي لها من قبل.

                3️⃣ **مكان الحفظ بعد الاسترجاع:**
                يمكنك اختيار حفظ الملفات المسترجعة إما في **معرض الهاتف العام** أو إعادة حفظها في **خزنة الجهاز الآمنة (Device_Secure_Vault)** لحماية خصوصيتك التامة.
                """.trimIndent()
            }
            q.contains("موسيقى") || q.contains("اغاني") || q.contains("أغاني") || q.contains("صوت") || q.contains("music") || q.contains("audio") -> {
                """
                🎵 **استرجاع ملفات الموسيقى والصوتيات المحذوفة:**

                1. يدعم التطبيق فحص واسترجاع جميع صيغ الموسيقى: MP3, WAV, M4A, FLAC, AAC, OGG, وتسجيلات الملاحظات الصوتية.
                2. حتى لو حُذفت المقاطع ولم يكن لها نسخة احتياطية، يقوم محرك النبش العميق (Deep Carving) بفحص فهارس MediaStore ومجلدات الكاش والتنزيلات.
                3. عند الضغط على "استعادة"، تُحفظ المقاطع في مجلد `Music/Restored_Music` وتظهر فوراً في مشغل الموسيقى وتطبيقات الوسائط.
                """.trimIndent()
            }
            q.contains("بدون نسخ") || q.contains("نسخ احتياطي") || q.contains("backup") -> {
                """
                ⚡ **استرجاع الصور والفيديوهات التي لم يتم عمل نسخ احتياطي لها:**

                1️⃣ **تقنية النبش المباشر (Raw Sector Carving):**
                حتى لو لم تُرفع ملفاتك إلى Google Photos أو السحابة، فإن نظام أندرويد لا يحذف بايتات الملف فوراً، بل يُلغي فهرستها فقط.
                
                2️⃣ **استخراج المصغرات العالية الدقة (Thumbnails):**
                يقوم التطبيق بفحص مجلدات `.thumbnails` وذاكرة التخزين المؤقت للصور ومجلدات الكاميرا والـ DCIM، حيث يحتفظ النظام بنسخ مصغرة كاملة الجودة للصور والفيديوهات حتى بعد حذف الأصل.

                3️⃣ **خطوات الاسترجاع:**
                - فعّل خيار **"فحص دقيق وعميق"** من الشاشة الرئيسية.
                - اختر فئة **"الصور"** أو **"الفيديوهات"** أو **"الخزنة"**.
                - سيتم جلب الملفات غير المنسوخة مع وضع علامة "⚡ بدون نسخ احتياطي" عليها لتسترجعها بنقرة واحدة!
                """.trimIndent()
            }
            q.contains("ضبط المصنع") || q.contains("فورمات") || q.contains("format") || q.contains("reset") -> {
                """
                📌 **استرجاع البيانات بعد إعادة ضبط المصنع (الفورمات):**

                1️⃣ **الواقع التقني والتشفير:**
                في أنظمة أندرويد الحديثة (Android 7 فما فوق)، تُشفر الذاكرة الداخلية عبر تقنية FBE (التشفير المعتمد على الملفات). عند عمل فورمات، يقوم النظام بإتلاف مفاتيح التشفير الأساسية من رقاقة الأمان (Titan/TEE).

                2️⃣ **طرق الاسترجاع الحقيقية والفعالة 100%:**
                - **سلة محذوفات Google Photos:** تظل الصور المحذوفة في السلة لمدة تصل إلى 60 يوماً وتُسترجع فور تسجيل الدخول بحسابك في Google.
                - **بطاقة الذاكرة الخارجية (MicroSD):** إذا كانت ملفاتك مخزنة على كارت ميموري، فإن ضبط المصنع للجهاز لا يؤثر عليها ويمكنك فحصها واسترجاع كامل صورك وفيديوهاتك عبر خاصية "فحص الذاكرة الخارجية" في التطبيق.
                - **النسخ الاحتياطي السحابي:** افتح إعدادات الهاتف > Google > إدارة النسخ الاحتياطي لاستعادة سجل المكالمات وجهات الاتصال وبيانات التطبيقات.
                - **مخلفات الكاش والمصغرات (Thumbnails):** يحتوي نظام أندرويد على نسخ مصغرة عالية الجودة تظل قابلة للاستخراج عبر الفحص العميق بالتطبيق.
                """.trimIndent()
            }
            q.contains("واتساب") || q.contains("whatsapp") || q.contains("محادثات") -> {
                """
                💬 **طريقة استرجاع محادثات وصور وفيديوهات الواتساب:**

                1. **استرجاع الوسائط عبر الفحص العميق:**
                يقوم تطبيقنا بفحص مجلدات `Android/media/com.whatsapp/WhatsApp/Media` لاستخراج الصور ومقاطع الصوت المتبقية حتى لو حُذفت من التطبيق.

                2. **استعادة النسخة الاحتياطية لقواعد البيانات:**
                - يمتلك واتساب نسخاً احتياطية محلية بصيغة `msgstore.db.crypt14/15` داخل مجلد Databases.
                - عند إعادة تثبيت واتساب والتحقق برقم الهاتف، سيطلب منك فوراً استعادة النسخة من Google Drive أو التخزين الداخلي.
                """.trimIndent()
            }
            q.contains("صورة") || q.contains("صور") || q.contains("photo") || q.contains("video") || q.contains("فيديو") -> {
                """
                🖼️ **خطوات استرجاع الصور والفيديوهات المحذوفة:**

                1. اضغط على خيار **"الصور"** أو **"الفيديو"** من الشاشة الرئيسية.
                2. اختر **"الفحص العميق"** لمسح قطاعات التخزين ومجلدات الكاش والمصغرات `.thumbnails`.
                3. بعد انتهاء الفحص، ستظهر لك الملفات مصنفة مع نسبة دقة الاسترجاع.
                4. حدد الملفات التي تريدها واضغط على **"استعادة المحدد"** لحفظها مباشرة في استوديو الهاتف (معرض الصور).
                """.trimIndent()
            }
            else -> {
                """
                مرحباً بك! أنا مستشارك الذكي لاسترجاع البيانات في أندرويد.
                
                يمكنني مساعدتك في:
                - استعادة الصور والفيديوهات والملفات بدقة عالية بدون روت.
                - توجيهك لاسترجاع البيانات بعد الفورمات وضبط المصنع.
                - استرداد صور ومقاطع ومحادثات واتساب.
                - حل مشاكل عدم ظهور الملفات المسترجعة في المعرض.

                كيف يمكنني مساعدتك اليوم؟
                """.trimIndent()
            }
        }
    }
}
