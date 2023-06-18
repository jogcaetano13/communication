package com.joel.communication_android.deserializables

import com.joel.communication_android.builders.ResponseBuilder
import com.joel.communication_android.extensions.toEnvelopeList
import com.joel.communication_android.extensions.toError
import com.joel.communication_android.extensions.toList
import com.joel.communication_android.extensions.toModel
import com.joel.communication_android.extensions.toModelWrapped
import com.joel.communication_android.states.AsyncState
import com.joel.communication_core.enums.ErrorResponseType
import com.joel.communication_core.request.CommunicationRequest
import com.joel.communication_core.response.CommunicationResponse
import com.joel.communication_core.response.ErrorResponse

/**
 * Deserialize the request into a [AsyncState].
 *
 * This method receives a [ResponseBuilder] as parameter to customize the response.
 *
 * This is a suspend functions, so you need to call it from a coroutine or from another suspend function.
 * The dispatcher is handled by the function, so it can be called from any thread.
 *
 * @return [AsyncState] with success (with data as [T]) or error (with error as [ErrorResponse]).
 */
suspend inline fun <reified T : Any> CommunicationRequest.responseAsync(
    crossinline responseBuilder: ResponseBuilder<T>. () -> Unit = {}
): AsyncState<T> {
    return toAsync(
        responseBuilder = responseBuilder,
        deserializeBlock = { it.toModel<T>(builder.dateFormat) }
    )
}

/**
 * Deserialize the request into a [AsyncState] list.
 *
 * This method receives a [ResponseBuilder] as parameter to customize the response.
 *
 * This is a suspend functions, so you need to call it from a coroutine or from another suspend function.
 * The dispatcher is handled by the function, so it can be called from any thread.
 *
 * @return [AsyncState] with success (with data as [List] of type [T]) or error (with error as [ErrorResponse]).
 */
suspend inline fun <reified T : Any> CommunicationRequest.responseListAsync(
    crossinline responseBuilder: ResponseBuilder<List<T>>. () -> Unit = {}
): AsyncState<List<T>> {
    return toAsync(
        responseBuilder = responseBuilder,
        deserializeBlock = { it.toList(builder.dateFormat) }
    )
}

/**
 * Deserialize the request into a [AsyncState] list wrapped by the data json object.
 * The list wrapped in json object response should be called "data"
 *
 * This method receives a [ResponseBuilder] as parameter to customize the response.
 *
 * This is a suspend functions, so you need to call it from a coroutine or from another suspend function.
 * The dispatcher is handled by the function, so it can be called from any thread.
 *
 * @return [AsyncState] with success (with data as [List] of type [T]) or error (with error as [ErrorResponse]).
 */
suspend inline fun <reified T : Any> CommunicationRequest.responseWrappedListAsync(
    crossinline responseBuilder: ResponseBuilder<List<T>>. () -> Unit = {}
): AsyncState<List<T>> {
    return toAsync(
        responseBuilder = responseBuilder,
        deserializeBlock = { it.toEnvelopeList<T>(builder.dateFormat).data }
    )
}

/**
 * Deserialize the request into a [AsyncState] wrapped by the data json object.
 * The object wrapped in json object response should be called "data"
 *
 * This method receives a [ResponseBuilder] as parameter to customize the response.
 *
 * This is a suspend functions, so you need to call it from a coroutine or from another suspend function.
 * The dispatcher is handled by the function, so it can be called from any thread.
 *
 * @return [AsyncState] with success (with data as [T]) or error (with error as [ErrorResponse]).
 */
suspend inline fun <reified T: Any> CommunicationRequest.responseWrappedAsync(
    crossinline responseBuilder: ResponseBuilder<T>. () -> Unit = {}
): AsyncState<T> {
    return toAsync(
        responseBuilder = responseBuilder,
        deserializeBlock = { it.toModelWrapped<T>(builder.dateFormat) }
    )
}

@PublishedApi
internal suspend inline fun <reified T: Any> CommunicationRequest.toAsync(
    crossinline responseBuilder: ResponseBuilder<T>. () -> Unit,
    deserializeBlock: (CommunicationResponse) -> T?
): AsyncState<T> {
    val response = ResponseBuilder<T>().also(responseBuilder)

    builder.preCall?.invoke()

    val callResponse = response()

    response.post?.invoke()

    return if (callResponse.isSuccess) {
        val result = deserializeBlock(callResponse)
        result?.let {
            response.onNetworkSuccess?.invoke(it)
            AsyncState.Success(it)
        } ?: AsyncState.Error(ErrorResponse(404, "Not found", ErrorResponseType.Empty))

    } else {
        AsyncState.Error(callResponse.toError())
    }
}