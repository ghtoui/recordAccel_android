package com.moritoui.recordaccel.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.moritoui.recordaccel.model.User
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface UserListDataRepository {
    val userList: StateFlow<List<User>>
    suspend fun saveUserList()
    suspend fun loadUserList()
    fun addUser(user: User)
    fun removeUser(user: User)
}

class UserListDataRepositoryImpl @Inject constructor(
    private val userListDataStore: DataStore<Preferences>,
    private val jsonAdapter: JsonAdapter<MutableList<User>>
) : UserListDataRepository {
    private val USER_LIST = "user_list"
    private val _userList = MutableStateFlow<MutableList<User>>(mutableListOf())
    override val userList: StateFlow<List<User>> = _userList.asStateFlow()

    override suspend fun saveUserList() {
        val userListJson = jsonAdapter.toJson(_userList.value)
        userListDataStore.edit { settings ->
            settings[stringPreferencesKey(USER_LIST)] = userListJson
        }
        loadUserList()
    }

    override suspend fun loadUserList() {
        val value: String = userListDataStore.data
            .map { preferences ->
                preferences[stringPreferencesKey(USER_LIST)] ?: "[]"
            }.first()
        val json = jsonAdapter.fromJson(value)
        if (json != null) {
            _userList.value = json
        }
    }

    override fun addUser(user: User) {
        _userList.value.add(user)
    }

    override fun removeUser(user: User) {
        _userList.value.remove(user)
    }
}
