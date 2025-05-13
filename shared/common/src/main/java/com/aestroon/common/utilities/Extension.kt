package com.aestroon.common.utilities

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update

fun <K, V> MutableStateFlow<Map<K, V>>.update(key: K, value: V) {
    update {
        it.plus(key to value)
    }
}

fun <K, V> Map<K, V>.update(key: K, value: V) =
    this.plus(key to value)

fun <K, V> Map<K, V>.delete(key: K) =
    this.minus(key)

suspend inline fun <T> Flow<Result<T>>.await() = await { }

suspend inline fun <T> Flow<Result<T>>.await(crossinline onLoading: suspend () -> Unit) =
    firstOrNull {
        it.onLoading {
            onLoading()
        }
        it != Result.Loading
    } ?: awaitCancellation()