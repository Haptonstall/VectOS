package com.lz.vectos.domain.project

import com.lz.vectos.domain.structural.ProjectDesignContext
import java.util.UUID
import java.time.LocalDateTime

data class Project(
    val id: UUID,
    val name: String,
    val projectNumber: String? = null,
    val siteLocation: String? = null,
    val description: String?,
    val clientName: String?,
    val engineerName: String?,
    val createdAt: LocalDateTime,
    val designContext: ProjectDesignContext
)
