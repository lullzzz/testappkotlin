package com.gshubina.lullzzz.testappkotlin.service.simulator

import android.util.Log
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.stream.Stream
import kotlin.math.abs
import kotlin.math.sin

class TachometerSimulator(filterFunction : Predicate<Double>) {
    private val LOG_TAG = TachometerSimulator::class.simpleName
    private val DATA_GENERATION_PERIOD_MS = 300

    private var mFilterFunction : Predicate<Double> = filterFunction

    private val supplier: Supplier<Long> = Supplier {
        try {
            Thread.sleep(DATA_GENERATION_PERIOD_MS.toLong())
        } catch (e: InterruptedException) {
            Log.w(LOG_TAG, e.message)
        }
        java.time.Instant.now().toEpochMilli()
    }

    private fun buildStream(): Stream<Long> {
        return Stream.generate(supplier)
    }

    private fun tachometerFunction(t: Long): Double {
        return abs(10000 * sin(0.0001 * t))
    }


    fun tachometerStream(): Stream<Double> = buildStream()
        .map(this::tachometerFunction)
        .filter(mFilterFunction)

}