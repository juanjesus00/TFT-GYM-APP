package model


data class User(
    val email: String,
    val userId: String,
    val userName: String,
    val profileImageUrl: String,
    val password: String,
    val gender: String,
    val birthDate: String,
    val weight: Int,
    val height: Int
){
    fun toMap(): MutableMap<String, Any>{
        return mutableMapOf(
            "user_id" to this.userId,
            "userName" to this.userName,
            "profileImageUrl" to this.profileImageUrl,
            "email" to this.email,
            "password" to this.password,
            "gender" to this.gender,
            "birthDate" to this.birthDate,
            "weight" to this.weight,
            "height" to this.height

        )
    }
}