package com.example.database

import com.example.Message
import java.math.BigDecimal
import java.util.*


interface ChatService {
    suspend fun createGroup()

    suspend fun fetchAllGroups()
    suspend fun register()
    suspend fun login()

    suspend fun fetchMessagesForGroup(groupId: String): List<Message>
    // suspend fun fetch
}

/*
 to create and manage the group there must be an admin, process listed below:
 1. admin must create a random symmetric key
 2. admin must send this key to each recipient of the group via asymmetric encryption
 3. all further messages will then be encrypted and decrypted using this key
 4. if a new party joins the group then send to
 5. if party leaves then regenerate step 1
 let say admin generates random key 24
 */

fun main() {
    // val primeNumbers = sieveOfEratosthenes(5000)
    // val p = primeNumbers.random()
    // val availableRoots = (0 until p).filter { isPrimitive(it, p) }
    //println("$p has ${availableRoots.count()} primitive roots: $availableRoots")
    val p1 = 7;
    val q = 19
    val n = p1 * q
    val totient = 6 * 18
    val publicKey = 29
    val privateKey = 41
    val plainText = 99
    //formula for encryption = PLAINTEXT^PUBLIC KEY MOD PRODUCT
    val encrypted = BigDecimal(99).pow(publicKey).remainder(BigDecimal(n))
    println(encrypted)
}


fun isPrimitive(number: Int, prime: Int): Boolean {
    val list = mutableListOf<BigDecimal>()
    for (i in 0 until prime - 1) {
        //number^n - 1 mod prime
        val result = BigDecimal(number).pow(i).remainder(BigDecimal(prime))
        if (result in list) {
            return false
        }
        list.add(result)
    }
    return true
}


/*
Create a list of consecutive integers from 2 to n: (2, 3, 4, …, n)
Initially, let p be equal 2, the first prime number
Starting from p, count up in increments of p and mark each of these numbers greater than p itself in the list.
These numbers will be 2p, 3p, 4p, etc.; note that some of them may have already been marked
Find the first number greater than p in the list that is not marked. If there was no such number, stop.
 Otherwise, let p now equal this number (which is the next prime), and repeat from step 3
 */
fun sieveOfEratosthenes(n: Int): List<Int> {
    val prime = BooleanArray(n + 1)
    Arrays.fill(prime, true)
    var p = 2
    while (p * p <= n) {
        if (prime[p]) {
            var i = p * 2
            while (i <= n) {
                prime[i] = false
                i += p
            }
        }
        p++
    }
    val primeNumbers: MutableList<Int> = LinkedList()
    for (i in 2..n) {
        if (prime[i]) {
            primeNumbers.add(i)
        }
    }
    return primeNumbers
}