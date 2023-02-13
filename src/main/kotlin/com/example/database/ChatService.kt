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

fun main() {
    val primeNumbers = sieveOfEratosthenes(5000)
    val p = primeNumbers.random()
    val availableRoots = (0 until p).filter { isPrimitive(it, p) }
    println("$p has ${availableRoots.count()} primitive roots: $availableRoots")
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