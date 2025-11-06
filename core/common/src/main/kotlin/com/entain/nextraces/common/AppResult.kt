package com.entain.nextraces.common

sealed class AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>()
    data class Error(val error: AppError) : AppResult<Nothing>()
}

sealed class AppError(open val cause: Throwable? = null) {
    data class Network(override val cause: Throwable? = null) : AppError(cause)
    data class Serialization(override val cause: Throwable? = null) : AppError(cause)
    data class Server(val code: Int, override val cause: Throwable? = null) : AppError(cause)
    data class Unknown(override val cause: Throwable? = null) : AppError(cause)
}
