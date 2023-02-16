package com.example.database

import com.example.database.models.*
import io.ktor.server.websocket.*
import org.litote.kmongo.contains
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


class ChatServiceImpl : ChatService {
    //kmongo client for mongodb
    private val client = KMongo.createClient().coroutine

    //this field holds all online users
    var onlineUsers = Collections.synchronizedSet<ActiveUsers?>(LinkedHashSet())

    //databases
    private val database = client.getDatabase("chat_db")

    //collections
    private val groups = database.getCollection<Group>()
    private val users = database.getCollection<User>()
    private val userIds = database.getCollection<UserIds>()
    override suspend fun createGroup(group: Group): Boolean {
        if (groupExists(group.groupId)) return false
        return groups.insertOne(group).wasAcknowledged()
    }


    override suspend fun fetchAllGroups(): List<Group> {
        return groups.find().toList()
    }

    override suspend fun register(username: String, password: String): Boolean {
        if (userExist(username)) return false
        return users.insertOne(
            User(username, password, UUID.randomUUID().toString())
        ).wasAcknowledged()
    }

    override suspend fun addUserToActive(userId: String, session: DefaultWebSocketServerSession) {
        val user = getUserById(userId) ?: return
        val activeUser = ActiveUsers(user.userId, session)
        onlineUsers.plusAssign(activeUser)
    }

    override suspend fun removeUserFromActive(userId: String) {
        val user = getUserById(userId) ?: return
        val activeUser = onlineUsers.find { it.userId == user.userId } ?: return
        onlineUsers.minusAssign(activeUser)
    }

    override suspend fun login(username: String, password: String) {
        TODO("Not yet implemented")
    }

    override suspend fun groupExists(groupId: String): Boolean {
        return groups.findOne(Group::groupId eq groupId) != null
    }

    override suspend fun userExist(username: String): Boolean {
        return users.findOne(User::username eq username) != null
    }

    override suspend fun getGroupById(groupId: String): Group? {
        return groups.findOne(Group::groupId eq groupId)
    }

    override suspend fun getUserByName(userName: String): User? {
        return users.findOne(User::username eq userName)
    }

    override suspend fun getUserById(userId: String): User? {
        return users.findOne(User::userId eq userId)
    }

    override suspend fun getUserGroups(user: User): List<Group> {
        if (!userExist(user.username)) return emptyList()
        return groups.find(
            Group::users contains user
        ).toList()
    }

    override suspend fun fetchMessagesForGroup(groupId: String): List<Message> {
        TODO("Not yet implemented")
    }

    override suspend fun getIdForGroup(): Int {
        val userIdList = userIds.find().toList()
        var lastId = UserIds(UUID.randomUUID().toString(), 0)
        if (userIdList.isEmpty()) {
            userIds.insertOne(lastId)
        } else {
            lastId = userIdList[0]
            lastId.value = AtomicInteger(lastId.value).incrementAndGet()
            userIds.updateOneById(lastId._id, lastId)
        }
        return AtomicInteger(lastId.value).incrementAndGet()
    }
}