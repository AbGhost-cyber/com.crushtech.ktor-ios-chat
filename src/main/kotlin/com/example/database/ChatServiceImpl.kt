package com.example.database

import com.example.database.models.*
import kotlinx.coroutines.isActive
import org.bson.types.ObjectId
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
    private var onlineUsers = Collections.synchronizedSet<ActiveUser?>(LinkedHashSet())

    //databases
    private val database = client.getDatabase("chat_db")

    //collections
    private val groups = database.getCollection<Group>()
    private val users = database.getCollection<User>()
    private val userIds = database.getCollection<UserIds>()
    private val userGroupAcceptKeys = database.getCollection<GroupAccept>()

    override suspend fun upsertGroup(group: Group): Boolean {
        if (groupExists(group.groupId)) {
            return groups.updateOneById(group.id, group).wasAcknowledged()
        }
        return groups.insertOne(group).wasAcknowledged()
    }


    override suspend fun fetchAllGroups(): List<Group> {
        return groups.find().toList()
    }

    override suspend fun register(user: User): Boolean {
        if (userExist(user.username)) return false
        return users.insertOne(user).wasAcknowledged()
    }

    override suspend fun addUserToActive(activeUser: ActiveUser) {
        if (!userExist(activeUser.username)) return
        if (onlineUsers.find { it.username == activeUser.username } == null)
            onlineUsers.plusAssign(activeUser)
    }

    override suspend fun getActiveUserByName(username: String): ActiveUser? {
        return onlineUsers.find { it.username == username }
    }

    override suspend fun getActiveUsers(): Set<ActiveUser> {
        return onlineUsers
    }

    override suspend fun userIsOnline(username: String): Boolean {
        if (!userExist(username)) {
            return false
        }
        val activeUser = onlineUsers.find { it.username == username } ?: return false

        return activeUser.session.isActive
    }

    override suspend fun removeUserFromActive(username: String) {
        val user = getUserByName(username) ?: return
        val activeUser = onlineUsers.find { it.username == user.username } ?: return
        onlineUsers.minusAssign(activeUser)
    }

    override suspend fun getUserEncryptedGroupKeys(username: String): List<GroupAccept> {
        return userGroupAcceptKeys
            .find(GroupAccept::username eq username)
            .toList()
    }

    override suspend fun upsertUserEncryptedGKey(key: GroupAccept): Boolean {
        if (userGroupAcceptKeys.findOneById(key.id) == null) {
            return userGroupAcceptKeys.insertOne(key).wasAcknowledged()
        }
        return userGroupAcceptKeys.updateOneById(key.id, key).wasAcknowledged()
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

    override suspend fun getUserById(id: ObjectId): User? {
        return users.findOneById(id)
    }

    override suspend fun getUserGroups(user: String): List<Group> {
        if (!userExist(user)) return emptyList()
        return groups.find(
            Group::users contains user
        ).toList()
    }

    override suspend fun fetchMessagesForGroup(groupId: String): List<Message> {
        TODO("Not yet implemented")
    }

    override suspend fun getIdForGroup(): Int {
        val userIdList = userIds.find().toList()
        var lastId = UserIds(value = 0)
        if (userIdList.isEmpty()) {
            userIds.insertOne(lastId)
        } else {
            lastId = userIdList[0]
            lastId.value = AtomicInteger(lastId.value).incrementAndGet()
            userIds.updateOneById(lastId.id, lastId)
        }
        return AtomicInteger(lastId.value).incrementAndGet()
    }
}