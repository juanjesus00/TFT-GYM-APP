package firebase.auth

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import routes.NavigationActions
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firestore.admin.v1.Index
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import components.newbox.ViewModelBox
import geminiApi.GeminiApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import model.DiaRutina
import model.Registro
import model.RutinaFirebase
import model.User
import model.Widget
import org.json.JSONObject
import viewModel.api.GymViewModel
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.String
import kotlin.toString

class AuthRepository : ViewModel(){
    private val auth = FirebaseAuth.getInstance()
    private var _loading = MutableLiveData(false)
    val currentUser = FirebaseAuth.getInstance().currentUser

    private val _isEmailVerified = MutableLiveData<Boolean>()
    val isEmailVerified: LiveData<Boolean> = _isEmailVerified

    private val _maxRm = MutableStateFlow<Map<String, Float>>(emptyMap())
    val maxRm: StateFlow<Map<String, Float>> = _maxRm

    private val storageRef= FirebaseStorage.getInstance().reference
    private val dbRef = FirebaseFirestore.getInstance()

    fun signIn(
        email: String,
        password: String,
        context: Context,
        onSuccess: () -> Unit,
        navigationActions: NavigationActions,
        onErrorAction: () -> Unit
    ) = viewModelScope.launch {
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            Toast.makeText(
                context,
                "Todos los campos son obligatorios",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            try {
                //navigationActions.navigateToCarga()
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (FirebaseAuth.getInstance().currentUser?.isEmailVerified == false) {
                                sendVerificationRegisterEmail()
                            }
                            Log.d("Loginbackend", "Inicio de sesión exitoso")
                            onSuccess()

                        } else {
                            val errorMessage = task.exception?.localizedMessage ?: "Error desconocido"
                            Log.d("LoginBackend", "Error al iniciar sesión: $errorMessage")
                            onErrorAction()
                            Toast.makeText(
                                context,
                                "Inicio de sesión fallido: $errorMessage",
                                Toast.LENGTH_LONG
                            ).show()

                        }
                    }
                    .addOnFailureListener { exception ->
                        exception.printStackTrace()
                        onErrorAction()
                        Toast.makeText(
                            context,
                            "Inicio de se sesio erroneo la contraseña o el email son incorrectos",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } catch (e: Exception) {
                Log.e("LoginBackend", "Excepción capturada: ${e.message}")
                Toast.makeText(
                    context,
                    "Ha ocurrido un error inesperado: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun logOut(onSuccess: () -> Unit) {
        Firebase.auth.signOut()
        onSuccess.invoke()
    }

    private fun sendVerificationRegisterEmail() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("Correo de verificación enviado.")
            } else {
                println("Error al enviar el correo: ${task.exception?.message}")
            }
        }
    }
    fun checkIfEmailVerified(){
        val user = FirebaseAuth.getInstance().currentUser
        if (user?.isEmailVerified == true) {
            // El correo ha sido verificado
            _isEmailVerified.value = user.isEmailVerified
        } else {
            // El correo no ha sido verificado aún
            Log.d("Verification", "Email is not verified.")
            _isEmailVerified.value = false
        }
    }
    suspend fun verifyEmailWithAPI(email: String): Boolean = withContext(Dispatchers.IO) {
        val apiKey = "15a852747b6b249e078725c6985da185"
        try {
            val url = URL("https://apilayer.net/api/check?access_key=$apiKey&email=$email")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val response = inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()
                // Parseamos la respuesta JSON
                val jsonObject = JSONObject(response)
                val smtpCheck = jsonObject.optBoolean("smtp_check", false) // True si el correo es válido
                smtpCheck
            } else {
                connection.disconnect()
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    fun register(
        email: String,
        password: String,
        context: Context,
        name: String,
        onSuccess: () -> Unit,
        navigationActions: NavigationActions,
        repeatPassword: String
    ) = viewModelScope.launch {
        var isCorrectEmail = true//verifyEmailWithAPI(email)
        var passwordValid = isPasswordValid(password)
        //println(isCorrectEmail)
        if(true){
            if (_loading.value == false) { //no se esta creando usuarios actualmente
                if (password.length < 6) {
                    Toast.makeText(
                        context,
                        "La contraseña debe tener al menos 6 caracteres",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if(passwordValid){
                    if(!name.isEmpty()){
                        if(password != repeatPassword){
                            Toast.makeText(
                                context,
                                "Las contraseñas No coinciden",
                                Toast.LENGTH_LONG
                            ).show()
                        }else{
                            //navigationActions.navigateToCarga()
                            _loading.value = true

                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        sendVerificationRegisterEmail()
                                        createUser(name, email, "")
                                        onSuccess()
                                    } else {
                                        Log.d(
                                            "loginbackend",
                                            "La creacion de usuarios falló: ${task.result.toString()}"
                                        )
                                    }
                                    _loading.value = false
                                }
                        }
                    }else{
                        Toast.makeText(
                            context,
                            "El nombre de usuario no puede ser vacío",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }else{
                    Toast.makeText(
                        context,
                        "La contraseña debe tener al menos 1 letra 1 mayuscula 1 número y 1 caracter especial !%*?&.#",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
        }else{
            Toast.makeText(context, "El correo introducido No es Real", Toast.LENGTH_SHORT).show()
        }


    }
    private fun isPasswordValid(password: String): Boolean {
        // Expresión regular para la contraseña
        val passwordPattern = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@\$!%*?&.#])[A-Za-z\\d@\$!%*?&.#]{6,}\$")
        return passwordPattern.matches(password)
    }

    private fun createUser(
        displayName: String,
        email: String,
        profileImage: String
    ) {
        val userId = auth.currentUser?.uid
        val user = User(
            userId = userId.toString(),
            userName = displayName,
            profileImageUrl = profileImage,
            email = email,
            gender = "",
            birthDate = "",
            weight = 0,
            height = 0
        ).toMap()

        val registro = Registro(
            fecha = "",
            peso = 0f,
            repeticiones = 0,
            rm = 0f
        )

        FirebaseFirestore.getInstance().collection("Usuarios")
            .document(userId.toString())
            .set(user)
            .addOnSuccessListener {
                Log.d("loginbackend", "Creado ${it}")
                FirebaseFirestore.getInstance().collection("Usuarios")
                    .document(userId.toString()).collection("Ejercicios").document("Press de Banca").set(hashMapOf(
                        "registros" to listOf(null),
                        "max_rm" to 0
                    )).addOnSuccessListener {
                        Log.d("loginbackend", "Creado ${it}")
                    }.addOnFailureListener {
                        Log.d("loginbackend", "Error ${it}")
                    }
                FirebaseFirestore.getInstance().collection("Usuarios")
                    .document(userId.toString()).collection("Ejercicios").document("Peso Muerto").set(hashMapOf(
                        "registros" to listOf(null),
                        "max_rm" to 0
                    )).addOnSuccessListener {
                        Log.d("loginbackend", "Creado ${it}")
                    }.addOnFailureListener {
                        Log.d("loginbackend", "Error ${it}")
                    }
                FirebaseFirestore.getInstance().collection("Usuarios")
                    .document(userId.toString()).collection("Ejercicios").document("Sentadilla").set(hashMapOf(
                        "registros" to listOf(null),
                        "max_rm" to 0
                    )).addOnSuccessListener {
                        Log.d("loginbackend", "Creado ${it}")
                    }.addOnFailureListener {
                        Log.d("loginbackend", "Error ${it}")
                    }


            }.addOnFailureListener {
                Log.d("loginbackend", "Error ${it}")
            }

    }

    fun editUser(gender: String, birthDate: String, weight: Int, height: Int, onSuccess: () -> Unit)=viewModelScope.launch{
        currentUser?.let{ user ->
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()

            val userUpdates = mapOf(
                "gender" to gender,
                "birthDate" to birthDate,
                "weight" to weight,
                "height" to height
            )

            db.collection("Usuarios").document(uid).update(userUpdates)
                .addOnSuccessListener {
                    Log.d("Firestore", "$currentUser ha sido actualizado exitosamente")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error al actualizar a $currentUser", e)
                }
        }
    }

    fun editUser2(userInfoToUpdate: User, onSuccess: () -> Unit)=viewModelScope.launch{
        currentUser?.let{ user ->
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()

            val userUpdates = mapOf(
                "gender" to userInfoToUpdate.gender,
                "birthDate" to userInfoToUpdate.birthDate,
                "weight" to userInfoToUpdate.weight,
                "height" to userInfoToUpdate.height,
                "email" to userInfoToUpdate.email,
                "userName" to userInfoToUpdate.userName,
                "user_id" to userInfoToUpdate.userId
            )

            db.collection("Usuarios").document(uid).update(userUpdates)
                .addOnSuccessListener {
                    Log.d("Firestore", "$currentUser ha sido actualizado exitosamente")
                    onSuccess.invoke()
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error al actualizar a $currentUser", e)
                }
        }
    }


    fun editUserImageStorage(uriImage: Uri?, onSuccess: () -> Unit){
        currentUser?.let { user ->
            val uid = user.uid
            val imageStorage = storageRef.child("userImage/$uid.jpg")
            uriImage?.let {
                imageStorage.putFile(it)
                    .addOnSuccessListener { taskSnapshot ->
                        Log.d("Storage", "imagen de usuario Guardada en Storage correctamente")
                        imageStorage.downloadUrl.addOnSuccessListener { uri ->
                            val imageUri = uri.toString()

                            dbRef.collection("Usuarios").document(uid).update(mapOf("profileImageUrl" to imageUri))
                                .addOnSuccessListener {
                                    Log.d("Firestore", "usuario actualizado correctamente")
                                    onSuccess.invoke()
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Firestore", "Error al actualizar imagen de usuario", e)
                                }
                        }.addOnFailureListener { e ->
                            Log.w("Storage", "Error al obtener imagen de usuario del storage", e)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w("Storage", "Error al subir al Storage la imagen de usuario", e)
                    }
            }
        }
    }

    fun calculateUserMaxRm(rm: Float, exercise: String, callback: (Boolean) -> Unit){
        currentUser?.let { user ->
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()
            db.collection("Usuarios").document(uid).collection("Ejercicios").document(exercise).get()
                .addOnSuccessListener { document ->
                    if(document != null && document.exists()){
                        val currentRm = document.get("max_rm")
                        if((currentRm as? Number)?.toFloat() ?: 0f > rm){
                            callback(false)

                        }else{
                            callback(true)
                        }

                    }else{
                        callback(true)
                    }
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                    callback(false)
                }
        }
    }

    fun editUserFromVideo(exercise: String, weight: Float, repetitions: Int, date:String, rm: Float?, onSuccess: () -> Unit){
        currentUser?.let{ user ->
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()

            val registro = Registro(
                fecha = date,
                peso = weight,
                repeticiones = repetitions,
                rm = rm ?: 0f
            )

            calculateUserMaxRm(rm ?: 0f, exercise) { isNewMaxRm ->
                val updateData = hashMapOf<String, Any>(
                    "registros" to FieldValue.arrayUnion(registro)
                )

                if (isNewMaxRm) {
                    updateData["max_rm"] = rm ?: 0f
                }

                db.collection("Usuarios").document(uid)
                    .collection("Ejercicios").document(exercise)
                    .set(updateData, SetOptions.merge()) // merge para no borrar campos anteriores
                    .addOnSuccessListener {
                        Log.d("editUserFromVideo", "Datos de ejercicio actualizados correctamente")
                        onSuccess.invoke()
                    }
                    .addOnFailureListener { e ->
                        Log.w("editUserFromVideo", "Error al actualizar datos del ejercicio", e)
                    }
            }
        }
    }

    fun updateHistoryUser(
        history: List<Registro>,
        rm: Float,
        exercise: String,
        onSuccess: () -> Unit,
        context: Context
    ){
        val historySorted = sortHistoryFromFecha(history)

        currentUser?.let{ user ->
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()

            calculateUserMaxRm(rm ?: 0f, exercise) { isNewMaxRm ->
                val updateData = hashMapOf<String, Any>(
                    "registros" to historySorted
                )

                if (isNewMaxRm) {
                    updateData["max_rm"] = rm ?: 0f

                }else{
                    updateData["max_rm"] = getNewMaxRmFromHistory(history)

                }

                db.collection("Usuarios").document(uid)
                    .collection("Ejercicios").document(exercise)
                    .set(updateData, SetOptions.merge()) // merge para no borrar campos anteriores
                    .addOnSuccessListener {
                        Log.d("editUserFromVideo", "Datos de ejercicio actualizados correctamente")
                        onSuccess.invoke()
                    }
                    .addOnFailureListener { e ->
                        Log.w("editUserFromVideo", "Error al actualizar datos del ejercicio", e)
                    }
            }

        }
    }

    fun updateUserExerciseStats(exercise: String, rareza:String, nivel: String){
        currentUser?.let{ user ->
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()

            db.collection("Usuarios").document(uid)
                .collection("Ejercicios").document(exercise)
                .update(
                    mapOf(
                        "rareza" to rareza,
                        "nivel" to nivel
                    )
                )
                .addOnSuccessListener {
                    Log.d("Firestore", "Campos 'rareza' y 'nivel' añadidos correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error al añadir campos dinámicos", e)
                }
        }
    }

    fun deleteHistoryRegistro(
        history: List<Registro>,
        rm: Float,
        exercise: String,
        onSuccess: () -> Unit,
        context: Context
    ){
        val historySorted = sortHistoryFromFecha(history)

        currentUser?.let{ user ->
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()

            val updateData = hashMapOf<String, Any>(
                "registros" to historySorted
            )

            updateData["max_rm"] = rm ?: 0f


            db.collection("Usuarios").document(uid)
                .collection("Ejercicios").document(exercise)
                .set(updateData, SetOptions.merge()) // merge para no borrar campos anteriores
                .addOnSuccessListener {
                    Log.d("editUserFromVideo", "Datos de ejercicio actualizados correctamente")
                    onSuccess.invoke()
                }
                .addOnFailureListener { e ->
                    Log.w("editUserFromVideo", "Error al actualizar datos del ejercicio", e)
                }

        }
    }

    fun sortHistoryFromFecha(history: List<Registro>): List<Registro> {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return history.sortedBy { registro ->
            try {
                formatter.parse(registro.fecha)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getNewMaxRmFromHistory(history: List<Registro>): Float {
        return history.maxOfOrNull { it.rm } ?: 0f
    }

    fun getInfoUser(onResult: (Map<String, Any>?) -> Unit){
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()

            db.collection("Usuarios").document(uid).get()
                .addOnSuccessListener { document ->
                    if(document != null && document.exists()){
                        val user = User(
                            userId = document.getString("user_id").toString(),
                            email = document.getString("email").toString(),
                            userName = document.getString("userName").toString(),
                            profileImageUrl = document.getString("profileImageUrl").toString(),
                            birthDate = document.getString("birthDate").toString(),
                            gender = document.getString("gender").toString(),
                            height = (document.get("height") as Long).toInt(),
                            weight = (document.getLong("weight") as Long).toInt(),
                        ).toMap()
                        onResult(user)
                    }else{
                        onResult(null)
                    }
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                    onResult(null)
                }
        } ?: run{
            onResult(null)
        }
    }

    private var listenerRegistration: ListenerRegistration? = null

    fun setNewMaxRm(exercise: String, value: Float) {
        _maxRm.value = _maxRm.value.toMutableMap().apply {
            put(exercise, value)
        }
    }

    fun observeMaxRmChanges(
        context: Context,
        exercise: String,
        bodyWeight: Float,
        levelStrength: String,
        onResult: (String) -> Unit = {}
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val docRef = FirebaseFirestore.getInstance()
            .collection("Usuarios")
            .document(uid)
            .collection("Ejercicios")
            .document(exercise)

        listenerRegistration?.remove() // 🔁 Cancela el anterior si existe

        val geminiApiService = GeminiApiService(context)
        var lastRm: Float? = null

        listenerRegistration = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener

            val currentRm = snapshot?.getDouble("max_rm")?.toFloat()
            if (currentRm != null && currentRm != lastRm) {
                lastRm = currentRm

                CoroutineScope(Dispatchers.IO).launch {
                    val prompt = "Actúa como un estadístico deportivo con acceso a la base de datos de estándares de fuerza de powerlifting 2025 (StrengthLevel, USPA, IPF o datos equivalentes).\n" +
                            "\n" +
                            "• Se te facilita una tabla de percentiles donde cada celda indica el peso (en kg) que alcanza el percentil P para un 1RM en un ejercicio concreto.  \n" +
                            "• Tu tarea es calcular el % de levantadores que pueden igualar o superar un RM dado.  \n" +
                            "• El % se define como: (número de levantadores con RM ≥ dato) / (total de levantadores) × 100.  \n" +
                            "• Reporta solo una línea con el formato:  \n" +
                            "rareza: X%  \n" +
                            "donde X es el porcentaje redondeado a un decimal.\n" +
                            "\n" +
                            "Ejemplo de entrada:\n" +
                            "- ejercicio: SENTADILLA\n" +
                            "- peso corporal: 83kg\n" +
                            "- RM: 210kg\n" +
                            "\n" +
                            "Ejemplo de tabla (extracto simplificado—usa esta tabla **y nada más**):\n" +
                            "P99: 270kg  \n" +
                            "P95: 240kg  \n" +
                            "P90: 220kg  \n" +
                            "P80: 200kg  \n" +
                            "P70: 185kg  \n" +
                            "P50: 160kg  \n" +
                            "\n" +
                            "### PASOS QUE DEBES SEGUIR\n" +
                            "1. Localiza el intervalo de percentiles que encierra el RM (ej.: 210kg está entre P90 y P80).  \n" +
                            "2. Interpola linealmente si el RM no coincide exactamente con un percentil.  \n" +
                            "3. Calcula el porcentaje de levantadores con RM≥210kg = 100−percentilCorrespondiente.  \n" +
                            "4. Devuélvelo como «rareza: X%» sin texto extra.\n" +
                            "\n" +
                            "---  \n" +
                            "Datos reales a procesar:  \n" +
                            "• ejercicio: $exercise  \n" +
                            "• peso corporal: $bodyWeight kg  \n" +
                            "• RM: $currentRm kg  \n" +
                            "• Tabla de percentiles para $exercise y $bodyWeight kg (inclúyela aquí tal como la recuperes)."
                    val result = geminiApiService.sendPrompt(prompt)
                    result.onSuccess { textoLimpio ->
                        val updateMap = parsearRarezaNivel(textoLimpio, levelStrength)
                        docRef.update(updateMap)
                        onResult(textoLimpio)
                    }
                }
            }
        }
    }
    private fun parsearRarezaNivel(respuesta: String, levelStrength: String): Map<String, Any> {
        val regex = Regex("""rareza:\s*(\d+([.,]\d+)?)%?""", RegexOption.IGNORE_CASE)
        val match = regex.find(respuesta)

        return if (match != null) {
            val rareza = match.groupValues[1].toFloat()
            mapOf("rareza" to rareza, "nivel" to levelStrength)
        } else emptyMap()
    }
    fun getHistoryUser(onResult: (List<Registro>?) -> Unit, exercise: String){
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()

            db.collection("Usuarios").document(uid).collection("Ejercicios").document(exercise).get()
                .addOnSuccessListener { document ->
                    if(document != null && document.exists()){
                        val registrosRaw = document.get("registros") as? List<Map<String, Any>>
                        val registros = registrosRaw?.mapNotNull { item ->
                            try {
                                Registro(
                                    fecha = item["fecha"] as String,
                                    peso = (item["peso"] as Number).toFloat(),
                                    repeticiones = (item["repeticiones"] as Number).toInt(),
                                    rm = (item["rm"] as Number).toFloat()
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                                null
                            }
                        }
                        onResult(registros)
                    }else{
                        onResult(null)
                    }
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                    onResult(null)
                }
        } ?: run{
            onResult(null)
        }
    }

    fun getRMUser(onResult: (Map<String, Float>) -> Unit){
        var resultList = mutableMapOf<String, Float>()
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()
            val listExercise = listOf("Press de Banca", "Peso Muerto", "Sentadilla")
            var completedRequests = 0

            listExercise.forEach { exerciseName ->
                db.collection("Usuarios").document(uid)
                    .collection("Ejercicios").document(exerciseName).get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val value = document.get("max_rm")
                            resultList[exerciseName] = (value as? Number)?.toFloat() ?: 0f
                        } else {
                            resultList[exerciseName] = 0f
                        }
                        completedRequests++
                        if (completedRequests == listExercise.size) {
                            onResult(resultList)
                        }
                    }
                    .addOnFailureListener { exception ->
                        exception.printStackTrace()
                        completedRequests++
                        resultList[exerciseName] = 0f
                        if (completedRequests == listExercise.size) {
                            onResult(resultList)
                        }
                    }
            }
        } ?: run{
            onResult(resultList)
        }
    }


    fun getLevelRare(onResult: (Map<String, Map<String, Any>>) -> Unit){
        var resultList = mutableMapOf<String, Map<String, Any>>()
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()
            val listExercise = listOf("Press de Banca", "Peso Muerto", "Sentadilla")
            var completedRequests = 0

            listExercise.forEach { exerciseName ->
                db.collection("Usuarios").document(uid)
                    .collection("Ejercicios").document(exerciseName).get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val nivel = document.getString("nivel") ?: "Novato"
                            val rareza = (document.get("rareza") as? Number)?.toFloat() ?: 100f
                            resultList[exerciseName] = mapOf("nivel" to nivel, "rareza" to rareza)
                        } else {
                            resultList[exerciseName] = mapOf("nivel" to "", "rareza" to  0f)
                        }
                        completedRequests++
                        if (completedRequests == listExercise.size) {
                            onResult(resultList)
                        }
                    }
                    .addOnFailureListener { exception ->
                        exception.printStackTrace()
                        completedRequests++
                        resultList[exerciseName] = mapOf("nivel" to "", "rareza" to  0f)
                        if (completedRequests == listExercise.size) {
                            onResult(resultList)
                        }
                    }
            }
        } ?: run{
            onResult(resultList)
        }
    }
    fun addNewWidget(
        widgetType: String,
        exercise: String,
        onSuccess: () -> Unit,
        viewModelBox: ViewModelBox
    ){
        val currentUser = FirebaseAuth.getInstance().currentUser
        val widgetData = mutableMapOf(
            "type" to widgetType,
            "exercise" to exercise
        )
        currentUser?.let { user ->
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()

            db.collection("Usuarios").document(uid)
                .collection("dashboardWidgets")
                .document("$widgetType-$exercise").set(widgetData)
                .addOnSuccessListener {
                    Log.d("Firebase", "Widget añadido con ID: $it")
                    val widget = Widget(
                        id = "$widgetType-$exercise",
                        type = widgetType,
                        exercise = exercise
                    )
                    viewModelBox.addWidget(widget)
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Error al añadir widget", e)
                }


        }
    }

    fun deleteWidgetFromFirebase(widgetId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val uid = currentUser.uid

        FirebaseFirestore.getInstance()
            .collection("Usuarios")
            .document(uid)
            .collection("dashboardWidgets")
            .document(widgetId)
            .delete()
            .addOnSuccessListener {
                Log.d("Firebase", "Widget $widgetId eliminado con éxito")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error al eliminar el widget", e)
            }
    }

    suspend fun saveRoutine(
        tipo: String,
        ejercicio: String?,
        contenido: String
    ) {
        currentUser?.uid?.let { uid ->
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("Usuarios").document(uid)
            val rutina = parsearRutinaTextoAJson(contenido)
            when (tipo.lowercase()) {
                "hipertrofia", "hypertrophy" -> saveHypertrophyRoutine(userRef, rutina)
                "fuerza", "strength" -> saveStrengthRoutine(userRef, ejercicio, rutina)
            }
        }
    }

    suspend fun saveManualRoutine(ejercicio: String, tipo: String, contenido: List<Map<String, Any>>, index: Int? = null){
        currentUser?.uid?.let { uid ->
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("Usuarios").document(uid)
            when (tipo.lowercase()) {
                "hipertrofia", "hypertrophy" -> saveHypertrophyRoutine(userRef, contenido, index)
                "fuerza", "strength" -> saveStrengthRoutine(userRef, ejercicio, contenido)
            }
        }
    }

    fun marcarDiaComoHecho(rutinaType: String, rutinaIndex:Int, diaIndex: Int, hecho: Boolean) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val rutinaRef = db.collection("Usuarios").document(uid).collection("Rutinas").document(rutinaType)
        viewModelScope.launch {
            rutinaRef.get().addOnSuccessListener { doc ->
                val rutinasRaw = doc["rutinas"] as? List<Map<String, Any>> ?: emptyList()
                val gson = Gson()

                val rutinas = rutinasRaw.mapNotNull { item ->
                    try {
                        val activa = item["activa"] as? Boolean ?: false
                        val fecha = item["fecha"] as? String ?: ""
                        val contenidoJson = gson.toJson(item["contenido"])
                        val contenido = gson.fromJson(
                            contenidoJson,
                            object : TypeToken<List<DiaRutina>>() {}.type
                        ) as List<DiaRutina>

                        RutinaFirebase(activa, fecha, contenido)
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error al parsear rutina: ${e.message}")
                        null
                    }
                }.toMutableList()

                if (rutinaIndex !in rutinas.indices || diaIndex !in rutinas[rutinaIndex].contenido.indices) {
                    Log.e("Firestore", "Índice fuera de rango")
                    return@addOnSuccessListener
                }

                // Actualizar el campo "hecho"
                val rutina = rutinas[rutinaIndex]
                val contenidoActualizado = rutina.contenido.toMutableList()
                contenidoActualizado[diaIndex] = contenidoActualizado[diaIndex].copy(hecho = hecho)

                val todosHechos = contenidoActualizado.all { it.hecho }

                if (todosHechos) {
                    for (i in contenidoActualizado.indices) {
                        contenidoActualizado[i] = contenidoActualizado[i].copy(hecho = false)
                    }
                }
                rutinas[rutinaIndex] = rutina.copy(contenido = contenidoActualizado)

                // Convertir de nuevo a List<Map<String, Any>>
                val rutinasActualizadas = rutinas.map { rutina ->
                    mapOf(
                        "activa" to rutina.activa,
                        "fecha" to rutina.fecha,
                        "contenido" to rutina.contenido.map { dia ->
                            mapOf(
                                "dia" to dia.dia,
                                "ejercicios" to dia.ejercicios,
                                "hecho" to dia.hecho
                            )
                        }
                    )
                }

                rutinaRef.update("rutinas", rutinasActualizadas)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Día actualizado correctamente")
                    }
                    .addOnFailureListener {
                        Log.e("Firestore", "Error al actualizar día", it)
                    }

            }.addOnFailureListener {
                Log.e("Firestore", "Error al leer rutina", it)
            }
        }
    }

    private suspend fun saveHypertrophyRoutine(
        userRef: DocumentReference,
        contenido: List<Map<String, Any>>,
        editIndex: Int? = null
    ) {
        val hypertrophyRef = userRef.collection("Rutinas").document("Hipertrofia")
        val docSnapshot = hypertrophyRef.get().await()
        val existingRoutines = docSnapshot.get("rutinas") as? List<Map<String, Any>> ?: emptyList()

        val updatedRoutines = existingRoutines.map { rutina ->
            val mutableRutina = rutina.toMutableMap()
            mutableRutina["activa"] = false

            // Reseteamos el campo "hecho" de cada día si lo tiene
            val contenidoReseteado = (mutableRutina["contenido"] as? List<Map<String, Any>>)?.map { dia ->
                dia.toMutableMap().apply {
                    this["hecho"] = false
                }
            } ?: emptyList()

            mutableRutina["contenido"] = contenidoReseteado
            mutableRutina
        }.toMutableList()

        val fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

        // Nos aseguramos de que todos los días del contenido nuevo tengan el campo "hecho"
        val contenidoConHecho = contenido.map { dia ->
            dia.toMutableMap().apply {
                if (!this.containsKey("hecho")) {
                    this["hecho"] = false
                }
            }
        }

        val newRoutine = mapOf(
            "contenido" to contenidoConHecho,
            "fecha" to fechaActual,
            "activa" to true
        )

        if (editIndex != null && editIndex < updatedRoutines.size) {
            updatedRoutines[editIndex] = newRoutine as MutableMap<String, Any>
        } else {
            updatedRoutines.add(newRoutine as MutableMap<String, Any>)
        }

        hypertrophyRef.set(mapOf("rutinas" to updatedRoutines), SetOptions.merge()).await()
    }


    private suspend fun saveStrengthRoutine(
        userRef: DocumentReference,
        ejercicio: String?,
        contenido: List<Map<String, Any>>
    ) {
        if (ejercicio == null) return

        val strengthRef = userRef.collection("Rutinas").document("Fuerza")

        val exerciseKey = when (ejercicio.lowercase()) {
            "press de banca", "bench press" -> "Press de Banca"
            "peso muerto", "deadlift" -> "Peso Muerto"
            "sentadilla", "squad" -> "Sentadilla"
            else -> return
        }

        val docSnapshot = strengthRef.get().await()
        val existingRoutines = docSnapshot.get(exerciseKey) as? List<Map<String, Any>> ?: emptyList()

        // Marcar todas las rutinas como inactivas y resetear el campo "hecho" de cada día
        val updatedRoutines = existingRoutines.map { rutina ->
            val mutableRutina = rutina.toMutableMap()
            mutableRutina["activa"] = false

            val contenidoReseteado = (mutableRutina["contenido"] as? List<Map<String, Any>>)?.map { dia ->
                dia.toMutableMap().apply {
                    this["hecho"] = false
                }
            } ?: emptyList()

            mutableRutina["contenido"] = contenidoReseteado
            mutableRutina
        }.toMutableList()

        val fechaActual = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

        // Nos aseguramos de que todos los días del contenido nuevo tengan el campo "hecho"
        val contenidoConHecho = contenido.map { dia ->
            dia.toMutableMap().apply {
                if (!this.containsKey("hecho")) {
                    this["hecho"] = false
                }
            }
        }

        val newRoutine = mapOf(
            "contenido" to contenidoConHecho,
            "fecha" to fechaActual,
            "activa" to true
        )

        // Añadir la nueva rutina activa al final de la lista
        updatedRoutines.add(newRoutine as MutableMap<String, Any>)

        strengthRef.set(mapOf(exerciseKey to updatedRoutines), SetOptions.merge()).await()
    }


    fun getRutinasHipertrofia(
        onResult: (List<RutinaFirebase>) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("Usuarios")
            .document(userId)
            .collection("Rutinas")
            .document("Hipertrofia")
            .get()
            .addOnSuccessListener { doc ->
                val rutinasRaw = doc["rutinas"] as? List<Map<String, Any>> ?: emptyList()
                val gson = Gson()

                val rutinas = rutinasRaw.mapNotNull { item ->
                    try {
                        val activa = item["activa"] as? Boolean ?: false
                        val fecha = item["fecha"] as? String ?: ""
                        val contenidoJson = gson.toJson(item["contenido"])
                        val contenido = gson.fromJson(
                            contenidoJson,
                            object : TypeToken<List<DiaRutina>>() {}.type
                        ) as List<DiaRutina>

                        RutinaFirebase(activa, fecha, contenido)
                    } catch (e: Exception) {
                        Log.e("Firebase", "Error al parsear rutina: ${e.message}")
                        null
                    }
                }

                onResult(rutinas)
            }
            .addOnFailureListener {
                Log.e("Firebase", "Error al obtener rutinas de hipertrofia: ${it.message}")
                onResult(emptyList())
            }
    }

    fun getRutinasFuerza(
        onResult: (List<RutinaFirebase>) -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("Usuarios")
            .document(userId)
            .collection("Rutinas")
            .document("Fuerza")
            .get()
            .addOnSuccessListener { doc ->
                val rutinasRaw = doc["rutinas"] as? List<Map<String, Any>> ?: emptyList()
                val gson = Gson()

                val rutinas = rutinasRaw.mapNotNull { item ->
                    try {
                        val activa = item["activa"] as? Boolean ?: false
                        val fecha = item["fecha"] as? String ?: ""
                        val contenidoJson = gson.toJson(item["contenido"])
                        val contenido = gson.fromJson(
                            contenidoJson,
                            object : TypeToken<List<DiaRutina>>() {}.type
                        ) as List<DiaRutina>

                        RutinaFirebase(activa, fecha, contenido)
                    } catch (e: Exception) {
                        Log.e("Firebase", "Error al parsear rutina: ${e.message}")
                        null
                    }
                }

                onResult(rutinas)
            }
            .addOnFailureListener {
                Log.e("Firebase", "Error al obtener rutinas de hipertrofia: ${it.message}")
                onResult(emptyList())
            }
    }

    fun deleteHypertrophyRoutine(routineList: MutableList<RutinaFirebase>, onSuccess: () -> Unit){
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val uid = currentUser.uid

        FirebaseFirestore.getInstance()
            .collection("Usuarios")
            .document(uid)
            .collection("Rutinas")
            .document("Hipertrofia")
            .set(mapOf("rutinas" to routineList))
            .addOnSuccessListener {
                Log.d("deleteHypertrophyRoutine", "Hypertrophy routine delete successfully")
                onSuccess.invoke()
            }
            .addOnFailureListener { e ->
                Log.w("deleteHypertrophyRoutine", "Error trying to delete hypertrophy routine", e)
                onSuccess.invoke()
            }
    }

    fun activateHypertrophyRoutine(routineList: MutableList<RutinaFirebase>, onSuccess: () -> Unit){
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val uid = currentUser.uid

        FirebaseFirestore.getInstance()
            .collection("Usuarios")
            .document(uid)
            .collection("Rutinas")
            .document("Hipertrofia")
            .set(mapOf("rutinas" to routineList))
            .addOnSuccessListener {
                Log.d("activateHypertrophyRoutine", "Hypertrophy routine activate successfully")
                onSuccess.invoke()
            }
            .addOnFailureListener { e ->
                Log.w("activateHypertrophyRoutine", "Error trying to activate hypertrophy routine", e)
                onSuccess.invoke()
            }
    }

    fun deleteUser(
        context: Context,
        onSuccess: () -> Unit
    ):Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val exercises = listOf("Press de Banca", "Peso Muerto", "Sentadilla")
        if (currentUser != null) {
            return try {
                // Eliminar los datos asociados al usuario de Firestore
                val db = FirebaseFirestore.getInstance()
                val userId = currentUser.uid
                exercises.forEach { exercise ->
                    db.collection("Usuarios").document(userId).collection("Ejercicios").document(exercise).delete()
                        .addOnSuccessListener {
                            Log.d("delete User", "$exercise is now deleted")
                        }
                        .addOnFailureListener { e ->
                            Log.w("delete User", "$e trying to delete $exercise")
                        }
                }

                // Eliminar datos de Firestore
                db.collection("Usuarios").document(userId).delete()
                    .addOnSuccessListener {
                        // Luego de eliminar los datos, eliminamos el usuario
                        logOut(onSuccess={})
                        currentUser.delete()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Usuario eliminado exitosamente
                                    Toast.makeText(
                                        context,
                                        "El usuario se ha eliminado con éxito",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onSuccess.invoke()
                                } else {
                                    // Manejar error al eliminar el usuario
                                    task.exception?.let {
                                        Toast.makeText(
                                            context,
                                            "El usuario no se ha podido eliminar",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        // Error al eliminar los datos de Firestore
                        Toast.makeText(
                            context,
                            "Error al eliminar los datos del usuario: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                        false
                    }
                true // Si todo salió bien
            } catch (e: Exception) {
                // Manejo de excepciones
                Toast.makeText(
                    context,
                    "Ocurrió un error: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
                false
            }
        } else {
            // Si no hay un usuario autenticado
            Toast.makeText(
                context,
                "No hay un usuario autenticado",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

    }


    fun parsearRutinaTextoAJson(rutinaTexto: String): List<Map<String, Any>> {
        val dias = mutableListOf<Map<String, Any>>()

        val bloques = rutinaTexto.split("##")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        for (bloque in bloques) {
            val lineas = bloque.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            if (lineas.isEmpty()) continue

            val nombreDia = lineas.first()

            val ejercicios = mutableListOf<Map<String, Any>>()
            for (linea in lineas.drop(3)) { // Saltamos título, encabezado, separador
                if (!linea.contains("|")) continue

                val columnas = linea.split("|")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }

                if (columnas.size >= 3) {
                    val nombre = columnas[0]
                    val series = columnas[1].toIntOrNull() ?: 0
                    val reps = columnas[2]

                    ejercicios.add(
                        mapOf(
                            "nombre" to nombre,
                            "series" to series,
                            "reps" to reps
                        )
                    )
                }
            }

            dias.add(
                mapOf(
                    "dia" to nombreDia,
                    "ejercicios" to ejercicios
                )
            )
        }

        return dias
    }
}