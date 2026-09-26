package com.example.data.repository

import com.example.data.local.dao.PresetDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.entity.ExportPreset
import com.example.data.local.entity.OptimizedProject
import kotlinx.coroutines.flow.Flow

class OptimizedRepository(
    private val projectDao: ProjectDao,
    private val presetDao: PresetDao
) {
    val allProjects: Flow<List<OptimizedProject>> = projectDao.getAllProjects()
    val allPresets: Flow<List<ExportPreset>> = presetDao.getAllPresets()

    fun getRecentProjects(limit: Int = 5): Flow<List<OptimizedProject>> =
        projectDao.getRecentProjects(limit)

    suspend fun getProjectById(id: Long): OptimizedProject? =
        projectDao.getProjectById(id)

    suspend fun saveProject(project: OptimizedProject): Long =
        projectDao.insertProject(project)

    suspend fun updateProject(project: OptimizedProject) =
        projectDao.updateProject(project)

    suspend fun deleteProject(id: Long) =
        projectDao.deleteProjectById(id)

    suspend fun clearProjects() =
        projectDao.clearAllProjects()

    suspend fun savePreset(preset: ExportPreset): Long =
        presetDao.insertPreset(preset)

    suspend fun deletePreset(id: Long) =
        presetDao.deletePresetById(id)

    suspend fun getProjectCount(): Int =
        projectDao.getProjectCount()
}
