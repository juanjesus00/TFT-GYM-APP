package components.userProfileComponents
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import components.box.GetUserPersonalRecordBox

@Composable
fun GetUserPersonalRecords(){
    Column (

    ){
        Text(text = "Record Personales")
        for (i in 0..2){
            GetUserPersonalRecordBox()
        }
    }
}