package com.thumbtack.kotlin.test

import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateTestObjectTest {
    class SampleClass(
        val stringField: String,
        val intField: Int,
        val floatField: Float,
        val doubleField: Double,
        val booleanField: Boolean,
        val charField: Char,
        val listField: List<String>,
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
    }
}
