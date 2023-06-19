package com.crakac.blenc.util

import timber.log.Timber

class DebugTree: Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String {
        val className = super.createStackElementTag(element)
        return "$className:${element.methodName}"
    }
}