package components.newbox

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import model.Widget

class ViewModelBox:ViewModel () {
    private val _widgetList = mutableStateListOf<Widget>()
    val widgetList: List<Widget> get() = _widgetList

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
        _widgetList.add(widget)
    }

    // Opcional si quieres eliminar widgets visualmente
    fun removeWidget(widgetId: String) {
        _widgetList.removeAll { it.id == widgetId }
    }
}