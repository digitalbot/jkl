/*
 * The MIT License (MIT)
 *
 * Copyright 2019 Kosuke Nakamura a.k.a. digitalbot
 */
package com.digitalbot.jkl

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import kotlin.system.exitProcess

/**
 * Jkl - commandline jmx client tool entity.
 *
 * @author digitalbot
 */
class Jkl : CliktCommand() {
    /**
     * requires valid jmx rmi "host:port"
     */
    private val hostport by argument("HOST:PORT")
            .convert { it.split(":").let { t -> Pair(t[0], t[1]) } }
            .validate { require(it.second.toIntOrNull() != null) }

    /**
     * bean name
     */
    private val bean by argument().optional()

    /**
     * attribute name
     */
    private val attribute by argument().optional()

    /**
     * requires "BEAN\tCOMMAND"
     */
    private val targets by option("-t", "--target")
            .convert { it.replace("\\t", "\t").split("\t").let { t -> Pair(t[0], t[1]) } }
            .multiple()

    /**
     * Only check client can connect to JMX Server if specified.
     */
    private val ping by option("-p", "--ping").flag()

    /**
     * Implementation of CLI application.
     */
    override fun run() {
        // validate. correlation check.
        if ((attribute ?: bean) != null && targets.isNotEmpty()) {
            echo(message = "Cannot specify BEAN or ATTRIBUTE argument with '--targets' option.", err = true)
            exitProcess(1)
        }
        try {
            JmxClient(hostport.first, hostport.second.toInt()).use { client ->
                when {
                    // do nothing
                    ping -> true

                    // bean and attribute arguments
                    bean != null -> {
                        if (attribute != null) {
                            // show values
                            val values = client.getValues(bean!!, attribute!!)
                            val result = values.map { it.value }
                            echo(result.joinToString(","))
                        } else {
                            // show attribute list
                            val attributeNames = client.getAttributeNames(bean!!)
                            attributeNames.forEach { echo(it) }
                        }
                    }
                    // show values
                    targets.isNotEmpty() -> {
                        val values = targets.map { client.getValues(it.first, it.second) }.flatten()
                        val result = values.map { it.value }
                        echo(result.joinToString(","))
                    }
                    // show all beans
                    else -> {
                        client.getBeanNames().forEach { echo(it) }
                    }
                }
            }
        } catch (e: JmxClientException) {
            echo(message = e.message, err = true)
            exitProcess(1)
        }
    }
}

/**
 * Main
 *
 * This is a command line jmx client tool's endpoint.
 * This tool is written by kotlin. So, this is called Jmx KLient JKL (Jekyll).
 *
 * @author digitalbot
 */
fun main(argv: Array<String>) = Jkl().main(argv)
