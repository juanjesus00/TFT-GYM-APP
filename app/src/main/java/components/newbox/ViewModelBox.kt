package components.newbox

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import model.Widget

class ViewModelBox:ViewModel () {
    private val _widgetList = mutableStateListOf<Widget>()
    val widgetList: List<Widget> get() = _widgetList

    private val _rango = mutableStateOf<Int?>(null)
    val rango: State<Int?> get() = _rango

    fun loadWidgets() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("Usuarios").document(currentUser.uid)
            .collection("dashboardWidgets")
            .get()
            .addOnSuccessListener { result ->
                _widgetList.clear() // por si recargas
                for (document in result) {
                    val widget = Widget(
                        id = document.id,
                        type = document.getString("type") ?: "",
                        exercise = document.getString("exercise") ?: ""
                    )
                    _widgetList.add(widget)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error obteniendo widgets", exception)
            }
    }

    fun addWidget(widget: Widget) {
        if (_widgetList.none {it.id == widget.id}){
            _widgetList.add(widget)
        }

    }

    // Opcional si quieres eliminar widgets visualmente
    fun removeWidget(widgetId: String) {
        _widgetList.removeAll { it.id == widgetId }
    }

    fun calcularRango(ejercicio: String, peso: Float?) {
        val nuevoRango = when (ejercicio.lowercase()) {
            "press de banca", "bench press" -> {
                when (peso?.toInt()) {
                    in 90..99 -> 1
                    in 100..109 -> 2
                    in 110..119 -> 3
                    in 120..129 -> 4
                    in 130..139 -> 5
                    in 140..149 -> 6
                    in 150..159 -> 7
                    in 160..169 -> 8
                    in 170..179 -> 9
                    in 180..189 -> 10
                    in 190..Int.MAX_VALUE -> 11
                    else -> null
                }
            }
            "Peso Muerto", "dead lift" -> {
                when (peso?.toInt()) {
                    in 120..159 -> 1
                    in 160..179 -> 2
                    in 180..199 -> 3
                    in 200..219 -> 4
                    in 220..239 -> 5
                    in 240..259 -> 6
                    in 260..279 -> 7
                    in 280..299 -> 8
                    in 300..319 -> 9
                    in 320..339 -> 10
                    in 340..Int.MAX_VALUE -> 11
                    else -> null
                }
            }
            "Sentadilla", "squad" -> {
                when (peso?.toInt()) {
                    in 120..139 -> 1
                    in 140..159 -> 2
                    in 160..179 -> 3
                    in 180..199 -> 4
                    in 200..219 -> 5
                    in 220..239 -> 6
                    in 240..259 -> 7
                    in 260..279 -> 8
                    in 280..299 -> 9
                    in 300..319 -> 10
                    in 320..Int.MAX_VALUE -> 11
                    else -> null
                }
            }
            // Puedes agregar otros ejercicios con otros rangos
            else -> null
        }

        _rango.value = nuevoRango
    }
}