package api

import com.example.networkcellanalyzer.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Authentication
    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("register")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<LoginResponse>

    // Data Submission
    @POST("submit_data")
    suspend fun submitNetworkData(
        @Body data: CellRecordSubmission,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>

    // Statistics
    @GET("stats/operator")
    suspend fun getOperatorStats(
        @Header("Authorization") token: String,
        @Query("device_id") deviceId: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Map<String, String>

    @GET("stats/network_type")
    suspend fun getNetworkTypeStats(
        @Header("Authorization") token: String,
        @Query("device_id") deviceId: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Map<String, String>

    @GET("stats/signal_power_per_network")
    suspend fun getSignalPowerPerNetwork(
        @Header("Authorization") token: String,
        @Query("device_id") deviceId: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Map<String, Double>

    @GET("stats/signal_power_per_device")
    suspend fun getSignalPowerPerDevice(
        @Header("Authorization") token: String,
        @Query("device_id") deviceId: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): SignalPowerDeviceResponse

    @GET("stats/sinr_per_network")
    suspend fun getSINRStats(
        @Header("Authorization") token: String,
        @Query("device_id") deviceId: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String
    ): Map<String, Double>
}
