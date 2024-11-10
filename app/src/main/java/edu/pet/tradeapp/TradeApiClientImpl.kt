package edu.pet.tradeapp

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

data class RegistrationRequestDto(val login: String, val password: String, val email: String)
data class RegistrationResponseDto(val token: String, val expireAt: Int)

data class LoginRequestDto(val login: String, val password: String)
data class LoginResponseDto(val token: String, val approveType: Int, val expireAt: Int)

data class ApproveByEmailRequestDto(val emailCode: String)
data class ApproveByPushRequestDto(val secretKey: String)

data class TradeApiErrorDto(val code: Int, val message: String)

interface TradeApiClient {
    fun registration(requestDto: RegistrationRequestDto, aid: String): RegistrationResponseDto
    fun login(requestDto: LoginRequestDto, aid: String): LoginResponseDto
    fun approveByEmail(requestDto: ApproveByEmailRequestDto, token: String): LoginResponseDto
    fun approveByPush(requestDto: ApproveByPushRequestDto, token: String) : LoginResponseDto
}

class TradeApiClientImpl(private val context: Context) : TradeApiClient {
    private val gson = GsonBuilder().create()
    private val webClient = OkHttpClient()

    override fun registration(
        requestDto: RegistrationRequestDto,
        aid: String
    ): RegistrationResponseDto {
        val request = Request.Builder()
            .url(context.getString(R.string.base_url)+context.getString(R.string.registration_path))
            .addHeader(context.getString(R.string.aid_header), aid)
            .post(
                gson.toJson(requestDto).toRequestBody(
                    context.getString(R.string.json_media_type).toMediaTypeOrNull()
                )
            )
            .build()

        val response = webClient.newCall(request).execute()

        return handleResponse(response, RegistrationResponseDto::class.java)
    }

    override fun login(requestDto: LoginRequestDto, aid: String): LoginResponseDto {
        val request = Request.Builder()
            .url(context.getString(R.string.base_url)+context.getString(R.string.login_path))
            .addHeader(context.getString(R.string.aid_header), aid)
            .post(
                gson.toJson(requestDto).toRequestBody(
                    context.getString(R.string.json_media_type).toMediaTypeOrNull()
                )
            )
            .build()

        val response = webClient.newCall(request).execute()

        return handleResponse(response, LoginResponseDto::class.java)
    }

    override fun approveByEmail(requestDto: ApproveByEmailRequestDto, token: String): LoginResponseDto {
        val request = Request.Builder()
            .url(context.getString(R.string.base_url)+context.getString(R.string.approve_by_email_path))
            .addHeader(context.getString(R.string.authorization_header), token)
            .post(
                gson.toJson(requestDto).toRequestBody(
                    context.getString(R.string.json_media_type).toMediaTypeOrNull()
                )
            )
            .build()

        val response = webClient.newCall(request).execute()

        return handleResponse(response, LoginResponseDto::class.java)
    }

    override fun approveByPush(requestDto: ApproveByPushRequestDto, token: String): LoginResponseDto {
        val request = Request.Builder()
            .url(context.getString(R.string.base_url)+context.getString(R.string.approve_by_push_path))
            .addHeader(context.getString(R.string.authorization_header), token)
            .post(
                gson.toJson(requestDto).toRequestBody(
                    context.getString(R.string.json_media_type).toMediaTypeOrNull()
                )
            )
            .build()

        val response = webClient.newCall(request).execute()

        return handleResponse(response, LoginResponseDto::class.java)
    }

    private fun <T> handleResponse(response: Response, clazz: Class<T>): T {
        return when (response.code) {
            200 -> gson.fromJson(response.body!!.string(), clazz)
            400 -> throw IllegalArgumentException(
                gson.fromJson(response.body!!.string(), TradeApiErrorDto::class.java).code.toString()
            )
            else -> throw RuntimeException("internal server error")
        }
    }
}