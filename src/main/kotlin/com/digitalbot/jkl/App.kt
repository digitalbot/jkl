/*
 * The MIT License (MIT)
 *
 * Copyright 2019 Kosuke Nakamura a.k.a. digitalbot
 */
package com.digitalbot.jkl

import kotlin.system.exitProcess

/** for print messages to stdout with timestamp */
val datetimeLogger = org.slf4j.LoggerFactory.getLogger("com.digitalbot.jkl.AppKt.datetime")!!

/** for print only messages to stdout */
val straightLogger = org.slf4j.LoggerFactory.getLogger("com.digitalbot.jkl.AppKt.straight")!!

/**
 * Main
 *
 * This is a command line jmx client tool's endpoint.
 * This tool is written by kotlin. So, this is called Jmx KLient JKL (Jekyll).
 *
 * @author digitalbot
 */
fun main() {
    try {
        val client = JmxClient("", 0)
        straightLogger.info(client.getBeanNames().toString())
    } catch (e: JmxClientException) {
        straightLogger.error(e.message)
        exitProcess(1)
    }
}
