package com.joel.communication.states

import com.joel.communication.models.ErrorResponse

sealed class ResultState<out T> {
    data class Error<T>(val error: ErrorResponse, val data: T? = null) : ResultState<T>()
    data class Loading<T>(val data: T? = null) : ResultState<T>()
    object Empty : ResultState<Nothing>()
    data class Success<out T>(val data: T) : ResultState<T>()
    object EmptyData : ResultState<Nothing>()
}
