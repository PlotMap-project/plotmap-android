package com.plotmap.app.core.network
import com.plotmap.app.core.network.dto.CreateProjectRequest
import com.plotmap.app.core.network.dto.GenerateProjectRequest
import com.plotmap.app.core.network.dto.GraphResponse
import com.plotmap.app.core.network.dto.HealthResponse
import com.plotmap.app.core.network.dto.ProjectResponse
import com.plotmap.app.core.network.dto.UpdateProjectRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface PlotMapApi {
    @GET("health")
    suspend fun health(): HealthResponse

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest,
    ): AuthResponse

    @POST("auth/login/email")
    suspend fun login(
        @Body request: LoginEmailRequest,
    ): AuthResponse

    @POST("auth/login/name")
    suspend fun loginByName(
        @Body request: LoginNameRequest,
    ): AuthResponse

    @GET("projects")
    suspend fun getProjects(): List<ProjectResponse>

    @POST("projects")
    suspend fun createProject(
        @Body request: CreateProjectRequest,
    ): ProjectResponse

    @PATCH("projects/{projectId}")
    suspend fun updateProject(
        @Path("projectId") id: String,
        @Body request: UpdateProjectRequest,
    ): ProjectResponse

    @DELETE("projects/{projectId}")
    suspend fun deleteProject(
        @Path("projectId") id: String,
    )

    @GET("projects/{projectId}")
    suspend fun getProjectDetails(
        @Path("projectId") id: String,
    ): GraphResponse

    @POST("projects/generate")
    suspend fun generateProject(
        @Body request: GenerateProjectRequest,
    ): GraphResponse
}
