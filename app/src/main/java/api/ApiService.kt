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

    @POST("register")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<LoginResponse>

    // Data Submission
    @POST("submit_data")
    suspend fun submitNetworkData(
        @Body data: CellRecordSubmission,
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>


}