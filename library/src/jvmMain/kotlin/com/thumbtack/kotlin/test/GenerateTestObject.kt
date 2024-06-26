package com.thumbtack.kotlin.test

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.javaType

private const val DEFAULT_COLLECTION_SIZE = 3

/**
 * Generates a test object for the given data class, where each field is given an actual,
 * unique value (as opposed to just some default value like empty string or null).
 * This can be useful for those unit tests that want to ensure field values are unique, for
 * testing things like data transformation, data marshalling and data rendering.
 * String fields are assigned values based on their name and their nesting. For example, for
 * a class:
 * data class TestClass(
 *   val one: String,
 *   val two: Double?,
 *   val three: ArrayList<String>
 * )
 * It's values will be
 * TestClass(one=oneValue, two=0.0, three=[three0Value, three1Value, three2Value])
 *
 * The values generated for each field type are as follows:
 * - Strings are given values of the form <fieldName>Value.
 * - Numbers are set to zero.
 * - Booleans are set to `false`.
 * - String collections: each value is appended their index position.
 * - Maps are set to the values [(<fieldName><index><key>, <fieldName><index><value>, ...]
 * - Nested values are of the form: <parentFieldName>.<childValue>.
 * - Collections of aggregate types have values of the form: <parentFieldName><index>.<child>Value.
 * - Dates are set to the current system clock time unless overridden by [referenceDate]
 * - Instants are set to "zero epoch time" (Jan 1, 1970 +000)
 * - Characters are set to 'a'
 *
 * You can also choose to have specific fields be of a single value, by passing in a map
 * where the key is the regex of the field(s) to set, and the value is the value to assign.
 * Fields are referenced in "dot" notation like ...<parent>.<field>. So if you have the following:
 *
 * data class OuterTestClass(
 *   val inner: InnerTestClass,
 *   val one: String
 * )
 * data class InnerTestClass(
 *   val one: String
 * )
 *
 * You can pass in an override map of ("inner.one".toRegex() to "MyValue") to set inner.one
 * to "MyValue". You can set *all* fields called "one" to the same value by specifying
 * (".*\.?one".toRegex() to "MyValue")
 *
 * There is also the flag [useNullForNullableFields] that you may enable to enforce all nullable
 * fields to be populated with null.
 *
 * @param prefix An initial prefix to give to the test object, to distinguish it from other test
 *     objects you may create.
 * @param overrides A map of override values for particular fields (key is a regex as described
 *     above.
 * @param referenceDate The value to assign to Date fields. If not provided, then the current system
 *     time will be used (however, that could cause test flakiness, so it is usually good to
 *     specify a consistent value for this field.
 * @param useNullForNullableFields If true, then all fields that are nullable will be set to null,
 *     unless specified in the overrides map.
 * @param collectionSize If specified, supported collection types will be set to the specified
 *     size rather than the default of size 3
 * @return The generated object.
 */
fun <T : Any> KClass<T>.generateTestObject(
    prefix: String = "",
    overrides: Map<Regex, Any?>? = null,
    referenceDate: Date? = null,
    useNullForNullableFields: Boolean = false,
    collectionSize: Int = DEFAULT_COLLECTION_SIZE,
): T {
    return generateTestObject(
        prefix,
        Parameters(
            overrides,
            referenceDate,
            useNullForNullableFields,
            collectionSize.takeIf { it > 0 } ?: DEFAULT_COLLECTION_SIZE,
        )
    )
}

@OptIn(ExperimentalStdlibApi::class)
private fun <T : Any> KClass<T>.generateTestObject(prefix: String = "", params: Parameters): T {
    val ctor = this.primaryConstructor ?: throw IllegalArgumentException(
        """
            Could not find primary constructor for generating test object for
            $prefix.${this.simpleName}. This could be because the class $this is not a data class.
            To fix this, try specifying a value for this field in the `overrides` parameter,
            e.g. overrides =
            mapOf("${prefix.replace(".","\\\\.")}".toRegex() to myValue).
        """.trimIndent()
    )
    val ctorValues = mutableListOf<Any?>()
    ctor.parameters.forEach { ctorParam ->
        val fullyQualifiedParamName =
            if (prefix.isNotEmpty()) "$prefix.${ctorParam.name}" else ctorParam.name.orEmpty()
        params.overrides?.entries?.find { it.key.matches(fullyQualifiedParamName) }?.apply {
            // We found a matching override via Regex; use the value in the map.
            ctorValues.add(value)
        } ?: ctorValues.add(
            if (params.useNullForNullableFields && ctorParam.type.isMarkedNullable) {
                null
            } else {
                generateValueForField(
                    ctorParam.type.javaType,
                    fullyQualifiedParamName,
                    params
                )
            }
        )
    }
    return runCatching { ctor.call(*ctorValues.toTypedArray()) }.getOrElse {
        throw IllegalArgumentException(
            """
                Could not create object $prefix.${this.simpleName}. This usually means that
                one of the parameters to the constructor is not constructable by this method.
                It could also mean that one of the override parameters is of the wrong type.
                To resolve this, try specifying an explicit value for this field with the
                `overrides` parameter, and ensure that it's value is of the correct type.
            """.trimIndent(),
            it
        )
    }
}

