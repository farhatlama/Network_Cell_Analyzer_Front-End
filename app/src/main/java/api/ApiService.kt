package api

import com.example.networkcellanalyzer.model.CellRecordSubmission
import com.example.networkcellanalyzer.model.LoginRequest
import com.example.networkcellanalyzer.model.LoginResponse
import com.example.networkcellanalyzer.model.SignupRequest
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Authentication
    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("signup")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<LoginResponse>

    // Data Submission
    @POST("submit_data")
    suspend fun submitNetworkData(
        @Body data: CellRecordSubmission,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    // Stats
    @GET("stats/operator")
    suspend fun getOperatorStats(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("device_id") deviceId: String,
        @Header("Authorization") token: String
    ): Response<Map<String, String>>

    @GET("stats/network_type")
    suspend fun getNetworkTypeStats(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("device_id") deviceId: String,
        @Header("Authorization") token: String
    ): Response<Map<String, String>>

    @GET("stats/signal_power_per_network")
    suspend fun getSignalPowerPerNetwork(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("device_id") deviceId: String,
        @Header("Authorization") token: String
    ): Response<Map<String, Double>>

    @GET("stats/sinr_per_network")
    suspend fun getSinrPerNetwork(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("device_id") deviceId: String,
        @Header("Authorization") token: String
    ): Response<Map<String, Double>>
}