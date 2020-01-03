package hong.common.auth

interface Principal {
    val username: String
    val role: String
}