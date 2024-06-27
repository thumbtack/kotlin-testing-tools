package com.thumbtack.kotlin.test

import java.time.Instant
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateTestObjectTest {
    class SampleClass(
        val stringField: String,
        val intField: Int,
        val floatField: Float,
        val doubleField: Double,
        val booleanField: Boolean,
        val charField: Char,
        val listField: List<String>,
        val setField: Set<String>,
        val mapField: Map<String, String>,
        val intArray: IntArray,
        val floatArray: FloatArray,
        val doubleArray: DoubleArray,
        val booleanArray: BooleanArray,
        val charArray: CharArray,
        val shortArray: ShortArray,
        val byteArray: ByteArray,
        val date: Date,
        val instant: Instant,
    )

    @Test
    fun `basic test object generation works`() {
        val testObject = SampleClass::class.generateTestObject()
        assertEquals("stringFieldValue", testObject.stringField)
        assertEquals(0, testObject.intField)
        assertEquals(0f, testObject.floatField)
        assertEquals(0.0, testObject.doubleField)
        assertEquals(false, testObject.booleanField)
        assertEquals('a', testObject.charField)
        assertEquals(3, testObject.listField.size)
        testObject.listField.forEachIndexed { index, item ->
            assertEquals("listField${index}Value", item)
        }
        assertEquals(3, testObject.setField.size)
        testObject.setField.forEachIndexed { index, item ->
            assertEquals("setField${index}Value", item)
        }
        assertEquals(3, testObject.mapField.size)
        testObject.mapField.onEachIndexed { index, entry ->
            assertEquals("mapField${index}keyValue", entry.key)
            assertEquals("mapField${index}valueValue", entry.value)
        }
        assertEquals(3, testObject.intArray.size)
        assertTrue(testObject.intArray.all { it == 0 })
        assertEquals(3, testObject.floatArray.size)
        assertTrue(testObject.floatArray.all { it == 0f })
        assertEquals(3, testObject.doubleArray.size)
        assertTrue(testObject.doubleArray.all { it == 0.0 })
        assertEquals(3, testObject.charArray.size)
        assertTrue(testObject.charArray.all { it == 'a' })
        assertEquals(3, testObject.shortArray.size)
        assertTrue(testObject.shortArray.all { it == 0.toShort() })
        assertEquals(3, testObject.byteArray.size)
        assertTrue(testObject.byteArray.all { it == 0.toByte() })
        assertEquals(3, testObject.booleanArray.size)
        assertTrue(testObject.booleanArray.all { !it })
        assertEquals(Date(0), testObject.date)
        assertEquals(Instant.EPOCH, testObject.instant)
    }

    @Test
    fun `test collectionSize`() {
        val collectionSize = 5
        val testObject = SampleClass::class.generateTestObject(collectionSize = collectionSize)

        assertEquals(collectionSize, testObject.listField.size)
        assertEquals(collectionSize, testObject.setField.size)
        assertEquals(collectionSize, testObject.mapField.size)
    }

    @Test
    fun `test referenceDate`() {
        val currentDate = Date()
        val testObject = SampleClass::class.generateTestObject(referenceDate = currentDate)

        assertEquals(currentDate, testObject.date)
        assertEquals(currentDate.toInstant(), testObject.instant)
        assertEquals(testObject.date.toInstant(), testObject.instant)
    }
}
