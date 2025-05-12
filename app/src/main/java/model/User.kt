package model

data class User(
    val email: String,
    val userId: String,
    val userName: String,
    val profileImageUrl: String,
    val password: String
){
    fun toMap(): MutableMap<String, Any>{
        return mutableMapOf(
            "user_id" to this.userId,
            "userName" to this.userName,
            "PerfilImage" to this.profileImageUrl,
            "email" to this.email,
            "password" to this.password

        )
    }
}