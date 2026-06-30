package com.plotmap.app.core.network
import com.plotmap.app.core.network.dto.AddChapterRequest
import com.plotmap.app.core.network.dto.AddChapterResponse
import com.plotmap.app.core.network.dto.ChapterDetailDto
import com.plotmap.app.core.network.dto.ChapterDto
import com.plotmap.app.core.network.dto.CreateEventRequest
import com.plotmap.app.core.network.dto.CreateProjectRequest
import com.plotmap.app.core.network.dto.EventDto
import com.plotmap.app.core.network.dto.GenerateProjectRequest
import com.plotmap.app.core.network.dto.GraphResponse
import com.plotmap.app.core.network.dto.HealthResponse
import com.plotmap.app.core.network.dto.JobStatusResponse
import com.plotmap.app.core.network.dto.ProjectResponse
import com.plotmap.app.core.network.dto.UpdateEventRequest
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
    ): JobStatusResponse

    @GET("projects/{projectId}/chapters")
    suspend fun getChapters(
        @Path("projectId") projectId: String,
    ): List<ChapterDto>

    @GET("projects/{projectId}/chapters/{chapterId}")
    suspend fun getChapterById(
        @Path("projectId") projectId: String,
        @Path("chapterId") chapterId: String,
    ): ChapterDetailDto

    @POST("projects/{projectId}/chapters")
    suspend fun addChapter(
        @Path("projectId") projectId: String,
        @Body request: AddChapterRequest,
    ): AddChapterResponse

    @PATCH("projects/{projectId}/chapters/{chapterId}")
    suspend fun updateChapter(
        @Path("projectId") projectId: String,
        @Path("chapterId") chapterId: String,
        @Body request: AddChapterRequest,
    ): AddChapterResponse

    @POST("projects/{projectId}/events")
    suspend fun createEvent(
        @Path("projectId") projectId: String,
        @Body request: CreateEventRequest,
    ): EventDto

    @PATCH("projects/{projectId}/events/{eventId}")
    suspend fun updateEvent(
        @Path("projectId") projectId: String,
        @Path("eventId") eventId: String,
        @Body request: UpdateEventRequest,
    ): EventDto

    @GET("jobs/{jobId}")
    suspend fun getJobStatus(
        @Path("jobId") jobId: String,
    ): JobStatusResponse
}
