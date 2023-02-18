package com.example.database

import com.example.database.models.*
import org.bson.types.ObjectId


interface ChatService {
    suspend fun upsertGroup(group: Group): Boolean

    suspend fun fetchAllGroups(): List<Group>
    suspend fun register(user: User): Boolean

    suspend fun groupExists(groupId: String): Boolean
    suspend fun userExist(username: String): Boolean

    suspend fun getGroupById(groupId: String): Group?
    suspend fun getUserByName(userName: String): User?
    suspend fun getUserGroups(user: String): List<Group>

    suspend fun getActiveUsers(): Set<ActiveUser>
    suspend fun getUserEncryptedGroupKeys(username: String): List<GroupAccept>
    suspend fun upsertUserEncryptedGKey(key: GroupAccept): Boolean

    suspend fun getIdForGroup(): Int

    suspend fun fetchMessagesForGroup(groupId: String): List<Message>

    suspend fun addUserToActive(activeUser: ActiveUser)


    suspend fun userIsOnline(username: String): Boolean

    suspend fun getActiveUserByName(username: String): ActiveUser?
    suspend fun removeUserFromActive(username: String)
    suspend fun getUserById(id: ObjectId): User?
}


/*
 to create and manage the group there must be an admin, process listed below:
 1. admin must create a random symmetric key
 2. admin must send this key to each recipient of the group via asymmetric encryption
 i.e...the admin will receive the user's public key and encrypt the symmetric key and
 then send it to the user
 3. all further messages will then be encrypted and decrypted using this key
 4. if a new party joins the group then send to
 5. if party leaves then regenerate step 1
 */
//fun main() {
//    //client()
//    val s = Json.encodeToJsonElement(JoinGroupRequest(User("Ab", "123"), "122222"))
//    println(s.json)
//}

//fun shortUUID(length: Int = 8): String {
//    val allChars = UUID.randomUUID().toString().replace("-", "")
//    val random = Random()
//    val otp = CharArray(length)
//    for (i in 0 until length) {
//        otp[i] = allChars[random.nextInt(allChars.length)]
//    }
//    return String(otp)
//}

//fun client() {
//    //two numbers are relatively prime if gcd(d,x) = 1,
//    // so the private key must be this and must not be a prime factor of x
//    // formula for encryption = PLAINTEXT^PUBLIC KEY MOD PRODUCT
//    val p = sieveOfEratosthenes(30).max()
//    val q = sieveOfEratosthenes(p * 2).max()
//    val n = p * q
//    val x = (p - 1).times((q - 1))
//    val publicKey = (0 until x).first { gcd(it, x) == 1 && it.isNotFactor(x) }
//    val privateKey = (0 until x).first { BigDecimal(it * publicKey).remainder(BigDecimal(x)).toInt() == 1 }
//    println("totient: $x")
//    println("product: $n")
//    println("public key: $publicKey")
//    println("private key: $privateKey")
//    val admin = admin(publicKey, n)
//    println("encrypted: $admin")
//    //decrypt CIPHER TEXT^PRIVATE KEY MOD PRODUCT
//    val decrypt = BigDecimal(admin).pow(privateKey).remainder(BigDecimal(n))
//    println("decrypted value: $decrypt")
//}
//
//fun admin(publicKey: Int, product: Int): Int {
//    // formula for encryption = PLAINTEXT^PUBLIC KEY MOD PRODUCT
//    // the symmetric keys will be unique for each group created by admin
//    // the symmetric key will also be regenerated when a user joins or leaves the group
//    val symmetricKey = 100
//    val result = BigDecimal(symmetricKey).pow(publicKey).remainder(BigDecimal(product))
//    println("result: $result")
//    return result.toInt()
//}
//
//
//fun gcd(n1: Int, n2: Int): Int {
//    return if (n2 == 0) {
//        n1
//    } else gcd(n2, n1 % n2)
//}
//
//
//fun Int.isNotFactor(of: Int): Boolean {
//    return of % this != 0
//}
//
//
///*
//Create a list of consecutive integers from 2 to n: (2, 3, 4, …, n)
//Initially, let p be equal 2, the first prime number
//Starting from p, count up in increments of p and mark each of these numbers greater than p itself in the list.
//These numbers will be 2p, 3p, 4p, etc.; note that some of them may have already been marked
//Find the first number greater than p in the list that is not marked. If there was no such number, stop.
// Otherwise, let p now equal this number (which is the next prime), and repeat from step 3
// */
//fun sieveOfEratosthenes(n: Int): List<Int> {
//    val prime = BooleanArray(n + 1)
//    Arrays.fill(prime, true)
//    var p = 2
//    while (p * p <= n) {
//        if (prime[p]) {
//            var i = p * 2
//            while (i <= n) {
//                prime[i] = false
//                i += p
//            }
//        }
//        p++
//    }
//    val primeNumbers: MutableList<Int> = LinkedList()
//    for (i in 2..n) {
//        if (prime[i]) {
//            primeNumbers.add(i)
//        }
//    }
//    return primeNumbers
//}