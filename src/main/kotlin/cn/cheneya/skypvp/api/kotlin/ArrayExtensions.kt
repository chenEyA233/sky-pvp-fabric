@file:Suppress("NOTHING_TO_INLINE", "detekt:TooManyFunctions")

package cn.cheneya.skypvp.api.kotlin

import it.unimi.dsi.fastutil.doubles.DoubleIterable
import it.unimi.dsi.fastutil.doubles.DoubleIterator
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet
import it.unimi.dsi.fastutil.ints.IntList
import it.unimi.dsi.fastutil.ints.IntSet
import java.util.*
import java.util.stream.Stream

inline infix operator fun IntRange.contains(range: IntRange): Boolean {
    return this.first <= range.first && this.last >= range.last
}

fun ClosedFloatingPointRange<Float>.valueAtProportion(proportion: Float): Float {
    return when {
        proportion >= 1f -> endInclusive
        proportion <= 0f -> start
        else -> start + (endInclusive - start) * proportion
    }
}

fun ClosedFloatingPointRange<Float>.proportionOfValue(value: Float): Float {
    return when {
        value >= endInclusive -> 1f
        value <= start -> 0f
        else -> (value - start) / (endInclusive - start)
    }
}

// https://stackoverflow.com/questions/44315977/ranges-in-kotlin-using-data-type-double
infix fun ClosedRange<Double>.step(step: Double): DoubleIterable {
    require(start.isFinite())
    require(endInclusive.isFinite())
    require(step > 0.0)

    return DoubleIterable {
        object : DoubleIterator {
            private var current = start
            private var hasNextValue = current <= endInclusive

            override fun hasNext(): Boolean = hasNextValue

            override fun nextDouble(): Double {
                if (!hasNextValue) {
                    throw NoSuchElementException()
                }

                val nextValue = current
                current += step
                if (current > endInclusive) {
                    hasNextValue = false
                }

                return nextValue
            }

            override fun remove() {
                throw UnsupportedOperationException("This iterator is read-only")
            }
        }
    }
}

inline fun range(iterable: DoubleIterable, operation: (Double) -> Unit) {
    iterable.doubleIterator().apply {
        while (hasNext()) {
            operation(nextDouble())
        }
    }
}

inline fun range(iterable: IntProgression, operation: (Int) -> Unit) {
    iterable.iterator().apply {
        while (hasNext()) {
            operation(nextInt())
        }
    }
}

inline fun range(iterable1: DoubleIterable, iterable2: DoubleIterable, operation: (Double, Double) -> Unit) {
    range(iterable1) { d1 ->
        range(iterable2) { d2 ->
            operation(d1, d2)
        }
    }
}

inline fun range(
    iterable1: IntProgression,
    iterable2: IntProgression,
    iterable3: IntProgression,
    operation: (Int, Int, Int) -> Unit
) {
    range(iterable1) { d1 ->
        range(iterable2) { d2 ->
            range(iterable3) { d3 ->
                operation(d1, d2, d3)
            }
        }
    }
}

fun ClosedFloatingPointRange<Float>.random(): Float {
    require(start.isFinite())
    require(endInclusive.isFinite())
    return (start + (endInclusive - start) * Math.random()).toFloat()
}

fun ClosedFloatingPointRange<Double>.random(): Double {
    require(start.isFinite())
    require(endInclusive.isFinite())
    return start + (endInclusive - start) * Math.random()
}

fun ClosedFloatingPointRange<Float>.toDouble(): ClosedFloatingPointRange<Double> {
    require(start.isFinite())
    require(endInclusive.isFinite())
    return start.toDouble()..endInclusive.toDouble()
}

fun <T> List<T>.subList(fromIndex: Int): List<T> {
    return this.subList(fromIndex, this.size)
}

/**
 * A JavaScript-styled forEach
 */
inline fun <T, C : Collection<T>> C.forEachWithSelf(action: (T, index: Int, self: C) -> Unit) {
    forEachIndexed { i, item ->
        action(item, i, this)
    }
}

inline fun Sequence<*>.isNotEmpty(): Boolean {
    return iterator().hasNext()
}

inline fun Sequence<*>.isEmpty(): Boolean {
    return !isNotEmpty()
}

inline fun <reified T : Enum<T>> Array<out T>.toEnumSet(): EnumSet<T> =
    emptyEnumSet<T>().apply { addAll(this@toEnumSet) }

inline fun <reified T : Enum<T>> Iterable<T>.toEnumSet(): EnumSet<T> =
    emptyEnumSet<T>().apply { addAll(this@toEnumSet) }

inline fun <reified T : Enum<T>> emptyEnumSet(): EnumSet<T> =
    EnumSet.noneOf(T::class.java)

/**
 * Directly map to a typed array
 */
inline fun <T, reified R> Array<T>.mapArray(transform: (T) -> R): Array<R> = Array(this.size) { idx ->
    transform(this[idx])
}

/**
 * Directly map to a typed array
 */
inline fun <T, reified R> Collection<T>.mapArray(transform: (T) -> R): Array<R> = with(iterator()) {
    Array(size) {
        transform(next())
    }
}

inline fun <T> Collection<T>.mapInt(transform: (T) -> Int): IntList {
    val result = IntArrayList(this.size)
    for (element in this) {
        result.add(transform(element))
    }
    return result
}

inline fun <T> Collection<T>.mapIntSet(transform: (T) -> Int): IntSet {
    val result = IntLinkedOpenHashSet(this.size * 4 / 3)
    for (element in this) {
        result.add(transform(element))
    }
    return result
}

/**
 * Inserts a new element into a sorted list while maintaining the order.
 */
inline fun <T, K : Comparable<K>> MutableList<T>.sortedInsert(item: T, crossinline selector: (T) -> K?) {
    val insertIndex = binarySearchBy(selector(item), selector = selector).let {
        if (it >= 0) it else it.inv()
    }

    add(insertIndex, item)
}

/**
 * Transform a String to another String with same length by given [transform]
 */
inline fun String.mapString(transform: (Char) -> Char) = String(CharArray(length) {
    transform(this[it])
})

/**
 * Transform a Collection to a String with by given [transform]
 */
inline fun <T> Collection<T>.mapString(transform: (T) -> Char) = with(iterator()) {
    String(CharArray(size) {
        transform(next())
    })
}

inline fun <reified T> Stream<T>.toTypedArray(): Array<T> = toArray(::arrayOfNulls)