private fun generateValueForField(type: Type, prefix: String, params: Parameters): Any? {
    return if (type is ParameterizedType) {
        generateValueForParameterizedType(type, prefix, params)
    } else {
        generateValueForSimpleField(type, prefix, params)
    }
}

@Suppress("MagicNumber")
private fun generateValueForParameterizedType(
    type: ParameterizedType,
    prefix: String,
    params: Parameters,
): Any {
    val collectionSize = params.collectionSize
    runCatching {
        when (type.rawType) {
            java.util.List::class.java,
            ArrayList::class.java ->
                return List(collectionSize) { index ->
                    generateValueForField(
                        generateFieldType(type),
                        "$prefix$index",
                        params,
                    )
                }
            java.util.Set::class.java ->
                return List(collectionSize) { index ->
                    generateValueForField(
                        generateFieldType(type),
                        "$prefix$index",
                        params,
                    )
                }.toSet()
            ArrayList::class.java ->
                return List(collectionSize) { index ->
                    generateValueForField(
                        generateFieldType(type),
                        "$prefix$index",
                        params,
                    )
                }
            java.util.Map::class.java ->
                return (0..< collectionSize).associate { index ->
                    generateValueForField(
                        generateFieldType(type, 0),
                        "${prefix}${index}key",
                        params,
                    ) to
                            generateValueForField(
                                generateFieldType(type, 1),
                                "${prefix}${index}value",
                                params,
                            )
                }
            java.util.HashMap::class.java ->
                return HashMap(
                    (0..< collectionSize).associate { index ->
                        generateValueForField(
                            generateFieldType(type, 0),
                            "${prefix}${index}key",
                            params,
                        ) to
                                generateValueForField(
                                    generateFieldType(type, 1),
                                    "${prefix}${index}value",
                                    params,
                                )
                    }
                )
            else ->
                throw IllegalArgumentException(
                    """
                    generateTestObject() does not currently support a parameterized type of
                    raw type ${type.rawType} (for field with prefix $prefix). It currently
                    only supports List, ArrayList, Map and Set.
                    """.trimIndent()
                )
        }
    }.getOrElse {
        throw IllegalArgumentException("Error trying to generate data for field $prefix", it)
    }
}

private fun generateFieldType(paramType: ParameterizedType, index: Int = 0): Type {
    paramType.actualTypeArguments[index].let {
        return when (it) {
            is Class<*> -> it
            is WildcardType -> if (it.upperBounds[0] is ParameterizedType) {
                it.upperBounds[0] as ParameterizedType
            } else {
                // supports generating open classes
                it.upperBounds[0] as Class<*>
            }
            else -> {
                throw IllegalArgumentException("Error trying to resolve type ${paramType.rawType}")
            }
        }
    }
}

private fun generateValueForSimpleField(
    type: Type,
    fullyQualifiedParamName: String,
    params: Parameters,
): Any? {
    if ((type as? Class<*>)?.isEnum == true) {
        return type.enumConstants?.firstOrNull()
    }

    return when (type) {
        CharSequence::class.java,
        String::class.java,
        java.lang.CharSequence::class.java,
        java.lang.String::class.java -> "${fullyQualifiedParamName}Value"
        Boolean::class.java,
        java.lang.Boolean::class.java -> false
        Int::class.java,
        Integer::class.java -> 0
        Long::class.java,
        java.lang.Long::class.java -> 0L
        Float::class.java,
        java.lang.Float::class.java -> 0.0f
        Double::class.java,
        java.lang.Double::class.java -> 0.0
        Date::class.java -> params.referenceDate ?: Date(0)
        Char::class.java -> 'a'
        java.lang.Character::class.java -> 'a'
        Byte::class.java -> 0.toByte()
        java.lang.Byte::class.java -> 0.toByte()
        Short::class.java -> 0.toShort()
        java.lang.Short::class.java -> 0.toShort()
        java.time.Instant::class.java -> Instant.EPOCH
        IntArray::class.java -> intArrayOf(0, 0, 0)
        FloatArray::class.java -> floatArrayOf(0f, 0f, 0f)
        DoubleArray::class.java -> doubleArrayOf(0.0, 0.0, 0.0)
        ShortArray::class.java -> shortArrayOf(0, 0, 0)
        ByteArray::class.java -> byteArrayOf(0, 0, 0)
        CharArray::class.java -> charArrayOf('a', 'a', 'a')
        LongArray::class.java -> longArrayOf(0L, 0L, 0L)
        BooleanArray::class.java -> booleanArrayOf(false, false, false)
        else -> (type as Class<*>).kotlin.generateTestObject(
            fullyQualifiedParamName,
            params,
            // TODO(briant): Infinite recursion can occur if we have nested objects of the
            // same class. Will fix this in a later CR, though.
        )
    }
}

private data class Parameters(
    val overrides: Map<Regex, Any?>?,
    val referenceDate: Date?,
    val useNullForNullableFields: Boolean,
    val collectionSize: Int,
)
