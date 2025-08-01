package org.jetbrains.skia.impl

internal actual class InteropScope actual constructor() {
    private val elements = mutableListOf<NativePointer>()
    private var callbacksInitialized = false

    actual fun toInterop(string: String?): InteropPointer {
        return if (string != null) {
            toInterop(convertToZeroTerminatedString(string))
        } else {
            0
        }
    }

    private fun toInterop(array: ByteArray?, copyArrayToWasm: Boolean): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size)
            elements.add(data)
            if (copyArrayToWasm) toWasm(data, array)
            data
        } else {
            0
        }
    }

    actual fun toInterop(array: ByteArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = true)

    actual fun toInteropForResult(array: ByteArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = false)

    actual fun InteropPointer.fromInterop(result: ShortArray) {
        fromWasm(this@fromInterop, result)
    }

    private fun toInterop(array: ShortArray?, copyArrayToWasm: Boolean): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size * 2)
            elements.add(data)
            if (copyArrayToWasm) toWasm(data, array)
            data
        } else {
            0
        }
    }

    actual fun toInterop(array: ShortArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = true)

    actual fun toInteropForResult(array: ShortArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = false)

    actual fun InteropPointer.fromInterop(result: IntArray) {
        fromWasm(this@fromInterop, result)
    }

    private fun toInterop(array: IntArray?, copyArrayToWasm: Boolean): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size * 4)
            elements.add(data)
            if (copyArrayToWasm) toWasm(data, array)
            data
        } else {
            0
        }
    }

    actual fun toInterop(array: IntArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = true)

    actual fun toInteropForResult(array: IntArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = false)

    actual fun InteropPointer.fromInterop(result: LongArray) {
        TODO("implement wasm fromInterop(LongArray)")
    }

    actual fun toInterop(array: LongArray?): InteropPointer {
        TODO("implement wasm toInterop(LongArray)")
    }

    actual fun InteropPointer.fromInterop(result: FloatArray) {
        fromWasm(this@fromInterop, result)
    }

    private fun toInterop(array: FloatArray?, copyArrayToWasm: Boolean): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size * 4)
            elements.add(data)
            if (copyArrayToWasm) toWasm(data, array)
            data
        } else {
            0
        }
    }

    actual fun toInterop(array: FloatArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = true)

    actual fun toInteropForResult(array: FloatArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = false)

    actual fun InteropPointer.fromInterop(result: DoubleArray) {
        fromWasm(this@fromInterop, result)
    }

    private fun toInterop(array: DoubleArray?, copyArrayToWasm: Boolean): InteropPointer {
        return if (array != null && array.isNotEmpty()) {
            val data = _malloc(array.size * 8)
            elements.add(data)
            if (copyArrayToWasm) toWasm(data, array)
            data
        } else {
            0
        }
    }

    actual fun toInterop(array: DoubleArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = true)

    actual fun toInteropForResult(array: DoubleArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = false)

    actual fun InteropPointer.fromInterop(result: ByteArray) {
        fromWasm(this@fromInterop, result)
    }

    private fun toInterop(array: NativePointerArray?, copyArrayToWasm: Boolean): InteropPointer {
        return if (array != null && array.size > 0) {
            val data = _malloc(array.size * 4)
            elements.add(data)
            if (copyArrayToWasm) toWasm(data, array.backing)
            data
        } else {
            0
        }
    }

    actual fun toInterop(array: NativePointerArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = true)

    actual fun toInteropForResult(array: NativePointerArray?): InteropPointer =
        toInterop(array = array, copyArrayToWasm = false)

    actual fun InteropPointer.fromInterop(result: NativePointerArray) {
        return fromWasm(this@fromInterop, result.backing)
    }

    actual fun toInterop(stringArray: Array<String>?): InteropPointer {
        return if (stringArray != null && stringArray.isNotEmpty()) {
            val ptrs = stringArray.map {
                toInterop(it)
            }.toIntArray()

            toInterop(ptrs)
        } else {
            0
        }
    }

    actual fun InteropPointer.fromInteropNativePointerArray(): NativePointerArray {
        TODO("implement wasm fromInteropNativePointerArray")
    }

    actual inline fun <reified T> InteropPointer.fromInterop(decoder: ArrayInteropDecoder<T>): Array<T> {
        val size = decoder.getArraySize(this)
        val result = Array(size) {
            decoder.getArrayElement(this, it)
        }
        decoder.disposeArray(this)
        return result
    }

    actual fun toInteropForArraysOfPointers(interopPointers: Array<InteropPointer>): InteropPointer {
        return toInterop(interopPointers.toIntArray())
    }

    actual fun booleanCallback(callback: (() -> Boolean)?): NativePointer {
        if (callback == null) { return 0 }
        initCallbacks()
        val data = crateCallbackObj() as CallbackDataBoolean
        return _registerCallback({ data.value = callback() }, data, global = false)
    }

    actual fun intCallback(callback: (() -> Int)?): NativePointer {
        if (callback == null) { return 0 }
        initCallbacks()
        val data = crateCallbackObj() as CallbackDataInt
        return _registerCallback({ data.value = callback() }, data, global = false)
    }

    actual fun nativePointerCallback(callback: (() -> NativePointer)?): NativePointer {
        if (callback == null) { return 0 }
        initCallbacks()
        val data = crateCallbackObj() as CallbackDataNativePointer
        return _registerCallback({ data.value = callback() }, data, global = false)
    }

    actual fun interopPointerCallback(callback: (() -> InteropPointer)?): NativePointer {
        if (callback == null) { return 0 }
        initCallbacks()
        val data = crateCallbackObj() as CallbackDataInteropPointer
        return _registerCallback({ data.value = callback() }, data, global = false)
    }

    actual fun callback(callback: (() -> Unit)?): InteropPointer {
        if (callback == null) { return 0 }
        initCallbacks()

        return _registerCallback({ callback() }, null, global = false)
    }

    actual fun virtual(method: () -> Unit): InteropPointer {
        return _registerCallback({ method() }, null, global = true)
    }

    actual fun virtualBoolean(method: () -> Boolean): InteropPointer {
        val data = crateCallbackObj() as CallbackDataBoolean
        return _registerCallback({ data.value = method() }, data, global = true)
    }

    actual fun virtualInt(method: () -> Int): InteropPointer {
        val data = crateCallbackObj() as CallbackDataInt
        return _registerCallback({ data.value = method() }, data, global = true)
    }

    actual fun virtualNativePointer(method: () -> NativePointer): InteropPointer {
        val data = crateCallbackObj() as CallbackDataNativePointer
        return _registerCallback({ data.value = method() }, data, global = true)
    }

    actual fun virtualInteropPointer(method: () -> InteropPointer): InteropPointer {
        val data = crateCallbackObj() as CallbackDataInteropPointer
        return _registerCallback({ data.value = method() }, data, global = true)
    }

    actual fun release()  {
        elements.forEach {
            _free(it)
        }
        elements.clear()
        releaseCallbacks()
    }

    private inline fun initCallbacks() {
        if (!callbacksInitialized) {
            _createLocalCallbackScope()
            callbacksInitialized = true
        }
    }

    private inline fun releaseCallbacks() {
        if (callbacksInitialized) {
            _releaseLocalCallbackScope()
            callbacksInitialized = false
        }
    }
}

@JsFun("() => { return {} }")
private external fun crateCallbackObj(): JsAny

// Callbacks
internal external interface CallbackDataBoolean : JsAny { @JsName("value") var value: Boolean? }
internal external interface CallbackDataInt : JsAny { @JsName("value") var value: Int? }
internal external interface CallbackDataNativePointer : JsAny { @JsName("value") var value: NativePointer? }
internal external interface CallbackDataInteropPointer: JsAny { @JsName("value") var value: InteropPointer? }