package com.aestroon.common.data

import android.util.Log
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateSerializer : KSerializer<Date> {
    private val formatWithMilliseconds = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val formatWithoutMilliseconds = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(formatWithMilliseconds.format(value))
    }

    override fun deserialize(decoder: Decoder): Date {
        val dateString = decoder.decodeString()
        try {
            return formatWithMilliseconds.parse(dateString)!!
        } catch (e: Exception) {
            try {
                return formatWithoutMilliseconds.parse(dateString)!!
            } catch (e: Exception) {
                e.localizedMessage?.let { Log.e("DateSerializer", it) }
                throw IllegalStateException("Cannot parse date: '$dateString'. It doesn't match expected formats.", e)
            }
        }
    }
}
