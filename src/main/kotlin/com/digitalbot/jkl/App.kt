package com.digitalbot.jkl

import kotlin.system.exitProcess

val datetimeLogger = org.slf4j.LoggerFactory.getLogger("com.digitalbot.jkl.AppKt.datetime")!!
val straightLogger = org.slf4j.LoggerFactory.getLogger("com.digitalbot.jkl.AppKt.straight")!!

fun main() {
    try {
        val client = JmxClient("", 0)
        straightLogger.info(client.getBeanNames().toString())
    } catch (e: JmxClientException) {
        straightLogger.error(e.message)
        exitProcess(1)
    }
}
