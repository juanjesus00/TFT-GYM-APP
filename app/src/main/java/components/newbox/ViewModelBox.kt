package components.newbox

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

class ViewModelBox:ViewModel () {
    public var boxCount by mutableIntStateOf(0)

    fun addBox() {
        boxCount++
    }

    fun removeBox() {
        if (boxCount > 0) boxCount--
    }
}