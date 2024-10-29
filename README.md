# Kotlin Testing Tools

This is a collection of utility functions that can be used to help write automation tests for Kotlin code
(e. g. unit tests). Currently this repo contains the `generateTestObject()` function which allows you
to create a fake object for a data class with real, stable values for all fields with just one line of code.

## How To Use

Kotlin Testing Tools is published using JitPack. [Follow the instructions here to add it to your project.](https://jitpack.io/#thumbtack/kotlin-testing-tools)

Then to use `generateTestObject()` simply call:

```kotlin
val myTestObject = MyTestClass::class.generateTestObject()
```

At the moment, only Kotlin/JVM is supported, but suport for other languages in KMP is forthcoming.

## Documentation

### Purpose

The `generateTestObject()` function creates an object for a Kotlin class, without the caller needing to specify each
field’s value. Rather, each field’s value is generated with a unique but consistent value (as opposed to just some
default value like empty string or null). Furthermore, it traverses embedded classes and generates values for their
fields, and so on down the tree, and also creates values for collection objects (lists, maps, etc.). It is particularly
useful for data objects that have a lot of fields, e. g. objects returned in network responses like GraphQL queries.
It avoids the need for developers to have to specify each field for such large objects, thereby saving developer time,
boilerplate, lines of code and test development time.

### What It Does

The signature for `generateTestObject` is:

```kotlin
fun <T : Any> KClass<T>.generateTestObject(
    prefix: String = "",
    overrides: Map<Regex, Any?>? = null,
    referenceDate: Date? = null,
    useNullForNullableFields: Boolean = false,
): T
```

If you call it with all its defaults:

```kotlin
val myTestObject = MyTestClass::class.generateTestObject()
```

You'll get back an object will all fields filled in with non-empty values:
* Strings are given values of the form `<fieldName>Value`.
* Numbers are set to zero.
* Booleans are set to `false`.
* String collections (lists, sets, arrays): each value is appended their index position.
* Maps are set to the values [(`<fieldName><index>Key`, `<fieldName><index>Value`), ...]
* Nested values are of the form: `<parentFieldName>.<childValue>`.
* Collections of aggregate types have values of the form: `<parentFieldName><index>.<child>Value`.
* Dates and Instants are set to UNIX epoch time unless overridden by [referenceDate]
* Characters are set to 'a'

You can also choose to have specific fields be of a single value, by passing in a map
where the key is the regex of the field(s) to set, and the value is the value to assign.
Fields are referenced in "dot" notation like `<parent>.<field>`. So if you have the following:

```kotlin
data class OuterTestClass(
    val inner: InnerTestClass,
    val one: String
)
data class InnerTestClass(
    val one: String
)
```

You can pass in an override map of `("inner.one".toRegex() to "MyValue")` to set `inner.one`
to `"MyValue"`. You can set *all* fields called `one` to the same value by specifying
`(".*\.?one".toRegex() to "MyValue")`

There is also the flag [useNullForNullableFields] that you may enable to enforce all nullable
fields to be populated with null.

### Philosophy

When writing tests, you often need to pass in data objects to the method-under-test. Take, for instance, a user
interface test. You want to ensure that all fields in that user interface are rendered correctly. You may have a method
like the following:

```kotlin
@Compose
fun MyScreen(model: MyModel) {
    Column {
        Text(model.firstField)
        Image(drawableUrl: imageField)
        . . .
    }
}
```

You could pass in a mock, but mocking will typically create “empty” values for each field: typically null for reference
types and zero or “falsy” values for primitives. That won’t render much in a UI test. Furthermore, it won’t test
contained objects, for instance if MyModel contained fields that were also aggregate types or collections:

```kotlin
data class MyModel(
    val firstField: String,
    val drawableUrl: String,
    val subsection: MySubsection,
    val listOfCustomers: List<Customer>,
    . . .
}
```

So why not just call the class constructor? Problem solved, right?

But what if your data object had 10 or more fields? And then what if each of those fields were aggregate types each with
several fields? It would involve having to write out a huge constructor call, not to mention taking the time to think of
values for each field!

That was one of the original intentions, and probably the most important feature, of generateTestObject(): to not only
generate a real object as a test fixture, but to save typing and the tedium of creating those objects.
generateTestObject() will create consistent values for each field of a data class, including nested classes within it as
well as collection types (arrays, lists, maps, etc.).

But generateTestObject() has another useful benefit. Every single string field has a unique, but predictable, value
within the object; even nested fields and values in collections all have unique values (for strings). This was primarily
done for the benefit of UI testing. So if you have to verify that 10 fields are rendered correctly on the screen, instead
of:

```kotlin
forViewWithId(viewId1).assertTextValueIs(myModel.field1)
forViewWithId(viewId1).forChildViewWithId(viewId2).assertTextValueIs(myModel.field2)
forViewWithId(viewId1).forChildViewWithId(viewId2).forChildViewWithId(viewId3).assertTextValueIs(myModel.field3)
forViewWithId(viewId4).assertTextValueIs(myModel.field4)
. . .
forViewWithId(viewId10).forChildViewWithid(viewId11).assertTextValueIs(myModel.field10)
```

(which can actually be even more complicated than that if you have to traverse hierarchies of views) you can do
something like this:

```kotlin
forViewWithId(rootViewId).assertHasDescendantsWithValues(
    myModel.field1, myModel.field2, myModel.field3, myModel.field4, myModel.field5, myModel.field6, myModel.field7, myModel.field8, myModel.field9, myModel.field10
)
```

It doesn’t save a lot of typing; rather the real savings is in not needing to find exact views because the uniqueness of
each field’s values ensures it gets rendered. True, it’s not as accurate in testing because it could be that a developer
transposed which UI element was supposed to render a particular field; but it does provide a convenient shorthand to at
least generate a test that provides reasonable confidence.

And generateTestObject() has other benefits:
* Makes it easy to test transformer functions: those functions that transform one data type into another. Especially for
larger data objects. e. g.:
```kotlin
val originObject = OriginData::class.generateTestObject()
val destObject = DestinationData.from(originObject)
assertThat(destObject.header).isEqualTo(originObject.title)
. . .
```
* Enables you to create a fake backend for developers and automated integration testing. Instead of the tedium of having
to define each fake response, generateTestObject() can do most of the work and we simply provide overrides for those
fields that are critical for our test situation (e. g. particular error conditions, deeplinks)
* Enables us to quickly write screenshot tests and UI previews (like Compose previews) without the tedium of spelling out
every field, only overriding those we need to.

## Contributing

Kotlin Testing Tools accepts issues and pull requests. Take at look at our [contribution instructions](CONTRIBUTING.md) if you'd like to contribute.

## License

Kotlin Testing Tools is licensed under the terms of the [Apache License 2.0](LICENSE).

