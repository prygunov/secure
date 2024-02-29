
import java.security.MessageDigest

enum class HashAlgorithm(val bits: Int) {
    MD5(128),
    SHA1(160),
    SHA256(256),
    SHA512(512);

    fun calculateHash(arr: ByteArray): ByteArray{
        val digest = MessageDigest.getInstance(this.name)
        return digest.digest(arr)
    }
}