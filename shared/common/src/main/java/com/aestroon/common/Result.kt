package com.aestroon.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable? = null) : Result<Nothing>
    object Loading : Result<Nothing>
}

inline fun <T> Result<T>.onLoading(block: () -> Unit) = this.also {
    if (it is Result.Loading) {
        block()
    }
}

inline fun <T> Result<T>.onSuccess(block: (T) -> Unit) = this.also {
    (it as? Result.Success)?.data?.let { data ->
        block(data)
    }
}

inline fun <T> Result<T>.onError(block: (Throwable) -> Unit) = this.also {
    (it as? Result.Error)?.exception?.let { exception ->
        block(exception)
    }
}

val <T> Result<T>.data: T?
    get() = (this as? Result.Success)?.data

fun <T, K> Flow<Result<T>>.mapValue(block: (T) -> K) = map {
    when (it) {
        is Result.Error -> Result.Error(it.exception)
        Result.Loading -> Result.Loading
        is Result.Success -> Result.Success(block(it.data))
    }
}

fun <T> Flow<Result<T>>.onSuccess(block: suspend (T) -> Unit) = transform { value ->
    (value as? Result.Success)?.data?.let { data ->
        block(data)
    }
    return@transform emit(value)
}
