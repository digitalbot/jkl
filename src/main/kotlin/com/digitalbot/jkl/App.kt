/*
 * The MIT License (MIT)
 *
 * Copyright 2019 Kosuke Nakamura a.k.a. digitalbot
 */
package com.digitalbot.jkl

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.arguments.validate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
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
            .convert { it.split(":").let { t -> t[0] to t[1] } }
            .validate { require(it.second.toIntOrNull() != null) { "Host and port must be joined by ':'." } }

    /**
     * bean name
     */
    private val bean by argument().optional()

    /**
     * attribute name
     */
    private val attribute by argument().optional()

    /**
     * type name (for composite date supported attribute.)
     */
    private val type by argument().optional()

    /**
     * requires "BEAN\tATTRIBUTE[\tTYPE][\tALIAS]"
     */
    private val targets by option("-t", "--target", help = """
        Usage: \"BEAN\\tATTRIBUTE[\\tTYPE][\\tALIAS]\".
        This option suppress specifying error's message.
        An alias cannot contains double quote and comma.
        """)
            .convert { it.replace("\\t", "\t").split("\t") }
            .multiple()
            .validate { target ->
                require(target.find { it.size == 1 } == null) { "Target must be splittable by tab character." }
            }

    /**
     * Only check client can connect to JMX Server if specified.
     */
    private val ping by option("-p", "--ping").flag()

    /**
     * Show header. Only can specify with returning value calling.
     */
    private val showKeys by option("--show-keys").flag()

    /**
     * Implementation of CLI application.
     */
    override fun run() {
        // validate. correlation check.
        if ((attribute ?: bean) != null && targets.isNotEmpty()) {
            echo(message = "Cannot specify BEAN or ATTRIBUTE argument with '--targets' option.", err = true)
            exitProcess(1)
        }
        if (showKeys && (attribute == null && targets.isEmpty())) {
            echo(message = "Cannot specify '--show-keys' without attribute or '--targets' option.", err = true)
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
                            if (type != null) {
                                val result = client.getValue(bean!!, attribute!!, type!!)
                                if (showKeys) {
                                    echo(result.getHeader())
                                }
                                echo(result.value)
                            } else {
                                val values = client.getValues(bean!!, attribute!!)
                                if (showKeys) {
                                    val headers = values.map { it.getHeader() }
                                    echo(headers.joinToString(","))
                                }
                                val result = values.map { it.value }
                                echo(result.joinToString(","))
                            }
                        } else {
                            // show attribute list
                            val attributeNames = client.getAttributeNames(bean!!)
                            echo(attributeNames.joinToString(","))
                        }
                    }

                    // show values
                    targets.isNotEmpty() -> {
                        val values = targets
                                .map {
                                    if (it.size >= 3) {
                                        listOf(client.getValueOrNull(it[0], it[1], it[2]))
                                    } else {
                                        client.getValuesOrNull(it[0], it[1])
                                    }
                                }
                                .flatten()
                        if (showKeys) {
                            val headers = targets
                                    .mapIndexed { index, list ->
                                        if (list.size >= 4) {
                                            list[3]
                                        } else {
                                            values[index]?.getHeader() ?: ""
                                        }
                                    }
                            echo(headers.joinToString(","))
                        }
                        val result = values.map { it?.value ?: "" }
                        echo(result.joinToString(","))
                    }

                    // show all beans
                    else -> {
                        val beans = client.getBeanNames()
                                .map { "\"${it.replace("\"", "\\\"")}\"" }
                        echo(beans.joinToString(","))
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
