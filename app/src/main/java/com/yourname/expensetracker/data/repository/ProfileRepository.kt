package com.yourname.expensetracker.data.repository

import com.yourname.expensetracker.data.local.dao.ProfileDao
import com.yourname.expensetracker.data.local.entity.Profile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao
) {
    fun getAllProfiles(): Flow<List<Profile>> = profileDao.getAllProfiles()

    suspend fun getAllProfilesList(): List<Profile> = profileDao.getAllProfilesList()

    suspend fun getProfileById(id: Long): Profile? = profileDao.getProfileById(id)

    fun getProfilesByType(type: String): Flow<List<Profile>> = profileDao.getProfilesByType(type)

    suspend fun insert(profile: Profile): Long = profileDao.insert(profile)

    suspend fun update(profile: Profile) = profileDao.update(profile)

    suspend fun delete(profile: Profile) = profileDao.delete(profile)

    suspend fun deleteById(id: Long) = profileDao.deleteById(id)
}
